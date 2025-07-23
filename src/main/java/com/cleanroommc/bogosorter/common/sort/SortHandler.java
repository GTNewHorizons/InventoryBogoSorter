package com.cleanroommc.bogosorter.common.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.McUtils;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.SortRulesConfig;
import com.cleanroommc.bogosorter.common.network.CSlotSync;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class SortHandler {

    public static final Map<EntityPlayer, List<SortRule<ItemStack>>> cacheItemSortRules = new Object2ObjectOpenHashMap<>();
    public static final Map<EntityPlayer, List<NbtSortRule>> cacheNbtSortRules = new Object2ObjectOpenHashMap<>();
    public static final AtomicReference<List<NbtSortRule>> currentNbtSortRules = new AtomicReference<>(
        Collections.emptyList());

    @Nullable
    private static ResourceLocation sortSound = new ResourceLocation(BogoSorterConfig.sortSound);
    private static ResourceLocation FallbacksortSound = new ResourceLocation("gui.button.press");
    private static List<ResourceLocation> foolsSounds = null;
    private static int foolsSortCounter = 0;

    @SideOnly(Side.CLIENT)
    public static void playSortSound() {
        ResourceLocation sound = sortSound;
        Minecraft mc = Minecraft.getMinecraft();
        SoundHandler soundHandler = mc.getSoundHandler();
        if (BogoSorter.isAprilFools()) {
            foolsSortCounter++;
            if (foolsSortCounter >= BogoSorter.RND.nextInt(100)) {
                if (foolsSounds == null) {
                    foolsSounds = getResourceLocations(soundHandler);
                }
                foolsSortCounter = 0;
                sound = foolsSounds.get(BogoSorter.RND.nextInt(foolsSounds.size()));
            }
        }

        // Fallback check in case something went wrong
        if (!soundHandler.sndRegistry.containsKey(sound)) {
            sound = FallbacksortSound;
        }

        if (sound != null) {
            soundHandler.playSound(PositionedSoundRecord.func_147674_a(sound, 1f));
        }
    }

    private static @NotNull List<ResourceLocation> getResourceLocations(SoundHandler soundHandler) {
        List<ResourceLocation> sounds = new ArrayList<>(256);
        for (Object key : soundHandler.sndRegistry.getKeys()) {
            if (key instanceof ResourceLocation soundEvent) {
                if (!soundEvent.getResourcePath()
                    .contains("music.")
                    && !soundEvent.getResourcePath()
                        .contains("records.")) {
                    sounds.add(soundEvent);
                }
            }
        }
        return sounds;
    }

    private final EntityPlayer player;
    private final Container container;
    private final GuiSortingContext context;
    private final Comparator<ItemSortContainer> containerComparator;
    private final Int2ObjectMap<ClientSortData> clientSortData;
    private final List<SortRule<ItemStack>> itemSortRules;

    public SortHandler(EntityPlayer entityPlayer, Container container, Int2ObjectMap<ClientSortData> clientSortData) {
        this(entityPlayer, container, GuiSortingContext.getOrCreate(container), clientSortData);
    }

    public SortHandler(EntityPlayer player, Container container, GuiSortingContext sortingContext,
        Int2ObjectMap<ClientSortData> clientSortData) {
        this.player = player;
        this.container = container;
        this.context = sortingContext;
        this.itemSortRules = cacheItemSortRules.getOrDefault(player, Collections.emptyList());
        this.containerComparator = (container1, container2) -> {
            int result;
            for (SortRule<ItemStack> sortRule : itemSortRules) {
                result = sortRule instanceof ClientItemSortRule
                    ? ((ClientItemSortRule) sortRule).compareServer(container1, container2)
                    : sortRule.compare(container1.getItemStack(), container2.getItemStack());
                if (result != 0) return result;
            }
            result = ItemCompareHelper.compareRegistryOrder(container1.getItemStack(), container2.getItemStack());
            if (result != 0) return result;
            result = ItemCompareHelper.compareMeta(container1.getItemStack(), container2.getItemStack());
            return result;
        };
        this.clientSortData = clientSortData;
    }

    public void sort(int slotId) {
        sort(slotId, true);
    }

    public void sort(int slotId, boolean sync) {
        SlotGroup slotGroup = context.getSlotGroup(slotId);
        sort(slotGroup, sync);
    }

    public void sort(SlotGroup slotGroup, boolean sync) {
        if (slotGroup != null) {
            if (BogoSorter.isAprilFools() && BogoSorter.RND.nextFloat() < 0.01f) {
                sortBogo(slotGroup);
                this.player.addChatMessage(new ChatComponentText("Get Bogo'd!"));
            } else {
                sortHorizontal(slotGroup);
            }
            if (sync) {
                container.detectAndSendChanges();
            }
        }
    }

    public void sortHorizontal(SlotGroup slotGroup) {
        LinkedList<ItemSortContainer> itemList = gatherItems(slotGroup);
        if (itemList.isEmpty()) return;

        currentNbtSortRules.set(cacheNbtSortRules.getOrDefault(player, Collections.emptyList()));
        itemList.sort(containerComparator);
        currentNbtSortRules.set(Collections.emptyList());

        ItemSortContainer itemSortContainer = itemList.pollFirst();
        if (itemSortContainer == null) return;
        for (SlotAccessor slot : getSortableSlots(slotGroup)) {
            if (itemSortContainer == null) {
                slot.callPutStack(null);
                continue;
            }

            ItemStack stack = itemSortContainer.getItemStack();
            int max = Math.min(slot.callGetSlotStackLimit(), stack.getMaxStackSize());
            if (max <= 0) continue;

            if (preventSplit(stack)) {
                slot.callPutStack(stack);
                itemSortContainer = itemList.pollFirst();
                continue;
            }

            slot.callPutStack(itemSortContainer.makeStack(max));

            if (!itemSortContainer.canMakeStack()) {
                itemSortContainer = itemList.pollFirst();
            }
        }

        // Remaining items that cannot fit are dropped to the player.
        if (itemSortContainer != null) {
            itemList.addFirst(itemSortContainer);
        }

        if (!itemList.isEmpty()) {
            McUtils.giveItemsToPlayer(this.player, prepareDropList(itemList));
        }

    }

    // TODO untested
    /*
     * public void sortVertical(SlotGroup slotGroup) {
     * LinkedList<ItemSortContainer> itemList = gatherItems(slotGroup);
     * if (itemList.isEmpty()) return;
     * currentNbtSortRules.set(cacheNbtSortRules.getOrDefault(player, Collections.emptyList()));
     * itemList.sort(containerComparator);
     * currentNbtSortRules.set(Collections.emptyList());
     * ItemSortContainer itemSortContainer = itemList.pollFirst();
     * if (itemSortContainer == null) return;
     * main:
     * for (int c = 0; c < slotGroup[0].length; c++) {
     * for (Slot[] slots : slotGroup) {
     * if (c >= slots.length) break main;
     * Slot slot = slots[c];
     * if (itemSortContainer == null) {
     * slot.putStack(ItemStack.EMPTY);
     * continue;
     * }
     * if (!itemSortContainer.canMakeStack()) {
     * itemSortContainer = itemList.pollFirst();
     * if (itemSortContainer == null) continue;
     * }
     * int max = slot.getItemStackLimit(itemSortContainer.getItemStack());
     * if (max <= 0) continue;
     * slot.putStack(itemSortContainer.makeStack(max));
     * }
     * }
     * if (!itemList.isEmpty()) {
     * McUtils.giveItemsToPlayer(this.player, prepareDropList(itemList));
     * }
     * }
     */

    public void sortBogo(SlotGroup slotGroup) {
        List<ItemStack> items = new ArrayList<>();
        for (SlotAccessor slot : getSortableSlots(slotGroup)) {
            ItemStack stack = slot.callGetStack();
            items.add(stack);
        }
        Collections.shuffle(items);
        List<SlotAccessor> slots = getSortableSlots(slotGroup);
        for (int i = 0; i < slots.size(); i++) {
            SlotAccessor slot = slots.get(i);
            slot.callPutStack(items.get(i));
        }
    }

    public LinkedList<ItemSortContainer> gatherItems(SlotGroup slotGroup) {
        LinkedList<ItemSortContainer> list = new LinkedList<>();
        Object2ObjectOpenCustomHashMap<ItemStack, ItemSortContainer> items = new Object2ObjectOpenCustomHashMap<>(
            BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (SlotAccessor slot : getSortableSlots(slotGroup)) {
            ItemStack stack = slot.callGetStack();
            if (stack != null) {
                ItemSortContainer container = new ItemSortContainer(stack, clientSortData.get(slot.getSlotNumber()));
                if (preventSplit(stack)) {
                    list.add(container);
                } else {
                    ItemSortContainer container1 = items.get(stack);
                    if (container1 == null) {
                        container1 = container;
                        items.put(stack, container1);
                        list.add(container1);
                    } else {
                        container1.grow(stack.stackSize);
                    }
                }
            }
        }
        return list;
    }

    private static List<ItemStack> prepareDropList(List<ItemSortContainer> sortedList) {
        List<ItemStack> dropList = new ArrayList<>();
        for (ItemSortContainer itemSortContainer : sortedList) {
            while (itemSortContainer.canMakeStack()) {
                dropList.add(
                    itemSortContainer.makeStack(
                        itemSortContainer.getItemStack()
                            .getMaxStackSize()));
            }
        }
        return dropList;
    }

    @SideOnly(Side.CLIENT)
    public static Comparator<ItemStack> getClientItemComparator() {
        return (stack1, stack2) -> {
            int result;
            for (SortRule<ItemStack> sortRule : SortRulesConfig.sortRules) {
                result = sortRule.compare(stack1, stack2);
                if (result != 0) return result;
            }
            result = ItemCompareHelper.compareRegistryOrder(stack1, stack2);
            if (result != 0) return result;
            result = ItemCompareHelper.compareMeta(stack1, stack2);
            return result;
        };
    }

    public void clearAllItems(SlotAccessor slot1) {
        SlotGroup slotGroup = context.getSlotGroup(slot1.getSlotNumber());
        if (slotGroup != null) {
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>();
            for (SlotAccessor slot : getSortableSlots(slotGroup)) {
                if (slot.callGetStack() != null) {
                    slot.callPutStack(null);
                    slots.add(Pair.of(null, slot.getSlotNumber()));
                }
            }
            NetworkHandler.sendToServer(new CSlotSync(slots));
        }
    }

    public void randomizeItems(SlotAccessor slot1) {
        SlotGroup slotGroup = context.getSlotGroup(slot1.getSlotNumber());
        if (slotGroup != null) {
            List<Pair<ItemStack, Integer>> slots = new ArrayList<>();
            Random random = new Random();
            for (SlotAccessor slot : getSortableSlots(slotGroup)) {
                if (random.nextFloat() < 0.3f) {
                    ItemStack randomItem = ClientEventHandler.allItems
                        .get(random.nextInt(ClientEventHandler.allItems.size()))
                        .copy();
                    slot.callPutStack(randomItem.copy());
                    slots.add(Pair.of(randomItem, slot.getSlotNumber()));
                }

            }
            NetworkHandler.sendToServer(new CSlotSync(slots));
        }
    }

    public List<SlotAccessor> getSortableSlots(SlotGroup slotGroup) {
        List<SlotAccessor> result = new ArrayList<>();

        for (SlotAccessor slot : slotGroup.getSlots()) {
            /*
             * Logic being used to check if we cannot access the slot:
             * 1. Can the player take the stack?
             * This usually returns true, but some slot implementations return false if the slot is empty.
             * 2. Can we insert the current stack into the slot?
             * This may seem roundabout, but this means that if it returns false, then most likely, the slot is
             * always returning false.
             * 3. Is the stack in the slot empty?
             * If it is empty, some implementations return false for both above methods.
             * Although this might risk changing actually inaccessible slots, most likely, those slots would not be
             * empty.
             * The slot should only be marked as inaccessible if all three conditions return false.
             */
            boolean canTake = slot.callCanTakeStack(player);
            boolean canInsert = (slot.callGetStack() != null) && slot.callIsItemValid(
                slot.callGetStack()
                    .copy());
            boolean isEmpty = slot.callGetStack() == null;
            if (canTake || canInsert || isEmpty) result.add(slot);
        }
        return result;

    }

    // Prevents splitting of non-stackable items (e.g., tools, armor) with stack size > 1
    // to avoid filling the inventory unnecessarily.
    private static boolean preventSplit(ItemStack stack) {
        if (!BogoSorterConfig.preventSplit) return false;
        return stack.getMaxStackSize() == 1;
    }
}
