package com.cleanroommc.bogosorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.ICustomInsertable;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.sort.ClientItemSortRule;
import com.cleanroommc.bogosorter.common.sort.ItemSortContainer;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.PlayerInvWrapper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.utils.item.SlotItemHandler;

import appeng.container.slot.AppEngSlot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BogoSortAPI implements IBogoSortAPI {

    public static final BogoSortAPI INSTANCE = new BogoSortAPI();
    public static final SortRule<ItemStack> EMPTY_ITEM_SORT_RULE = new SortRule<ItemStack>("empty", (o1, o2) -> 0) {

        @Override
        public boolean isEmpty() {
            return true;
        }
    };
    public static final NbtSortRule EMPTY_NBT_SORT_RULE = new NbtSortRule("empty", null, (o1, o2) -> 0) {

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static final Function<Slot, SlotAccessor> DEFAULT_SLOT_GETTER = slot -> (SlotAccessor) slot;

    private static final ICustomInsertable DEFAULT_INSERTABLE = (container, slots, stack, emptyOnly) -> ShortcutHandler
        .insertToSlots(slots, stack, emptyOnly);

    private BogoSortAPI() {}

    private final Map<Class<?>, BiConsumer<Container, ISortingContextBuilder>> COMPAT_MAP = new Object2ObjectOpenHashMap<>();
    private final Map<Class<?>, IPosSetter> playerButtonPos = new Object2ObjectOpenHashMap<>();
    private final Map<Class<?>, Function<Slot, SlotAccessor>> slotGetterMap = new Object2ObjectOpenHashMap<>();
    private final Map<Class<?>, ICustomInsertable> customInsertableMap = new Object2ObjectOpenHashMap<>();
    private final Map<String, SortRule<ItemStack>> itemSortRules = new Object2ObjectOpenHashMap<>();
    private final Map<String, NbtSortRule> nbtSortRules = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<SortRule<ItemStack>> itemSortRules2 = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<NbtSortRule> nbtSortRules2 = new Int2ObjectOpenHashMap<>();
    // lists for fast iteration
    private final List<SortRule<ItemStack>> itemSortRuleList = new ArrayList<>();
    private final List<NbtSortRule> nbtSortRuleList = new ArrayList<>();
    // a map of all rules that changed through versions for json parsing
    private final Map<String, String> remappedSortRules = new Object2ObjectOpenHashMap<>();

    public void remapSortRule(String old, String newName) {
        this.remappedSortRules.put(old, newName);
    }

    @Override
    public <T extends Slot> void addSlotGetter(Class<T> clazz, Function<T, SlotAccessor> function) {
        this.slotGetterMap.put(clazz, (Function<Slot, SlotAccessor>) function);
    }

    @Override
    public void addCustomInsertable(Class<? extends Container> clazz, ICustomInsertable insertable) {
        this.customInsertableMap.put(clazz, insertable);
    }

    @Override
    public <T extends Container> void addCompat(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder) {
        COMPAT_MAP.put(clazz, (BiConsumer<Container, ISortingContextBuilder>) builder);
    }

    @Override
    public <T> void addCompatSimple(Class<T> clazz, BiConsumer<T, ISortingContextBuilder> builder) {
        if (!Container.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class must be an instance of Container!");
        }
        COMPAT_MAP.put(clazz, (BiConsumer<Container, ISortingContextBuilder>) builder);
    }

    @Override
    public void addPlayerSortButtonPosition(Class<?> clazz, @Nullable IPosSetter buttonPos) {
        if (!Container.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class must be a subclass of Container!");
        }
        this.playerButtonPos.put(clazz, buttonPos);
    }

    @Override
    public <T extends Container> void removeCompat(Class<T> clazz) {
        COMPAT_MAP.remove(clazz);
    }

    private static void validateKey(String key) {
        if (!key.matches("[A-Za-z_]+")) {
            throw new IllegalArgumentException("Key must only have letters and underscores!");
        }
    }

    @Override
    public void registerItemSortingRule(String key, Comparator<ItemStack> itemComparator) {
        validateKey(key);
        SortRule<ItemStack> sortRule = new SortRule<>(key, itemComparator);
        itemSortRules.put(key, sortRule);
        itemSortRuleList.add(sortRule);
        itemSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @ApiStatus.Internal
    public void registerClientItemSortingRule(String key, Comparator<ItemStack> comparator,
        Comparator<ItemSortContainer> serverComparator) {
        validateKey(key);
        ClientItemSortRule sortRule = new ClientItemSortRule(key, comparator, serverComparator);
        itemSortRules.put(key, sortRule);
        itemSortRuleList.add(sortRule);
        itemSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @Override
    public void registerNbtSortingRule(String key, String tagPath, Comparator<NBTBase> comparator) {
        validateKey(key);
        NbtSortRule sortRule = new NbtSortRule(key, tagPath, comparator);
        nbtSortRules.put(key, sortRule);
        nbtSortRuleList.add(sortRule);
        nbtSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @Override
    public void registerNbtSortingRule(String key, String tagPath, int expectedType) {
        validateKey(key);
        NbtSortRule sortRule = new NbtSortRule(key, tagPath, expectedType);
        nbtSortRules.put(key, sortRule);
        nbtSortRuleList.add(sortRule);
        nbtSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    @Override
    public <T> void registerNbtSortingRule(String key, String tagPath, int expectedType, Comparator<T> comparator,
        Function<NBTBase, T> converter) {
        validateKey(key);
        NbtSortRule sortRule = new NbtSortRule(key, tagPath, expectedType, comparator, converter);
        nbtSortRules.put(key, sortRule);
        nbtSortRuleList.add(sortRule);
        nbtSortRules2.put(sortRule.getSyncId(), sortRule);
    }

    public <T extends Container> BiConsumer<T, ISortingContextBuilder> getBuilder(Container container) {
        BiConsumer<Container, ISortingContextBuilder> builder = COMPAT_MAP.get(container.getClass());
        return builder == null ? null : (BiConsumer<T, ISortingContextBuilder>) builder;
    }

    public IPosSetter getPlayerButtonPos(Container container) {
        if (container instanceof ISortableContainer) {
            return ((ISortableContainer) container).getPlayerButtonPosSetter();
        }
        return this.playerButtonPos.getOrDefault(container.getClass(), IPosSetter.TOP_RIGHT_HORIZONTAL);
    }

    @Unmodifiable
    public List<NbtSortRule> getNbtSortRuleList() {
        return Collections.unmodifiableList(nbtSortRuleList);
    }

    @Unmodifiable
    public List<SortRule<ItemStack>> getItemSortRuleList() {
        return Collections.unmodifiableList(itemSortRuleList);
    }

    public SortRule<ItemStack> getItemSortRule(String key) {
        SortRule<ItemStack> sortRule = this.itemSortRules.get(key);
        if (sortRule == null && this.remappedSortRules.containsKey(key)) {
            sortRule = this.itemSortRules.get(this.remappedSortRules.get(key));
        }
        return sortRule == null ? EMPTY_ITEM_SORT_RULE : sortRule;
    }

    public SortRule<ItemStack> getItemSortRule(int syncId) {
        return itemSortRules2.get(syncId);
    }

    public NbtSortRule getNbtSortRule(int syncId) {
        return nbtSortRules2.get(syncId);
    }

    public NbtSortRule getNbtSortRule(String key) {
        NbtSortRule sortRule = this.nbtSortRules.get(key);
        if (sortRule == null && this.remappedSortRules.containsKey(key)) {
            sortRule = this.nbtSortRules.get(this.remappedSortRules.get(key));
        }
        return sortRule == null ? EMPTY_NBT_SORT_RULE : sortRule;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openConfigGui(GuiScreen old) {
        ClientGUI.open(new ConfigGui(old));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean sortSlotGroup(Slot slot) {
        return ClientEventHandler.sort(Minecraft.getMinecraft().currentScreen, getSlot(slot));
    }

    @NotNull
    @Override
    public SlotAccessor getSlot(@NotNull Slot slot) {
        return this.slotGetterMap.getOrDefault(slot.getClass(), DEFAULT_SLOT_GETTER)
            .apply(slot);
    }

    @Override
    public List<SlotAccessor> getSlots(@NotNull List<Slot> slots) {
        List<SlotAccessor> slotAccessors = new ArrayList<>();
        for (Slot slot : slots) slotAccessors.add(getSlot(slot));
        return slotAccessors;
    }

    public static SlotAccessor getSlot(@NotNull Container container, int index) {
        return INSTANCE.getSlot(container.getSlot(index));
    }

    @NotNull
    public ICustomInsertable getInsertable(@NotNull Container container, boolean player) {
        return player ? DEFAULT_INSERTABLE
            : this.customInsertableMap.getOrDefault(container.getClass(), DEFAULT_INSERTABLE);
    }

    public static ItemStack insert(Container container, List<SlotAccessor> slots, ItemStack stack) {
        if (slots.isEmpty()) return stack;
        ICustomInsertable insertable = INSTANCE.getInsertable(container, isPlayerSlot(slots.get(0)));
        if (stack.isStackable()) {
            stack = insertable.insert(container, slots, stack, false);
        }
        if (stack != null) {
            stack = insertable.insert(container, slots, stack, true);
        }
        return stack;
    }

    public static ItemStack insert(Container container, List<SlotAccessor> slots, ItemStack stack, boolean emptyOnly) {
        if (slots.isEmpty()) return stack;
        return INSTANCE.getInsertable(container, isPlayerSlot(slots.get(0)))
            .insert(container, slots, stack, emptyOnly);
    }

    public static boolean isValidSortable(Container container) {
        return container instanceof ISortableContainer || INSTANCE.COMPAT_MAP.containsKey(container.getClass());
    }

    public static boolean isPlayerSlot(Slot slot) {
        return isPlayerSlot((SlotAccessor) slot);
    }

    public static boolean isPlayerSlot(SlotAccessor slot) {
        if (slot == null) return false;
        if (slot.getInventory() instanceof InventoryPlayer
            || (slot instanceof SlotItemHandler handler && isPlayerInventory(handler.getItemHandler()))
            || (Mods.Ae2.isLoaded() && slot instanceof AppEngSlot AppEng && isPlayerInventory(AppEng.inventory))) {
            return slot.callGetSlotIndex() >= 0 && slot.callGetSlotIndex() < 36;
        }
        return false;
    }

    public static boolean isPlayerInventory(IItemHandler itemHandler) {
        return itemHandler instanceof PlayerMainInvWrapper || itemHandler instanceof PlayerInvWrapper;
    }

    public static boolean isPlayerInventory(IInventory itemHandler) {
        return itemHandler instanceof InventoryPlayer;
    }

    public static final Hash.Strategy<ItemStack> ITEM_META_NBT_HASH_STRATEGY = new Hash.Strategy<ItemStack>() {

        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), o.getItemDamage(), o.getTagCompound());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return (a == null && b == null) || (a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
                && Objects.equals(a.getTagCompound(), b.getTagCompound()));
        }
    };

    public static final Hash.Strategy<ItemStack> ITEM_META_HASH_STRATEGY = new Hash.Strategy<ItemStack>() {

        @Override
        public int hashCode(ItemStack o) {
            return Objects.hash(o.getItem(), o.getItemDamage());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return (a == null && b == null) || (a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage());
        }
    };

}
