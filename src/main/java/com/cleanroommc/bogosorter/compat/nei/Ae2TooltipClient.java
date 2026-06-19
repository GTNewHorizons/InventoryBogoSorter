package com.cleanroommc.bogosorter.compat.nei;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.bogosorter.client.ae2.Ae2ClientBridge;
import com.cleanroommc.bogosorter.common.ReadableNumberConverter;
import com.cleanroommc.bogosorter.common.config.ae2.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.ae2.Ae2Status;
import com.cleanroommc.bogosorter.common.network.ae2.CAe2AmountBatchRequest;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.compat.ThaumicEnergisticsHelper;
import com.cleanroommc.bogosorter.compat.ae2.Ae2TerminalGuiDetector;
import com.github.bsideup.jabel.Desugar;

import codechicken.nei.PositionedStack;
import codechicken.nei.Widget;
import codechicken.nei.WidgetContainer;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.NEIRecipeWidget;
import codechicken.nei.recipe.StackInfo;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class Ae2TooltipClient {

    private static final long CACHE_TTL_MS = 3000L;
    private static final long MISS_TTL_MS = 1500L;
    private static final long NO_SYSTEM_TTL_MS = 5000L;
    private static final long FLUID_CONTAINER_TTL_MS = 60000L;
    private static final long BATCH_FLUSH_MS = 100L;
    private static final long REQUEST_TIMEOUT_MS = 15000L;
    private static final long CLIENT_CACHE_CLEANUP_MS = 30000L;
    private static final int CLIENT_REQUESTS_PER_SECOND = 8;
    private static final int CLIENT_REQUEST_BURST = 16;
    private static final int MAX_BATCH_SIZE = 32;
    private static final int MAX_CLIENT_CACHE_ENTRIES = 4096;
    private static final int MAX_FLUID_CACHE_ENTRIES = 1024;
    private static final int KIND_ITEM = 0;
    private static final int KIND_FLUID = 1;
    private static final int KIND_ESSENTIA = 2;
    private static final int HOT_CACHE_HIT_THRESHOLD = 10;
    private static final long HOT_CACHE_TTL_MULTIPLIER = 2L;
    private static final double MILLIS_PER_SECOND = 1000.0D;
    private static final double REQUEST_TOKEN_COST = 1.0D;
    private static final int SINGLE_STACK_SIZE = 1;
    private static final int SINGLE_FLUID_AMOUNT = 1;
    private static final Map<CacheKey, Entry> CACHE = new BoundedEntryCache();
    private static final Map<CacheKey, FluidEntry> FLUID_CONTAINER_CACHE = new BoundedFluidCache();
    private static final Map<Integer, CacheKey> REQUEST_KEYS = new LinkedHashMap<>();
    private static final Map<CacheKey, PendingRequest> BATCH_QUEUE = new LinkedHashMap<>();
    private static int nextRequestId = 1;
    private static boolean initialized;
    private static boolean serverAmountTooltipsAllowed = true;
    private static boolean serverThaumicAllowed = true;
    private static double requestTokens = CLIENT_REQUEST_BURST;
    private static long lastRequestRefillTime;
    private static long nextBatchFlushTime;
    private static long nextClientCacheCleanupTime;
    private static long nextContextRetryAt;
    private static int ae2ContextStatus = Ae2Status.NO_SYSTEM;

    private Ae2TooltipClient() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        Ae2ClientBridge.register(new ClientBridgeHandler());
        GuiContainerManager.addTooltipHandler(new RecipeTooltipHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new Ae2GuiWatcher());
    }

    public static void appendAmountTooltip(ItemStack stack, List<String> tooltip, boolean allowOpenTerminal) {
        if (!isAmountTooltipEnabled()) {
            return;
        }
        if (stack == null || stack.getItem() == null || !Mods.Ae2.isLoaded()) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        long now = Minecraft.getSystemTime();
        if (!allowOpenTerminal && ae2ContextStatus == Ae2Status.OUT_OF_RANGE && now < nextContextRetryAt) {
            addOutOfRangeLine(tooltip);
            return;
        }

        FluidStack fluidStack = fluidOf(stack, now);
        String essentiaAspectTag = fluidStack == null && isThaumicTooltipEnabled()
            ? ThaumicEnergisticsHelper.getAspectTag(stack)
            : null;
        int amountKind = fluidStack != null ? KIND_FLUID : essentiaAspectTag != null ? KIND_ESSENTIA : KIND_ITEM;
        CacheKey key = amountKind == KIND_FLUID ? new FluidKey(fluidStack)
            : amountKind == KIND_ESSENTIA ? new EssentiaKey(essentiaAspectTag) : new ItemKey(stack);
        Entry entry = CACHE.get(key);
        if (entry == null) {
            entry = new Entry();
            CACHE.put(key, entry);
        }
        entry.amountKind = amountKind;
        entry.lastAccess = now;
        entry.hits++;
        cleanupClientCaches(now);

        if (entry.hasResponse && now - entry.responseTime <= ttlFor(entry)) {
            addResponseLine(tooltip, entry);
            return;
        }

        if ((!entry.pending || now - entry.requestTime > REQUEST_TIMEOUT_MS) && now >= entry.nextRetryAt) {
            requestAmount(key, stack, fluidStack, essentiaAspectTag, amountKind, entry, now);
        }

        if (entry.hasResponse) {
            addResponseLine(tooltip, entry);
        } else {
            addCheckingLine(tooltip);
        }
    }

    private static void handleAmountResponse(int requestId, int status, long amount, int retryAfterMs) {
        if (!isAmountTooltipEnabled()) {
            return;
        }
        CacheKey key = REQUEST_KEYS.remove(requestId);
        if (key == null) {
            return;
        }

        Entry entry = CACHE.get(key);
        if (entry == null) {
            entry = new Entry();
            CACHE.put(key, entry);
        }
        entry.amountKind = key.amountKind();
        entry.pending = false;
        entry.requestTime = Minecraft.getSystemTime();
        entry.nextRetryAt = entry.requestTime + Math.max(retryAfterMs, status == Ae2Status.OK ? 0 : 500);
        if (status == Ae2Status.THROTTLED) {
            return;
        }
        if (status == Ae2Status.OUT_OF_RANGE) {
            entry.hasResponse = true;
            entry.status = status;
            entry.amount = 0L;
            entry.responseTime = entry.requestTime;
            ae2ContextStatus = status;
            return;
        }
        if (status == Ae2Status.NO_SYSTEM || status == Ae2Status.UNSUPPORTED) {
            entry.hasResponse = true;
            entry.status = status;
            entry.amount = 0L;
            entry.responseTime = entry.requestTime;
            return;
        }
        if (status == Ae2Status.ERROR && entry.hasResponse) {
            entry.responseTime = entry.requestTime;
            return;
        }
        if (status == Ae2Status.ERROR) {
            return;
        }

        entry.hasResponse = true;
        entry.status = status;
        entry.amount = amount;
        entry.responseTime = entry.requestTime;
    }

    private static void setAe2ContextStatus(int status) {
        ae2ContextStatus = isAmountTooltipEnabled() ? status : Ae2Status.NO_SYSTEM;
    }

    private static void resetAe2State() {
        ae2ContextStatus = Ae2Status.NO_SYSTEM;
        nextContextRetryAt = 0L;
        clearRequestState();
    }

    private static void clearRequestState() {
        CACHE.clear();
        REQUEST_KEYS.clear();
        BATCH_QUEUE.clear();
    }

    private static void requestAmount(CacheKey key, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag,
        int amountKind, Entry entry, long now) {
        if (!isAmountTooltipEnabled()) {
            return;
        }
        if (!tryConsumeRequestToken(now)) {
            entry.nextRetryAt = now + 250L;
            return;
        }

        // request id cleanup so stale keys dont stick around
        removeRequestIdsFor(key);
        int requestId = nextRequestId++;
        if (nextRequestId <= 0) {
            nextRequestId = 1;
        }

        // fixes essentia aspect tag
        ItemStack requestStack = null;
        FluidStack requestFluidStack = null;
        if (fluidStack != null) {
            requestFluidStack = fluidStack.copy();
            requestFluidStack.amount = SINGLE_FLUID_AMOUNT;
        } else if (essentiaAspectTag == null) {
            requestStack = stack.copy();
            requestStack.stackSize = SINGLE_STACK_SIZE;
        }

        REQUEST_KEYS.put(requestId, key);
        entry.pending = true;
        entry.amountKind = amountKind;
        entry.requestTime = now;
        entry.nextRetryAt = now + REQUEST_TIMEOUT_MS;

        BATCH_QUEUE.put(key, new PendingRequest(requestId, requestStack, requestFluidStack, essentiaAspectTag));
    }

    private static void flushPendingRequests(long now) {
        if (!isAmountTooltipEnabled()) {
            resetAe2State();
            return;
        }
        if (BATCH_QUEUE.isEmpty() || now < nextBatchFlushTime) {
            return;
        }

        nextBatchFlushTime = now + BATCH_FLUSH_MS;
        List<CAe2AmountBatchRequest.Entry> entries = new ArrayList<>(Math.min(BATCH_QUEUE.size(), MAX_BATCH_SIZE));
        Iterator<Map.Entry<CacheKey, PendingRequest>> iterator = BATCH_QUEUE.entrySet()
            .iterator();
        while (iterator.hasNext() && entries.size() < MAX_BATCH_SIZE) {
            PendingRequest request = iterator.next()
                .getValue();
            entries.add(
                new CAe2AmountBatchRequest.Entry(
                    request.requestId,
                    request.stack,
                    request.fluidStack,
                    request.essentiaAspectTag));
            iterator.remove();
        }

        if (!entries.isEmpty()) {
            NetworkHandler.sendToServer(new CAe2AmountBatchRequest(entries));
        }
    }

    private static void cleanupClientCaches(long now) {
        if (now < nextClientCacheCleanupTime) {
            return;
        }
        nextClientCacheCleanupTime = now + CLIENT_CACHE_CLEANUP_MS;

        CACHE.entrySet()
            .removeIf(
                entry -> !entry.getValue().pending && now - entry.getValue().lastAccess > CLIENT_CACHE_CLEANUP_MS);
        FLUID_CONTAINER_CACHE.entrySet()
            .removeIf(entry -> now - entry.getValue().createdAt > FLUID_CONTAINER_TTL_MS);

        REQUEST_KEYS.entrySet()
            .removeIf(requestEntry -> {
                int requestId = requestEntry.getKey();
                CacheKey key = requestEntry.getValue();
                Entry entry = CACHE.get(key);
                PendingRequest pending = BATCH_QUEUE.get(key);
                if (entry == null) {
                    BATCH_QUEUE.remove(key);
                    return true;
                }
                if (pending != null && pending.requestId == requestId) {
                    if (now - entry.requestTime > REQUEST_TIMEOUT_MS) {
                        BATCH_QUEUE.remove(key);
                        entry.pending = false;
                        return true;
                    }
                    return false;
                }
                return true;
            });
    }

    private static void removeRequestIdsFor(CacheKey key) {
        REQUEST_KEYS.entrySet()
            .removeIf(entry -> key.equals(entry.getValue()));
    }

    private static boolean tryConsumeRequestToken(long now) {
        if (lastRequestRefillTime <= 0L) {
            lastRequestRefillTime = now;
        }

        long elapsed = now - lastRequestRefillTime;
        if (elapsed > 0L) {
            requestTokens = Math
                .min(CLIENT_REQUEST_BURST, requestTokens + elapsed * CLIENT_REQUESTS_PER_SECOND / MILLIS_PER_SECOND);
            lastRequestRefillTime = now;
        }

        if (requestTokens < REQUEST_TOKEN_COST) {
            return false;
        }

        requestTokens -= REQUEST_TOKEN_COST;
        return true;
    }

    private static void addAmountLine(List<String> tooltip, long amount, int amountKind) {
        tooltip.add("");
        tooltip.add(
            EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "bogosorter.tooltip.amount_in_system",
                EnumChatFormatting.AQUA + ReadableNumberConverter.INSTANCE.toWideReadableForm(amount)
                    + suffixFor(amountKind)));
    }

    private static void addResponseLine(List<String> tooltip, Entry entry) {
        if (entry.status == Ae2Status.OUT_OF_RANGE) {
            addOutOfRangeLine(tooltip);
            return;
        }
        if (entry.status == Ae2Status.NO_SYSTEM || entry.status == Ae2Status.UNSUPPORTED
            || entry.status == Ae2Status.ERROR) {
            return;
        }
        addAmountLine(tooltip, entry.amount, entry.amountKind);
    }

    private static void addCheckingLine(List<String> tooltip) {
        tooltip.add("");
        tooltip
            .add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("bogosorter.tooltip.amount_checking"));
    }

    private static void addOutOfRangeLine(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("bogosorter.tooltip.amount_out_of_range"));
    }

    private static long ttlFor(Entry entry) {
        if (entry.status == Ae2Status.NO_SYSTEM || entry.status == Ae2Status.OUT_OF_RANGE
            || entry.status == Ae2Status.UNSUPPORTED) {
            return NO_SYSTEM_TTL_MS;
        }
        if (entry.amount <= 0L || entry.status == Ae2Status.ERROR) {
            return MISS_TTL_MS;
        }
        if (entry.hits >= HOT_CACHE_HIT_THRESHOLD) {
            return CACHE_TTL_MS * HOT_CACHE_TTL_MULTIPLIER;
        }
        return CACHE_TTL_MS;
    }

    private static String suffixFor(int amountKind) {
        if (amountKind == KIND_FLUID) {
            return " mB";
        }
        return "";
    }

    private static FluidStack fluidOf(ItemStack stack, long now) {
        CacheKey containerKey = new ItemKey(stack);
        FluidEntry cached = FLUID_CONTAINER_CACHE.get(containerKey);
        if (cached != null && now - cached.createdAt <= FLUID_CONTAINER_TTL_MS) {
            return cached.fluidStack == null ? null : cached.fluidStack.copy();
        }

        FluidEntry entry = new FluidEntry();
        entry.createdAt = now;
        try {
            FluidStack fluidStack = StackInfo.isFluidContainer(stack) ? null : StackInfo.getFluid(stack);
            entry.fluidStack = fluidStack == null ? null : fluidStack.copy();
        } catch (RuntimeException | LinkageError ignored) {}

        FLUID_CONTAINER_CACHE.put(containerKey, entry);
        return entry.fluidStack == null ? null : entry.fluidStack.copy();
    }

    private static boolean isAe2TerminalGui(Object gui) {
        return Ae2TerminalGuiDetector.isSearchableTerminal(gui);
    }

    private static boolean isAe2TerminalContextGui(GuiContainer gui) {
        return Ae2TerminalGuiDetector.resolveSearchTarget(gui) != null;
    }

    private static boolean isAmountTooltipEnabled() {
        return TooltipFeatureConfig.isAmountTooltipEnabled() && serverAmountTooltipsAllowed;
    }

    private static boolean isThaumicTooltipEnabled() {
        return TooltipFeatureConfig.isThaumicEnabled() && serverThaumicAllowed;
    }

    private static final class ClientBridgeHandler implements Ae2ClientBridge.Handler {

        @Override
        public void handleBatchResponse(int contextStatus, List<Ae2ClientBridge.Response> responses) {
            setAe2ContextStatus(contextStatus);
            int contextRetryAfterMs = 0;
            for (Ae2ClientBridge.Response response : responses) {
                contextRetryAfterMs = Math.max(contextRetryAfterMs, response.retryAfterMs());
                handleAmountResponse(
                    response.requestId(),
                    response.status(),
                    response.amount(),
                    response.retryAfterMs());
            }
            nextContextRetryAt = Minecraft.getSystemTime() + contextRetryAfterMs;
        }

        @Override
        public void setServerFeatures(boolean amountTooltipsAllowed, boolean thaumicAllowed) {
            serverAmountTooltipsAllowed = amountTooltipsAllowed;
            serverThaumicAllowed = thaumicAllowed;
            if (!amountTooltipsAllowed) {
                resetAe2State();
            }
        }

        @Override
        public void reset() {
            resetAe2State();
        }
    }

    public static final class Ae2GuiWatcher {

        private Object lastGui;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            long now = Minecraft.getSystemTime();
            flushPendingRequests(now);
            if (!isAmountTooltipEnabled()) {
                this.lastGui = null;
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            Object gui = mc.currentScreen;
            if (!isAe2TerminalGui(gui)) {
                this.lastGui = null;
                return;
            }

            if (gui == this.lastGui) {
                return;
            }

            this.lastGui = gui;
            CACHE.clear();
            REQUEST_KEYS.clear();
        }
    }

    private interface CacheKey {

        int amountKind();
    }

    private static final class ItemKey implements CacheKey {

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

        @Override
        public int amountKind() {
            return KIND_ITEM;
        }
    }

    private static final class FluidKey implements CacheKey {

        private final String fluidName;
        private final NBTTagCompound tag;

        private FluidKey(FluidStack fluidStack) {
            this.fluidName = fluidStack.getFluid() == null ? "null"
                : fluidStack.getFluid()
                    .getName();
            this.tag = copyTag(fluidStack.tag);
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

        @Override
        public int amountKind() {
            return KIND_FLUID;
        }
    }

    @Desugar
    private record EssentiaKey(String aspectTag) implements CacheKey {

        @Override
        public boolean equals(Object object) {
            return object instanceof EssentiaKey && this.aspectTag.equals(((EssentiaKey) object).aspectTag);
        }

        @Override
        public int amountKind() {
            return KIND_ESSENTIA;
        }
    }

    private static NBTTagCompound copyTag(NBTTagCompound tag) {
        return tag == null ? null : (NBTTagCompound) tag.copy();
    }

    private static final class BoundedEntryCache extends LinkedHashMap<CacheKey, Ae2TooltipClient.Entry> {

        private BoundedEntryCache() {
            super(64, 0.75F, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<CacheKey, Ae2TooltipClient.Entry> eldest) {
            return size() > MAX_CLIENT_CACHE_ENTRIES;
        }
    }

    private static final class BoundedFluidCache extends LinkedHashMap<CacheKey, FluidEntry> {

        private BoundedFluidCache() {
            super(64, 0.75F, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<CacheKey, FluidEntry> eldest) {
            return size() > MAX_FLUID_CACHE_ENTRIES;
        }
    }

    private static final class RecipeTooltipHandler implements IContainerTooltipHandler {

        @Override
        public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
            if (currenttip.isEmpty()) {
                return currenttip;
            }

            appendForHoveredRecipeStack(gui, mousex, mousey, currenttip);
            return currenttip;
        }

        @Override
        public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey,
            List<String> currenttip) {
            if (gui instanceof GuiRecipe) {
                if (!appendForHoveredRecipeStack(gui, mousex, mousey, currenttip)) {
                    appendAmountTooltip(itemstack, currenttip, isAe2TerminalContextGui(gui));
                }
            } else if (!isAe2TerminalStorageHover(gui)) {
                appendAmountTooltip(itemstack, currenttip, isAe2TerminalContextGui(gui));
            }
            return currenttip;
        }

        private static boolean isAe2TerminalStorageHover(GuiContainer gui) {
            return isAe2TerminalGui(gui) && !isThaumicArcaneCraftingTerminal(gui)
                && getAe2VirtualSlotUnderMouse(gui) != null;
        }

        private static Object getAe2VirtualSlotUnderMouse(GuiContainer gui) {
            try {
                return invokeMethod(gui);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private static boolean isThaumicArcaneCraftingTerminal(GuiContainer gui) {
            Class<?> current = gui.getClass();
            while (current != null) {
                if ("thaumicenergistics.client.gui.GuiArcaneCraftingTerminal".equals(current.getName())) {
                    return true;
                }
                current = current.getSuperclass();
            }
            return false;
        }

        private static boolean appendForHoveredRecipeStack(GuiContainer gui, int mousex, int mousey,
            List<String> currenttip) {
            if (!(gui instanceof GuiRecipe)) {
                return false;
            }

            NEIRecipeWidget recipeWidget = getRecipeWidget((GuiRecipe<?>) gui, mousex, mousey);
            if (recipeWidget == null) {
                return false;
            }

            PositionedStack hovered = recipeWidget.getPositionedStackMouseOver(mousex, mousey);
            if (hovered == null || hovered.item == null) {
                return false;
            }

            appendAmountTooltip(hovered.item, currenttip, isAe2TerminalContextGui(gui));
            return true;
        }

        private static NEIRecipeWidget getRecipeWidget(GuiRecipe<?> gui, int mousex, int mousey) {
            try {
                Field containerField = findField(gui.getClass());
                if (containerField == null) {
                    return null;
                }

                containerField.setAccessible(true);
                Object container = containerField.get(gui);
                if (!(container instanceof WidgetContainer)) {
                    return null;
                }

                Widget widget = ((WidgetContainer) container).getWidgetUnderMouse(mousex, mousey);
                return widget instanceof NEIRecipeWidget ? (NEIRecipeWidget) widget : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private static Field findField(Class<?> type) {
            Class<?> current = type;
            while (current != null) {
                try {
                    return current.getDeclaredField("container");
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }

            return null;
        }

        private static Object invokeMethod(Object instance) throws ReflectiveOperationException {
            Class<?> current = instance.getClass();
            while (current != null) {
                try {
                    java.lang.reflect.Method method = current.getDeclaredMethod("getVirtualMESlotUnderMouse");
                    method.setAccessible(true);
                    return method.invoke(instance);
                } catch (NoSuchMethodException ignored) {
                    current = current.getSuperclass();
                }
            }

            throw new NoSuchMethodException("getVirtualMESlotUnderMouse");
        }
    }

    private static final class Entry {

        private boolean pending;
        private boolean hasResponse;
        private int status = Ae2Status.OK;
        private int amountKind;
        private long amount;
        private long requestTime;
        private long responseTime;
        private long lastAccess;
        private long nextRetryAt;
        private int hits;
    }

    @Desugar
    private record PendingRequest(int requestId, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {

    }

    private static final class FluidEntry {

        private FluidStack fluidStack;
        private long createdAt;
    }
}
