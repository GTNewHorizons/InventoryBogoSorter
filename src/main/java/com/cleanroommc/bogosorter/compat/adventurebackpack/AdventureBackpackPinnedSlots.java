package com.cleanroommc.bogosorter.compat.adventurebackpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.darkona.adventurebackpack.block.TileAdventureBackpack;
import com.darkona.adventurebackpack.common.Constants;
import com.darkona.adventurebackpack.inventory.ContainerBackpack;
import com.darkona.adventurebackpack.inventory.IInventoryBackpack;
import com.darkona.adventurebackpack.inventory.InventoryBackpack;
import com.darkona.adventurebackpack.playerProperties.BackpackProperty;
import com.darkona.adventurebackpack.util.Wearing;

public final class AdventureBackpackPinnedSlots {

    private static final String MOD_TAG = "bogosorter";
    private static final String PINS_TAG = "pinnedSlots";
    private static final int[] EMPTY_MASK = new int[0];

    private AdventureBackpackPinnedSlots() {}

    public static int getSlotIndex(Container container, SlotAccessor slot) {
        if (slot == null || !(container instanceof ContainerBackpack backpack)) return -1;
        IInventoryBackpack inventory = backpack.getInventoryBackpack();
        if (!isSupported(inventory) || slot.getInventory() != inventory) return -1;
        int index = slot.callGetSlotIndex();
        return index >= 0 && index < Constants.INVENTORY_MAIN_SIZE ? index : -1;
    }

    public static int[] getMask(EntityPlayer player, Container container) {
        IInventoryBackpack owner = resolveOwner(player, container);
        return owner == null ? EMPTY_MASK : toPacketMask(readMask(owner));
    }

    public static boolean isPinnable(EntityPlayer player, Container container, SlotAccessor slot) {
        return getSlotIndex(container, slot) >= 0
            && (player.worldObj.isRemote || resolveOwner(player, container) != null);
    }

    public static boolean isPinned(int[] mask, Container container, SlotAccessor slot) {
        int index = getSlotIndex(container, slot);
        return index >= 0 && (index >>> 5) < mask.length && (mask[index >>> 5] & 1 << (index & 31)) != 0;
    }

    public static int[] toggle(EntityPlayer player, Container container, SlotAccessor slot) {
        IInventoryBackpack owner = resolveOwner(player, container);
        int index = getSlotIndex(container, slot);
        if (owner == null || index < 0) return EMPTY_MASK;

        long mask = readMask(owner) ^ 1L << index;
        NBTTagCompound extended = owner.getExtendedProperties();
        NBTTagCompound bogoTag = extended.getCompoundTag(MOD_TAG);
        bogoTag.setLong(PINS_TAG, mask);
        extended.setTag(MOD_TAG, bogoTag);
        if (owner instanceof TileAdventureBackpack tile) {
            tile.markDirty();
        } else {
            owner.dirtyExtended();
        }

        ItemStack parent = owner.getParentItem();
        if (parent == null) {
            return toPacketMask(mask);
        } else if (parent == Wearing.getWearingBackpack(player)) {
            BackpackProperty.sync(player);
        } else {
            player.inventory.markDirty();
        }
        return toPacketMask(mask);
    }

    private static IInventoryBackpack resolveOwner(EntityPlayer player, Container container) {
        if (!(container instanceof ContainerBackpack backpack)) return null;
        IInventoryBackpack inventory = backpack.getInventoryBackpack();
        if (inventory instanceof TileAdventureBackpack) return inventory;
        if (!(inventory instanceof InventoryBackpack)) return null;
        ItemStack parent = inventory.getParentItem();
        return parent != null
            && (parent == Wearing.getHoldingBackpack(player) || parent == Wearing.getWearingBackpack(player))
                ? inventory
                : null;
    }

    private static boolean isSupported(IInventoryBackpack backpack) {
        return backpack instanceof InventoryBackpack || backpack instanceof TileAdventureBackpack;
    }

    private static long readMask(IInventoryBackpack backpack) {
        return backpack.getExtendedProperties()
            .getCompoundTag(MOD_TAG)
            .getLong(PINS_TAG);
    }

    private static int[] toPacketMask(long mask) {
        return new int[] { (int) mask, (int) (mask >>> 32) };
    }
}
