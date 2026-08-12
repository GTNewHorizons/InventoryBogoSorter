package com.cleanroommc.bogosorter.compat.backpack;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

import de.eydamos.backpack.inventory.container.Boundaries;
import de.eydamos.backpack.inventory.container.ContainerAdvanced;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.saves.BackpackSave;
import de.eydamos.backpack.saves.PlayerSave;
import de.eydamos.backpack.util.BackpackUtil;

public final class BackpackPinnedSlots {

    private static final String MOD_TAG = "bogosorter";
    private static final String PINS_TAG = "pinnedSlots";
    private static final int[] EMPTY_MASK = new int[0];

    private BackpackPinnedSlots() {}

    public static boolean isContainer(Container container) {
        return container instanceof ContainerAdvanced;
    }

    public static int getSlotIndex(Container container, SlotAccessor slot) {
        if (slot == null || !(container instanceof ContainerAdvanced backpack)) return -1;
        int start = backpack.getBoundary(Boundaries.BACKPACK);
        int end = backpack.getBoundary(Boundaries.BACKPACK_END);
        int index = slot.callGetSlotIndex();
        return start >= 0 && end > start
            && slot.getInventory() == backpack.getInventoryToSave()
            && index >= 0
            && index < end - start ? index : -1;
    }

    public static int[] getMask(EntityPlayer player, Container container) {
        Owner owner = resolveOwner(player, container);
        return owner == null ? EMPTY_MASK : readMask(owner.stack, owner.size);
    }

    public static boolean isPinnable(EntityPlayer player, Container container, SlotAccessor slot) {
        return getSlotIndex(container, slot) >= 0
            && (player.worldObj.isRemote || resolveOwner(player, container) != null);
    }

    public static int[] toggle(EntityPlayer player, Container container, SlotAccessor slot) {
        Owner owner = resolveOwner(player, container);
        int index = getSlotIndex(container, slot);
        if (owner == null || index < 0) return EMPTY_MASK;

        int words = (owner.size + 31) >>> 5;
        int[] storedMask = readStoredMask(owner.stack);
        int[] mask = Arrays.copyOf(storedMask, Math.max(storedMask.length, words));
        mask[index >>> 5] ^= 1 << (index & 31);
        NBTTagCompound itemTag = owner.stack.getTagCompound();
        if (itemTag == null) {
            itemTag = new NBTTagCompound();
            owner.stack.setTagCompound(itemTag);
        }
        NBTTagCompound bogoTag = itemTag.getCompoundTag(MOD_TAG);
        bogoTag.setIntArray(PINS_TAG, mask);
        itemTag.setTag(MOD_TAG, bogoTag);
        if (owner.playerSave == null) {
            player.inventory.markDirty();
        } else {
            owner.playerSave.setPersonalBackpack(owner.stack);
        }
        return Arrays.copyOf(mask, words);
    }

    private static Owner resolveOwner(EntityPlayer player, Container container) {
        if (!(container instanceof ContainerAdvanced backpack)) return null;
        int start = backpack.getBoundary(Boundaries.BACKPACK);
        int end = backpack.getBoundary(Boundaries.BACKPACK_END);
        if (start < 0 || end <= start || end > container.inventorySlots.size()) return null;

        BackpackSave save = backpack.getBackpackSave();
        if (save == null) return null;

        Owner owner = resolveOwner(player.getCurrentEquippedItem(), save, end - start, null);
        if (owner != null) return owner;

        PlayerSave playerSave = new PlayerSave(player);
        return resolveOwner(playerSave.getPersonalBackpack(), save, end - start, playerSave);
    }

    private static Owner resolveOwner(ItemStack stack, BackpackSave save, int size, PlayerSave playerSave) {
        if (stack == null || !(stack.getItem() instanceof ItemBackpackBase)
            || BackpackUtil.isEnderBackpack(stack)
            || BackpackUtil.getType(stack) != save.getType()
            || !BackpackUtil.UUIDEquals(stack, save.getUUID())) return null;
        return new Owner(stack, size, playerSave);
    }

    private static int[] readMask(ItemStack stack, int slots) {
        int words = (slots + 31) >>> 5;
        // Limit the returned array to the current backpack size; the stored array remains intact if it grows again.
        return Arrays.copyOf(readStoredMask(stack), words);
    }

    private static int[] readStoredMask(ItemStack stack) {
        NBTTagCompound itemTag = stack.getTagCompound();
        return itemTag == null ? EMPTY_MASK
            : itemTag.getCompoundTag(MOD_TAG)
                .getIntArray(PINS_TAG);
    }

    private static final class Owner {

        private final ItemStack stack;
        private final int size;
        private final PlayerSave playerSave;

        private Owner(ItemStack stack, int size, PlayerSave playerSave) {
            this.stack = stack;
            this.size = size;
            this.playerSave = playerSave;
        }
    }
}
