package com.cleanroommc.bogosorter.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.compat.backpack.BackpackPinnedSlots;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

public final class PinnedSlots {

    public static final int FIRST_PLAYER_SLOT = 9;
    public static final int LAST_PLAYER_SLOT = 35;

    private static final String MOD_TAG = "bogosorter";
    private static final String PINS_TAG = "pinnedSlots";

    private PinnedSlots() {}

    public static boolean isPinnable(SlotAccessor slot) {
        return BogoSortAPI.isPlayerSlot(slot) && bitForIndex(slot.callGetSlotIndex()) != 0;
    }

    public static boolean isPinnable(EntityPlayer player, Container container, SlotAccessor slot) {
        return isPinnable(slot)
            || (Mods.Backpack.isLoaded() && BackpackPinnedSlots.isPinnable(player, container, slot));
    }

    public static boolean isPinned(EntityPlayer player, SlotAccessor slot) {
        return isPinnable(slot) && isPinned(player, slot.callGetSlotIndex());
    }

    public static boolean isPinned(EntityPlayer player, Container container, SlotAccessor slot, int[] backpackMask) {
        return isPinned(player, slot)
            || (Mods.Backpack.isLoaded() && BackpackPinnedSlots.isPinned(backpackMask, container, slot));
    }

    public static boolean isBackpackPinned(int[] mask, Container container, SlotAccessor slot) {
        return Mods.Backpack.isLoaded() && BackpackPinnedSlots.isPinned(mask, container, slot);
    }

    public static int[] getBackpackMask(EntityPlayer player, Container container) {
        return Mods.Backpack.isLoaded() ? BackpackPinnedSlots.getMask(player, container) : new int[0];
    }

    public static int[] toggleBackpack(EntityPlayer player, Container container, SlotAccessor slot) {
        return Mods.Backpack.isLoaded() ? BackpackPinnedSlots.toggle(player, container, slot) : new int[0];
    }

    public static boolean isPinned(EntityPlayer player, int inventoryIndex) {
        int bit = bitForIndex(inventoryIndex);
        return bit != 0 && (getMask(player) & bit) != 0;
    }

    public static int getMask(EntityPlayer player) {
        NBTTagCompound persisted = player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        return persisted.getCompoundTag(MOD_TAG)
            .getInteger(PINS_TAG);
    }

    public static int toggle(EntityPlayer player, int inventoryIndex) {
        int bit = bitForIndex(inventoryIndex);
        if (bit == 0) return getMask(player);

        NBTTagCompound entityData = player.getEntityData();
        NBTTagCompound persisted = entityData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        NBTTagCompound bogoData = persisted.getCompoundTag(MOD_TAG);
        int mask = bogoData.getInteger(PINS_TAG) ^ bit;
        bogoData.setInteger(PINS_TAG, mask);
        persisted.setTag(MOD_TAG, bogoData);
        entityData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        return mask;
    }

    static int bitForIndex(int inventoryIndex) {
        return inventoryIndex >= FIRST_PLAYER_SLOT && inventoryIndex <= LAST_PLAYER_SLOT
            ? 1 << inventoryIndex - FIRST_PLAYER_SLOT
            : 0;
    }
}
