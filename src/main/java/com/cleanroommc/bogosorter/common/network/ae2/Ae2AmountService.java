package com.cleanroommc.bogosorter.common.network.ae2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.bogosorter.common.config.ae2.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.compat.ThaumicEnergisticsHelper;
import com.github.bsideup.jabel.Desugar;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;

public final class Ae2AmountService {

    private static final int PLAYER_REQUESTS_PER_SECOND = 10;
    private static final int PLAYER_REQUEST_BURST = 40;
    private static final int NETWORK_LOOKUPS_PER_SECOND = 50;
    private static final int NETWORK_LOOKUP_BURST = 50;
    private static final int DEGRADED_NETWORK_LOOKUPS_PER_SECOND = 15;
    private static final int DEGRADED_NETWORK_LOOKUP_BURST = 15;
    private static final int MAX_PLAYER_LIMITS = 1024;
    private static final int MAX_LOOKUPS_PER_CONTEXT = 1024;
    private static final long CONTEXT_CACHE_MS = 1000L;
    private static final long CONTEXT_CACHE_RETAIN_MS = 30000L;
    private static final long LOOKUP_CACHE_MS = 3000L;
    private static final long ZERO_LOOKUP_CACHE_MS = 5000L;
    private static final long DEGRADED_LOOKUP_CACHE_MS = 10000L;
    private static final long CACHE_RETAIN_MS = 30000L;
    private static final long CACHE_CLEANUP_INTERVAL_MS = 30000L;
    private static final long HIGH_LOAD_TICK_TIME_NS = 75000000L;
    private static final long SERVER_LOAD_CACHE_MS = 5000L;
    private static final int HOT_CACHE_HIT_THRESHOLD = 10;
    private static final long HOT_CACHE_TTL_MULTIPLIER = 2L;
    private static final int CONTEXT_RETRY_MS = 1500;
    private static final int THROTTLE_RETRY_MS = 1000;
    private static final double WIRELESS_POWER_CHECK_AMOUNT = 1.0D;
    private static final double MILLIS_PER_SECOND = 1000.0D;
    private static final String WIRELESS_ACCESS_POINT_CLASS = "appeng.tile.networking.TileWireless";
    private static final String AE2FC_BASE_CONTAINER_CLASS = "com.glodblock.github.client.gui.container.base.FCBaseContainer";

    // player limits and context cache use concurrent hashmap now
    private static final Map<String, RateLimit> PLAYER_LIMITS = new ConcurrentHashMap<>();
    private static final Map<Object, RateLimit> NETWORK_LIMITS = new WeakHashMap<>();
    private static final Map<Object, BoundedLookupCache> LOOKUP_CACHES = new WeakHashMap<>();
    private static final Map<UUID, ContextCacheEntry> CONTEXT_CACHE = new ConcurrentHashMap<>();
    // baubles reflection gets cached so we dont look it up every time
    private static final Map<String, BaublesAccessor> BAUBLES_ACCESSORS = new ConcurrentHashMap<>();
    private static long nextCleanupTime;
    private static long nextServerLoadCheckTime;
    private static boolean cachedServerUnderLoad;

    private Ae2AmountService() {}

    static boolean arePlayerRequestsLimited(EntityPlayerMP player, long now, int count) {
        cleanupCaches(now);
        String key = playerKey(player);
        RateLimit limit = PLAYER_LIMITS
            .computeIfAbsent(key, ignored -> new RateLimit(PLAYER_REQUESTS_PER_SECOND, PLAYER_REQUEST_BURST, now));
        // rate limit checks all tokens at once now
        if (limit.isThrottled(now, count)) {
            IntegrationDiagnostics.recordThrottle();
            return true;
        }
        trimPlayerLimits();
        return false;
    }

    public static ContextResult resolvePlayerContext(EntityPlayerMP player, long now) {
        if (player == null) {
            return ContextResult.noSystem();
        }

        ContextResult openTerminal = getOpenTerminalContext(player);
        if (openTerminal.isAvailable()) {
            IntegrationDiagnostics.recordContextResolution(false);
            return openTerminal;
        }

        UUID playerId = player.getUniqueID();
        int signature = wirelessInventorySignature(player);
        ContextCacheEntry cached = CONTEXT_CACHE.get(playerId);
        if (cached != null && now <= cached.expiresAt
            && cached.dimension == player.dimension
            && cached.signature == signature
            && cached.blockX == floor(player.posX)
            && cached.blockY == floor(player.posY)
            && cached.blockZ == floor(player.posZ)) {
            IntegrationDiagnostics.recordContextResolution(true);
            return cached.result;
        }

        ContextResult result = getWirelessTerminalContext(player);
        IntegrationDiagnostics.recordContextResolution(false);
        CONTEXT_CACHE.put(
            playerId,
            new ContextCacheEntry(
                result,
                player.dimension,
                signature,
                floor(player.posX),
                floor(player.posY),
                floor(player.posZ),
                now + CONTEXT_CACHE_MS));
        return result;
    }

    public static AmountLookupResult lookupAmount(PlayerAeContext context, ItemStack stack, FluidStack fluidStack,
        String essentiaAspectTag, long now) {
        if (context == null || context.host == null) {
            return AmountLookupResult.noSystem();
        }

        LookupKey lookupKey = lookupKeyOf(stack, fluidStack, essentiaAspectTag);
        if (lookupKey == null) {
            return AmountLookupResult.error();
        }

        boolean degraded = isServerUnderLoad();
        Object cacheOwner = context.cacheOwner;
        BoundedLookupCache cache = LOOKUP_CACHES.computeIfAbsent(cacheOwner, ignored -> new BoundedLookupCache());
        LookupCacheEntry cached = cache.get(lookupKey);
        if (cached != null && now - cached.createdAt <= lookupCacheTtl(cached, degraded)) {
            cached.hits++;
            IntegrationDiagnostics.recordLookupCacheHit();
            return AmountLookupResult.ok(cached.amount);
        }

        if (degraded && cached != null && now - cached.createdAt <= CACHE_RETAIN_MS) {
            cached.hits++;
            IntegrationDiagnostics.recordLookupCacheHit();
            return AmountLookupResult.ok(cached.amount);
        }

        int perSecond = degraded ? DEGRADED_NETWORK_LOOKUPS_PER_SECOND : NETWORK_LOOKUPS_PER_SECOND;
        int burst = degraded ? DEGRADED_NETWORK_LOOKUP_BURST : NETWORK_LOOKUP_BURST;
        RateLimit networkLimit = NETWORK_LIMITS
            .computeIfAbsent(cacheOwner, ignored -> new RateLimit(perSecond, burst, now));
        if (networkLimit.isThrottled(now, 1)) {
            IntegrationDiagnostics.recordThrottle();
            return cached == null ? AmountLookupResult.throttled() : AmountLookupResult.ok(cached.amount);
        }
        if (degraded && cached == null) {
            IntegrationDiagnostics.recordThrottle();
            return AmountLookupResult.throttled();
        }

        AmountLookupResult result;
        if (essentiaAspectTag != null) {
            if (!TooltipFeatureConfig.isThaumicEnabled()) {
                return AmountLookupResult.unsupported();
            }
            result = getTerminalEssentiaAmount(context.grid, essentiaAspectTag);
        } else if (fluidStack != null) {
            result = AmountLookupResult.ok(getTerminalFluidAmount(context.host, fluidStack));
        } else {
            result = AmountLookupResult.ok(getTerminalItemAmount(context.host, stack));
        }

        if (result.status == Ae2Status.OK) {
            cache.put(lookupKey, new LookupCacheEntry(result.amount, now));
        }
        return result;
    }

    /**
     * Resolves many tooltip lookups for one player context. Duplicate item/fluid/essentia keys within the
     * batch hit {@link #lookupAmount} only once, which keeps large ME networks responsive when NEI shows
     * repeated stacks in a recipe view.
     */
    public static List<AmountLookupResult> lookupAmountBatch(PlayerAeContext context, List<BatchLookupEntry> entries,
        long now) {
        if (context == null || entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LookupKey, AmountLookupResult> shared = new LinkedHashMap<>();
        List<AmountLookupResult> results = new ArrayList<>(entries.size());
        for (BatchLookupEntry entry : entries) {
            LookupKey key = lookupKeyOf(entry.stack, entry.fluidStack, entry.essentiaAspectTag);
            if (key == null) {
                results.add(AmountLookupResult.error());
                continue;
            }
            AmountLookupResult result = shared.get(key);
            if (result == null) {
                result = lookupAmount(context, entry.stack, entry.fluidStack, entry.essentiaAspectTag, now);
                shared.put(key, result);
            }
            results.add(result);
        }
        return results;
    }

    /** Distinct lookup keys in a batch — used for fair per-player rate limiting. */
    public static int countDistinctLookupKeys(List<BatchLookupEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        Set<LookupKey> keys = new HashSet<>();
        for (BatchLookupEntry entry : entries) {
            LookupKey key = lookupKeyOf(entry.stack, entry.fluidStack, entry.essentiaAspectTag);
            if (key != null) {
                keys.add(key);
            }
        }
        return keys.size();
    }

    public static void clearCaches() {
        PLAYER_LIMITS.clear();
        NETWORK_LIMITS.clear();
        LOOKUP_CACHES.clear();
        CONTEXT_CACHE.clear();
    }

    public static void clearPlayer(EntityPlayerMP player) {
        if (player == null) return;
        PLAYER_LIMITS.remove(playerKey(player));
        CONTEXT_CACHE.remove(player.getUniqueID());
    }

    private static ContextResult getOpenTerminalContext(EntityPlayerMP player) {
        Container container = player.openContainer;
        if (container instanceof ContainerMEMonitorable monitorable) {
            if (!container.canInteractWith(player)) {
                return ContextResult.noSystem();
            }
            Object target = monitorable.getTarget();
            if (!(target instanceof ITerminalHost host)) {
                return ContextResult.noSystem();
            }
            IGridNode node = monitorable.getNetworkNode();
            IGrid grid = node == null ? getGrid(host) : node.getGrid();
            return ContextResult.ok(new PlayerAeContext(host, grid));
        }

        if (container == null || !isAe2FcBaseContainer(container) || !container.canInteractWith(player)) {
            return ContextResult.noSystem();
        }
        try {
            Object host = invokeGetHost(container);
            if (!(host instanceof ITerminalHost terminalHost)) {
                return ContextResult.noSystem();
            }
            return ContextResult.ok(new PlayerAeContext(terminalHost, getGrid(terminalHost)));
        } catch (ReflectiveOperationException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("ae2fc-container-host", e);
            return ContextResult.noSystem();
        }
    }

    private static ContextResult getWirelessTerminalContext(EntityPlayerMP player) {
        ContextResult fallback = null;
        IInventory baubles = getBaublesInventory(player);
        if (baubles != null) {
            for (int slot = 0; slot < baubles.getSizeInventory(); slot++) {
                ContextResult result = createWirelessTerminalContext(player, baubles.getStackInSlot(slot));
                if (result == null) continue;
                if (result.isAvailable()) return result;
                fallback = preferredFailure(fallback, result);
            }
        }

        for (ItemStack stack : player.inventory.mainInventory) {
            ContextResult result = createWirelessTerminalContext(player, stack);
            if (result == null) continue;
            if (result.isAvailable()) return result;
            fallback = preferredFailure(fallback, result);
        }
        return fallback == null ? ContextResult.noSystem() : fallback;
    }

    private static ContextResult createWirelessTerminalContext(EntityPlayerMP player, ItemStack terminal) {
        try {
            if (!isWirelessTerminal(terminal)) {
                return null;
            }

            IWirelessTermHandler handler = getWirelessTerminalHandler(terminal);
            if (handler == null) {
                return ContextResult.noSystem();
            }
            ILocatable locatable = getLinkedWirelessTerminalHost(handler, terminal);
            if (!(locatable instanceof IGridHost gridHost)) {
                return ContextResult.noSystem();
            }
            IGridNode node = gridHost.getGridNode(ForgeDirection.UNKNOWN);
            IGrid grid = node == null ? null : node.getGrid();
            if (grid == null || !Ae2AccessHelper.canPlayerReadGrid(player, grid)) {
                return ContextResult.noSystem();
            }
            if (!isWirelessTerminalInRange(player, handler, terminal, grid)) {
                return ContextResult.outOfRange();
            }
            if (!hasWirelessTerminalPower(player, handler, terminal)) {
                return ContextResult.noSystem();
            }
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                return ContextResult.noSystem();
            }
            return ContextResult.ok(
                new PlayerAeContext(
                    new StorageGridTerminalHost(storageGrid, handler.getConfigManager(terminal), grid),
                    grid));
        } catch (RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("ae2-wireless-context", e);
            return ContextResult.noSystem();
        }
    }

    private static ContextResult preferredFailure(ContextResult current, ContextResult candidate) {
        if (current == null || candidate.status == Ae2Status.OUT_OF_RANGE) {
            return candidate;
        }
        return current;
    }

    private static IWirelessTermHandler getWirelessTerminalHandler(ItemStack terminal) {
        IWirelessTermHandler handler = AEApi.instance()
            .registries()
            .wireless()
            .getWirelessTerminalHandler(terminal);
        return handler != null && handler.canHandle(terminal) ? handler : null;
    }

    private static ILocatable getLinkedWirelessTerminalHost(IWirelessTermHandler handler, ItemStack terminal) {
        String encryptionKey = handler.getEncryptionKey(terminal);
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return null;
        }
        try {
            return AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(Long.parseLong(encryptionKey));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean hasWirelessTerminalPower(EntityPlayerMP player, IWirelessTermHandler handler,
        ItemStack terminal) {
        try {
            return handler.hasInfinityPower(terminal)
                || handler.hasPower(player, WIRELESS_POWER_CHECK_AMOUNT, terminal);
        } catch (RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("ae2-wireless-power", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean isWirelessTerminalInRange(EntityPlayerMP player, IWirelessTermHandler handler,
        ItemStack terminal, IGrid grid) {
        if (handler.hasInfinityRange(terminal)) {
            return true;
        }
        try {
            Class<?> tileWireless = Class.forName(WIRELESS_ACCESS_POINT_CLASS);
            IMachineSet accessPoints = grid.getMachines((Class<? extends IGridHost>) tileWireless);
            for (IGridNode accessPointNode : accessPoints) {
                Object machine = accessPointNode.getMachine();
                if (machine instanceof IWirelessAccessPoint
                    && isWirelessAccessPointInRange(player, (IWirelessAccessPoint) machine)) {
                    return true;
                }
            }
        } catch (ClassNotFoundException | RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("ae2-wireless-access-point", e);
        }
        return false;
    }

    private static boolean isWirelessAccessPointInRange(EntityPlayerMP player, IWirelessAccessPoint accessPoint) {
        if (!accessPoint.isActive()) return false;
        DimensionalCoord location = accessPoint.getLocation();
        if (location == null || location.getWorld() != player.worldObj) return false;
        double dx = location.x - player.posX;
        double dy = location.y - player.posY;
        double dz = location.z - player.posZ;
        double range = accessPoint.getRange();
        return dx * dx + dy * dy + dz * dz <= range * range;
    }

    private static boolean isWirelessTerminal(ItemStack stack) {
        return stack != null && stack.getItem() != null
            && AEApi.instance()
                .registries()
                .wireless()
                .isWirelessTerminal(stack);
    }

    private static IInventory getBaublesInventory(EntityPlayerMP player) {
        IInventory inventory = getBaublesInventory("baubles.common.lib.PlayerHandler", "getPlayerBaubles", player);
        return inventory == null ? getBaublesInventory("baubles.api.BaublesApi", "getBaubles", player) : inventory;
    }

    private static IInventory getBaublesInventory(String className, String methodName, EntityPlayerMP player) {
        BaublesAccessor accessor = BAUBLES_ACCESSORS
            .computeIfAbsent(className + '#' + methodName, ignored -> BaublesAccessor.resolve(className, methodName));
        if (!accessor.available()) {
            return null;
        }
        try {
            Object inventory = accessor.method.invoke(null, player);
            return inventory instanceof IInventory ? (IInventory) inventory : null;
        } catch (ReflectiveOperationException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce(className + '#' + methodName, e);
            return null;
        }
    }

    private static int wirelessInventorySignature(EntityPlayerMP player) {
        int result = 1;
        for (ItemStack stack : player.inventory.mainInventory) {
            result = 31 * result + stackSignature(stack);
        }
        IInventory baubles = getBaublesInventory(player);
        if (baubles != null) {
            for (int slot = 0; slot < baubles.getSizeInventory(); slot++) {
                result = 31 * result + stackSignature(baubles.getStackInSlot(slot));
            }
        }
        return result;
    }

    private static int stackSignature(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0;
        int result = System.identityHashCode(stack.getItem());
        result = 31 * result + stack.getItemDamage();
        result = 31 * result + (stack.getTagCompound() == null ? 0
            : stack.getTagCompound()
                .hashCode());
        return result;
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }

    private static boolean isAe2FcBaseContainer(Object instance) {
        Class<?> current = instance.getClass();
        while (current != null) {
            if (AE2FC_BASE_CONTAINER_CLASS.equals(current.getName())) return true;
            current = current.getSuperclass();
        }
        return false;
    }

    private static Object invokeGetHost(Object instance) throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod("getHost");
                method.setAccessible(true);
                return method.invoke(instance);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException("getHost");
    }

    private static IGrid getGrid(ITerminalHost host) {
        if (host instanceof StorageGridTerminalHost) {
            return ((StorageGridTerminalHost) host).grid;
        }
        if (host instanceof IGridHost gridHost) {
            IGridNode node = gridHost.getGridNode(ForgeDirection.UNKNOWN);
            return node == null ? null : node.getGrid();
        }
        return null;
    }

    private static long getTerminalItemAmount(ITerminalHost host, ItemStack stack) {
        if (stack == null || stack.getItem() == null) return 0L;
        IMEMonitor<IAEItemStack> inventory = host.getItemInventory();
        if (inventory == null || inventory.getStorageList() == null) return 0L;
        IAEItemStack requested = AEItemStack.create(stack);
        IAEItemStack found = inventory.getStorageList()
            .findPrecise(requested);
        return found == null ? 0L : found.getStackSize();
    }

    private static long getTerminalFluidAmount(ITerminalHost host, FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.getFluid() == null) return 0L;
        IMEMonitor<IAEFluidStack> inventory = host.getFluidInventory();
        if (inventory == null || inventory.getStorageList() == null) return 0L;
        IAEFluidStack requested = AEFluidStack.create(fluidStack);
        if (requested == null) return 0L;
        IAEFluidStack found = inventory.getStorageList()
            .findPrecise(requested);
        return found == null ? 0L : found.getStackSize();
    }

    private static AmountLookupResult getTerminalEssentiaAmount(IGrid grid, String aspectTag) {
        if (grid == null) return AmountLookupResult.noSystem();
        ThaumicEnergisticsHelper.AmountResult result = ThaumicEnergisticsHelper.getEssentiaAmount(grid, aspectTag);
        if (result.isSuccess()) return AmountLookupResult.ok(result.getAmount());
        if (result.isUnsupported()) return AmountLookupResult.unsupported();
        return AmountLookupResult.error();
    }

    private static LookupKey lookupKeyOf(ItemStack stack, FluidStack fluidStack, String aspectTag) {
        if (aspectTag != null && !aspectTag.isEmpty()) return new EssentiaKey(aspectTag);
        if (fluidStack != null && fluidStack.getFluid() != null) return new FluidKey(fluidStack);
        if (stack != null && stack.getItem() != null) return new ItemKey(stack);
        return null;
    }

    private static long lookupCacheTtl(LookupCacheEntry entry, boolean degraded) {
        if (degraded) return DEGRADED_LOOKUP_CACHE_MS;
        if (entry.hits >= HOT_CACHE_HIT_THRESHOLD) return LOOKUP_CACHE_MS * HOT_CACHE_TTL_MULTIPLIER;
        return entry.amount <= 0L ? ZERO_LOOKUP_CACHE_MS : LOOKUP_CACHE_MS;
    }

    private static boolean isServerUnderLoad() {
        long now = System.currentTimeMillis();
        if (now < nextServerLoadCheckTime) return cachedServerUnderLoad;
        nextServerLoadCheckTime = now + SERVER_LOAD_CACHE_MS;
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.tickTimeArray.length == 0) {
            return cachedServerUnderLoad = false;
        }
        long total = 0L;
        for (long tickTime : server.tickTimeArray) total += tickTime;
        return cachedServerUnderLoad = total / server.tickTimeArray.length >= HIGH_LOAD_TICK_TIME_NS;
    }

    private static String playerKey(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        return uuid == null ? player.getCommandSenderName() : uuid.toString();
    }

    private static void cleanupCaches(long now) {
        if (now < nextCleanupTime) return;
        nextCleanupTime = now + CACHE_CLEANUP_INTERVAL_MS;
        PLAYER_LIMITS.entrySet()
            .removeIf(entry -> now - entry.getValue().lastRefillTime > CACHE_RETAIN_MS);
        CONTEXT_CACHE.entrySet()
            .removeIf(entry -> now - entry.getValue().expiresAt > CONTEXT_CACHE_RETAIN_MS);
    }

    private static void trimPlayerLimits() {
        while (PLAYER_LIMITS.size() > MAX_PLAYER_LIMITS) {
            String oldestKey = null;
            long oldestTime = Long.MAX_VALUE;
            for (Map.Entry<String, RateLimit> entry : PLAYER_LIMITS.entrySet()) {
                long lastRefillTime = entry.getValue().lastRefillTime;
                if (lastRefillTime < oldestTime) {
                    oldestTime = lastRefillTime;
                    oldestKey = entry.getKey();
                }
            }
            if (oldestKey == null) {
                return;
            }
            PLAYER_LIMITS.remove(oldestKey);
        }
    }

    @Desugar
    public record BatchLookupEntry(ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {

    }

    public static final class ContextResult {

        private final PlayerAeContext context;
        private final int status;
        private final int retryAfterMs;

        private ContextResult(PlayerAeContext context, int status, int retryAfterMs) {
            this.context = context;
            this.status = status;
            this.retryAfterMs = retryAfterMs;
        }

        static ContextResult ok(PlayerAeContext context) {
            return new ContextResult(context, Ae2Status.OK, 0);
        }

        static ContextResult noSystem() {
            return new ContextResult(null, Ae2Status.NO_SYSTEM, CONTEXT_RETRY_MS);
        }

        static ContextResult outOfRange() {
            return new ContextResult(null, Ae2Status.OUT_OF_RANGE, CONTEXT_RETRY_MS);
        }

        public boolean isAvailable() {
            return this.context != null && this.status == Ae2Status.OK;
        }

        public PlayerAeContext getContext() {
            return this.context;
        }

        public int getStatus() {
            return this.status;
        }

        public int getRetryAfterMs() {
            return this.retryAfterMs;
        }
    }

    public static final class PlayerAeContext {

        private final ITerminalHost host;
        private final IGrid grid;
        private final Object cacheOwner;

        private PlayerAeContext(ITerminalHost host, IGrid grid) {
            this.host = host;
            this.grid = grid;
            this.cacheOwner = grid == null ? host : grid;
        }
    }

    public static final class AmountLookupResult {

        private final int status;
        private final long amount;
        private final int retryAfterMs;

        private AmountLookupResult(int status, long amount, int retryAfterMs) {
            this.status = status;
            this.amount = amount;
            this.retryAfterMs = retryAfterMs;
        }

        static AmountLookupResult ok(long amount) {
            return new AmountLookupResult(Ae2Status.OK, amount, 0);
        }

        static AmountLookupResult noSystem() {
            return new AmountLookupResult(Ae2Status.NO_SYSTEM, 0L, CONTEXT_RETRY_MS);
        }

        static AmountLookupResult throttled() {
            return new AmountLookupResult(Ae2Status.THROTTLED, 0L, THROTTLE_RETRY_MS);
        }

        static AmountLookupResult unsupported() {
            return new AmountLookupResult(Ae2Status.UNSUPPORTED, 0L, 5000);
        }

        static AmountLookupResult error() {
            return new AmountLookupResult(Ae2Status.ERROR, 0L, 5000);
        }

        public int getStatus() {
            return this.status;
        }

        public long getAmount() {
            return this.amount;
        }

        public int getRetryAfterMs() {
            return this.retryAfterMs;
        }
    }

    private interface LookupKey {
    }

    private static final class ItemKey implements LookupKey {

        private final String itemName;
        private final int damage;
        private final NBTTagCompound tag;

        private ItemKey(ItemStack stack) {
            this.itemName = String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
            this.damage = stack.getItemDamage();
            this.tag = copyTag(stack.getTagCompound());
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof ItemKey other)) return false;
            return this.damage == other.damage && this.itemName.equals(other.itemName)
                && Objects.equals(this.tag, other.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.itemName, this.damage, this.tag);
        }
    }

    private static final class FluidKey implements LookupKey {

        private final String fluidName;
        private final NBTTagCompound tag;

        private FluidKey(FluidStack stack) {
            this.fluidName = stack.getFluid()
                .getName();
            this.tag = copyTag(stack.tag);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof FluidKey other)) return false;
            return this.fluidName.equals(other.fluidName) && Objects.equals(this.tag, other.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fluidName, this.tag);
        }
    }

    @Desugar
    private record EssentiaKey(String aspectTag) implements LookupKey {

        @Override
        public boolean equals(Object object) {
            return object instanceof EssentiaKey && this.aspectTag.equals(((EssentiaKey) object).aspectTag);
        }

    }

    private static NBTTagCompound copyTag(NBTTagCompound tag) {
        return tag == null ? null : (NBTTagCompound) tag.copy();
    }

    private static final class LookupCacheEntry {

        private final long amount;
        private final long createdAt;
        private int hits;

        private LookupCacheEntry(long amount, long createdAt) {
            this.amount = amount;
            this.createdAt = createdAt;
        }
    }

    private static final class BoundedLookupCache extends LinkedHashMap<LookupKey, LookupCacheEntry> {

        private BoundedLookupCache() {
            super(64, 0.75F, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<LookupKey, LookupCacheEntry> eldest) {
            return size() > MAX_LOOKUPS_PER_CONTEXT;
        }
    }

    @Desugar
    private record ContextCacheEntry(ContextResult result, int dimension, int signature, int blockX, int blockY,
        int blockZ, long expiresAt) {

    }

    private record BaublesAccessor(Method method) {

            private static final BaublesAccessor UNAVAILABLE = new BaublesAccessor(null);

        private boolean available() {
                return this.method != null;
            }

            private static BaublesAccessor resolve(String className, String methodName) {
                try {
                    Class<?> provider = Class.forName(className);
                    return new BaublesAccessor(provider.getMethod(methodName, EntityPlayer.class));
                } catch (ClassNotFoundException ignored) {
                    return UNAVAILABLE;
                } catch (ReflectiveOperationException | LinkageError e) {
                    IntegrationDiagnostics.logCapabilityFailureOnce(className + '#' + methodName, e);
                    return UNAVAILABLE;
                }
            }
        }

    private static final class RateLimit {

        private final int perSecond;
        private final int burst;
        private double tokens;
        private long lastRefillTime;

        private RateLimit(int perSecond, int burst, long now) {
            this.perSecond = perSecond;
            this.burst = burst;
            this.tokens = burst;
            this.lastRefillTime = now;
        }

        private boolean isThrottled(long now, int count) {
            if (count <= 0) {
                return false;
            }
            refill(now);
            if (this.tokens < count) {
                return true;
            }
            this.tokens -= count;
            return false;
        }

        private void refill(long now) {
            long elapsed = now - this.lastRefillTime;
            if (elapsed > 0L) {
                this.tokens = Math.min(this.burst, this.tokens + elapsed * this.perSecond / MILLIS_PER_SECOND);
                this.lastRefillTime = now;
            }
        }
    }

    @Desugar
    private record StorageGridTerminalHost(IStorageGrid storageGrid, IConfigManager configManager, IGrid grid)
        implements ITerminalHost {

        @Override
        public IMEMonitor<IAEItemStack> getItemInventory() {
            return this.storageGrid.getItemInventory();
        }

        @Override
        public IMEMonitor<IAEFluidStack> getFluidInventory() {
            return this.storageGrid.getFluidInventory();
        }

        @Override
        public IConfigManager getConfigManager() {
            return this.configManager;
        }
    }
}
