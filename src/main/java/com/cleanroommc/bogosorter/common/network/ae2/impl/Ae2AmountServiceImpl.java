package com.cleanroommc.bogosorter.common.network.ae2.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.cleanroommc.bogosorter.common.network.ae2.Ae2Status;
import com.cleanroommc.bogosorter.common.network.ae2.AmountLookupResult;
import com.cleanroommc.bogosorter.common.network.ae2.BatchLookupEntry;
import com.cleanroommc.bogosorter.common.network.ae2.ContextResult;
import com.cleanroommc.bogosorter.common.network.ae2.IAe2Service;
import com.cleanroommc.bogosorter.common.network.ae2.IntegrationDiagnostics;
import com.cleanroommc.bogosorter.common.network.ae2.PlayerAeContext;
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
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;

public final class Ae2AmountServiceImpl implements IAe2Service {

    private static final int NETWORK_LOOKUPS_PER_SECOND = 50;
    private static final int NETWORK_LOOKUP_BURST = 50;
    private static final int DEGRADED_NETWORK_LOOKUPS_PER_SECOND = 15;
    private static final int DEGRADED_NETWORK_LOOKUP_BURST = 15;
    private static final int MAX_LOOKUPS_PER_CONTEXT = 1024;
    private static final long LOOKUP_CACHE_MS = 3000L;
    private static final long ZERO_LOOKUP_CACHE_MS = 5000L;
    private static final long DEGRADED_LOOKUP_CACHE_MS = 10000L;
    private static final long CACHE_RETAIN_MS = 30000L;
    private static final long HIGH_LOAD_TICK_TIME_NS = 75000000L;
    private static final long SERVER_LOAD_CACHE_MS = 5000L;
    private static final int HOT_CACHE_HIT_THRESHOLD = 10;
    private static final long HOT_CACHE_TTL_MULTIPLIER = 2L;
    private static final double MILLIS_PER_SECOND = 1000.0D;
    private static final double WIRELESS_POWER_CHECK_AMOUNT = 1.0D;
    private static final String WIRELESS_ACCESS_POINT_CLASS = "appeng.tile.networking.TileWireless";
    private static final String AE2FC_BASE_CONTAINER_CLASS = "com.glodblock.github.client.gui.container.base.FCBaseContainer";

    private static final Map<Object, RateLimit> NETWORK_LIMITS = new ConcurrentHashMap<>();
    private static final Map<Object, BoundedLookupCache> LOOKUP_CACHES = Collections
        .synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, BoundedLookupCache> eldest) {
                return size() > 2048;
            }
        });
    private static final Map<String, BaublesAccessor> BAUBLES_ACCESSORS = new ConcurrentHashMap<>();
    private static volatile long nextServerLoadCheckTime;
    private static volatile boolean cachedServerUnderLoad;

    private static final boolean AE2_PRESENT;

    static {
        boolean ok = false;
        try {
            Class.forName("appeng.api.storage.ITerminalHost");
            ok = true;
        } catch (Throwable ignored) {}
        AE2_PRESENT = ok;
    }

    private static final Ae2AmountServiceImpl INSTANCE = new Ae2AmountServiceImpl();

    public static Ae2AmountServiceImpl instance() {
        return INSTANCE;
    }

    private Ae2AmountServiceImpl() {}

    @Override
    public ContextResult resolveContext(EntityPlayerMP player, long now) {
        if (player == null) {
            return ContextResult.noSystem();
        }

        ContextResult openTerminal = getOpenTerminalContext(player);
        if (openTerminal.isAvailable()) {
            IntegrationDiagnostics.recordContextResolution(false);
            return openTerminal;
        }

        ContextResult result = getWirelessTerminalContext(player);
        IntegrationDiagnostics.recordContextResolution(false);
        return result;
    }

    @Override
    public AmountLookupResult lookupAmount(PlayerAeContext context, ItemStack stack, FluidStack fluidStack,
        String essentiaAspectTag, long now) {
        if (context == null || context.host() == null) {
            return AmountLookupResult.noSystem();
        }

        LookupKey lookupKey = lookupKeyOf(stack, fluidStack, essentiaAspectTag);
        if (lookupKey == null) {
            return AmountLookupResult.error();
        }

        boolean degraded = isServerUnderLoad(now);
        Object cacheOwner = context.cacheOwner();
        BoundedLookupCache cache;
        synchronized (LOOKUP_CACHES) {
            cache = LOOKUP_CACHES.get(cacheOwner);
            if (cache == null) {
                cache = new BoundedLookupCache();
                LOOKUP_CACHES.put(cacheOwner, cache);
            }
        }
        LookupCacheEntry cached;
        synchronized (cache) {
            cached = cache.get(lookupKey);
            if (cached != null && now - cached.createdAt <= lookupCacheTtl(cached, degraded)) {
                cached.hits.incrementAndGet();
                IntegrationDiagnostics.recordLookupCacheHit();
                return AmountLookupResult.ok(cached.amount);
            }

            if (degraded && cached != null && now - cached.createdAt <= CACHE_RETAIN_MS) {
                cached.hits.incrementAndGet();
                IntegrationDiagnostics.recordLookupCacheHit();
                return AmountLookupResult.ok(cached.amount);
            }
        }

        int perSecond = degraded ? DEGRADED_NETWORK_LOOKUPS_PER_SECOND : NETWORK_LOOKUPS_PER_SECOND;
        int burst = degraded ? DEGRADED_NETWORK_LOOKUP_BURST : NETWORK_LOOKUP_BURST;
        RateLimit networkLimit = NETWORK_LIMITS
            .computeIfAbsent(cacheOwner, ignored -> new RateLimit(perSecond, burst, now));
        if (networkLimit.isThrottled(now)) {
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
            Object gridObj = context.grid();
            IGrid grid = gridObj instanceof IGrid ig ? ig : null;
            result = getTerminalEssentiaAmount(grid, essentiaAspectTag);
        } else if (fluidStack != null) {
            result = AmountLookupResult.ok(getTerminalFluidAmount(context.host(), fluidStack));
        } else {
            result = AmountLookupResult.ok(getTerminalItemAmount(context.host(), stack));
        }

        if (result.getStatus() == Ae2Status.OK) {
            synchronized (cache) {
                cache.put(lookupKey, new LookupCacheEntry(result.getAmount(), now));
            }
        }
        return result;
    }

    @Override
    public List<AmountLookupResult> lookupAmountBatch(PlayerAeContext context, List<BatchLookupEntry> entries,
        long now) {
        if (context == null || entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LookupKey, AmountLookupResult> shared = new LinkedHashMap<>();
        List<AmountLookupResult> results = new ArrayList<>(entries.size());
        for (BatchLookupEntry entry : entries) {
            LookupKey key = lookupKeyOf(entry.stack(), entry.fluidStack(), entry.essentiaAspectTag());
            if (key == null) {
                results.add(AmountLookupResult.error());
                continue;
            }
            AmountLookupResult result = shared.get(key);
            if (result == null) {
                result = lookupAmount(context, entry.stack(), entry.fluidStack(), entry.essentiaAspectTag(), now);
                shared.put(key, result);
            }
            results.add(result);
        }
        return results;
    }

    @Override
    public int countDistinctLookupKeys(List<BatchLookupEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        Set<LookupKey> keys = new HashSet<>();
        for (BatchLookupEntry entry : entries) {
            LookupKey key = lookupKeyOf(entry.stack(), entry.fluidStack(), entry.essentiaAspectTag());
            if (key != null) {
                keys.add(key);
            }
        }
        return keys.size();
    }

    @Override
    public void clearCaches() {
        NETWORK_LIMITS.clear();
        synchronized (LOOKUP_CACHES) {
            LOOKUP_CACHES.clear();
        }
    }

    private ContextResult getOpenTerminalContext(EntityPlayerMP player) {
        if (!AE2_PRESENT) return ContextResult.noSystem();
        Container container = player.openContainer;
        if (container instanceof ContainerMEMonitorable monitorable) {
            if (!container.canInteractWith(player)) {
                return ContextResult.noSystem();
            }
            Object target = monitorable.getTarget();
            if (!Ae2Access.isTerminalHost(target)) {
                return ContextResult.noSystem();
            }
            IGridNode node = monitorable.getNetworkNode();
            IGrid grid = node == null ? getGrid(target) : node.getGrid();
            return ContextResult.ok(new PlayerAeContext(target, grid, Ae2Access.getConfigManager(target)));
        }

        if (container == null || !isAe2FcBaseContainer(container) || !container.canInteractWith(player)) {
            return ContextResult.noSystem();
        }
        try {
            Object host = invokeGetHost(container);
            if (!Ae2Access.isTerminalHost(host)) {
                return ContextResult.noSystem();
            }
            return ContextResult.ok(new PlayerAeContext(host, getGrid(host), Ae2Access.getConfigManager(host)));
        } catch (ReflectiveOperationException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("ae2fc-container-host", e);
            return ContextResult.noSystem();
        }
    }

    private ContextResult getWirelessTerminalContext(EntityPlayerMP player) {
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

    private ContextResult createWirelessTerminalContext(EntityPlayerMP player, ItemStack terminal) {
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
                    new StorageGridTerminalHost(storageGrid, grid, handler.getConfigManager(terminal)),
                    grid,
                    handler.getConfigManager(terminal)));
        } catch (RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("ae2-wireless-context", e);
            return ContextResult.noSystem();
        }
    }

    private static ContextResult preferredFailure(ContextResult current, ContextResult candidate) {
        if (current == null || candidate.getStatus() == Ae2Status.OUT_OF_RANGE) {
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

    private static IGrid getGrid(Object host) {
        if (host instanceof StorageGridTerminalHost sgh) {
            return sgh.grid();
        }
        if (!AE2_PRESENT) return null;
        return Ae2Access.getGridFromHost(host);
    }

    private static long getTerminalItemAmount(Object host, ItemStack stack) {
        if (!AE2_PRESENT) return 0L;
        return Ae2Access.getItemAmount(host, stack);
    }

    private static long getTerminalFluidAmount(Object host, FluidStack fluidStack) {
        if (!AE2_PRESENT) return 0L;
        return Ae2Access.getFluidAmount(host, fluidStack);
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
        if (entry.hits.get() >= HOT_CACHE_HIT_THRESHOLD) return LOOKUP_CACHE_MS * HOT_CACHE_TTL_MULTIPLIER;
        return entry.amount <= 0L ? ZERO_LOOKUP_CACHE_MS : LOOKUP_CACHE_MS;
    }

    private static boolean isServerUnderLoad(long now) {
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
        while (current != null && current != Object.class) {
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

    private static final class EssentiaKey implements LookupKey {

        private final String aspectTag;

        private EssentiaKey(String aspectTag) {
            this.aspectTag = aspectTag;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EssentiaKey)) return false;
            return aspectTag.equals(((EssentiaKey) o).aspectTag);
        }

        public int hashCode() {
            return aspectTag.hashCode();
        }
    }

    private static NBTTagCompound copyTag(NBTTagCompound tag) {
        return tag == null ? null : (NBTTagCompound) tag.copy();
    }

    private static final class LookupCacheEntry {

        private final long amount;
        private final long createdAt;
        private final AtomicInteger hits;

        private LookupCacheEntry(long amount, long createdAt) {
            this.amount = amount;
            this.createdAt = createdAt;
            this.hits = new AtomicInteger();
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

        private boolean isThrottled(long now) {
            refill(now);
            if (this.tokens < 1) {
                return true;
            }
            this.tokens -= 1;
            return false;
        }

        private void refill(long now) {
            if (now <= this.lastRefillTime) return;
            long elapsed = Math.min(now - this.lastRefillTime, 60000L);
            if (elapsed > 0L) {
                this.tokens = Math.min(this.burst, this.tokens + elapsed * this.perSecond / MILLIS_PER_SECOND);
                this.lastRefillTime = now;
            }
        }
    }

    private static final class Ae2Access {

        private Ae2Access() {}

        private static final Class<?> TERMINAL_HOST_CLASS;

        static {
            Class<?> c = null;
            try {
                c = Class
                    .forName("appeng.api.storage.ITerminalHost", false, Ae2AmountServiceImpl.class.getClassLoader());
            } catch (Throwable ignored) {}
            TERMINAL_HOST_CLASS = c;
        }

        static boolean isTerminalHost(Object host) {
            return TERMINAL_HOST_CLASS != null && TERMINAL_HOST_CLASS.isInstance(host);
        }

        static IGrid getGridFromHost(Object host) {
            if (host instanceof IGridHost gridHost) {
                IGridNode node = gridHost.getGridNode(ForgeDirection.UNKNOWN);
                return node == null ? null : node.getGrid();
            }
            return null;
        }

        static Object getConfigManager(Object host) {
            if (TERMINAL_HOST_CLASS == null || !TERMINAL_HOST_CLASS.isInstance(host)) return null;
            return ((appeng.api.storage.ITerminalHost) host).getConfigManager();
        }

        static long getItemAmount(Object host, ItemStack stack) {
            if (stack == null || stack.getItem() == null) return 0L;
            if (host instanceof StorageGridTerminalHost sgh) {
                IMEMonitor<IAEItemStack> inventory = sgh.getItemInventory();
                if (inventory == null || inventory.getStorageList() == null) return 0L;
                IAEItemStack requested = AEItemStack.create(stack);
                IAEItemStack found = inventory.getStorageList()
                    .findPrecise(requested);
                return found == null ? 0L : found.getStackSize();
            }
            if (TERMINAL_HOST_CLASS == null || !TERMINAL_HOST_CLASS.isInstance(host)) return 0L;
            appeng.api.storage.ITerminalHost terminalHost = (appeng.api.storage.ITerminalHost) host;
            IMEMonitor<IAEItemStack> inventory = terminalHost.getItemInventory();
            if (inventory == null || inventory.getStorageList() == null) return 0L;
            IAEItemStack requested = AEItemStack.create(stack);
            IAEItemStack found = inventory.getStorageList()
                .findPrecise(requested);
            return found == null ? 0L : found.getStackSize();
        }

        static long getFluidAmount(Object host, FluidStack fluidStack) {
            if (fluidStack == null || fluidStack.getFluid() == null) return 0L;
            if (host instanceof StorageGridTerminalHost sgh) {
                IMEMonitor<IAEFluidStack> inventory = sgh.getFluidInventory();
                if (inventory == null || inventory.getStorageList() == null) return 0L;
                IAEFluidStack requested = AEFluidStack.create(fluidStack);
                if (requested == null) return 0L;
                IAEFluidStack found = inventory.getStorageList()
                    .findPrecise(requested);
                return found == null ? 0L : found.getStackSize();
            }
            if (TERMINAL_HOST_CLASS == null || !TERMINAL_HOST_CLASS.isInstance(host)) return 0L;
            appeng.api.storage.ITerminalHost terminalHost = (appeng.api.storage.ITerminalHost) host;
            IMEMonitor<IAEFluidStack> inventory = terminalHost.getFluidInventory();
            if (inventory == null || inventory.getStorageList() == null) return 0L;
            IAEFluidStack requested = AEFluidStack.create(fluidStack);
            if (requested == null) return 0L;
            IAEFluidStack found = inventory.getStorageList()
                .findPrecise(requested);
            return found == null ? 0L : found.getStackSize();
        }
    }

    @Desugar
    private record StorageGridTerminalHost(IStorageGrid storageGrid, IGrid grid, IConfigManager configManager) {

        public IMEMonitor<IAEItemStack> getItemInventory() {
            return this.storageGrid.getItemInventory();
        }

        public IMEMonitor<IAEFluidStack> getFluidInventory() {
            return this.storageGrid.getFluidInventory();
        }

    }

    @Desugar
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
}
