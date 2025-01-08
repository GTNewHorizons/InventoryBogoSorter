package com.cleanroommc.bogosorter.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.api.ISlot;

public class SlotDelegate implements ISlot {

    private final Slot slot;
    private final Item item;

    public SlotDelegate(Slot slot, Item item) {
        this.slot = slot;
        this.item = item;
    }

    @Override
    public Slot bogo$getRealSlot() {
        return slot;
    }

    @Override
    public int bogo$getX() {
        return slot.xDisplayPosition;
    }

    @Override
    public int bogo$getY() {
        return slot.yDisplayPosition;
    }

    @Override
    public int bogo$getSlotNumber() {
        return slot.slotNumber;
    }

    @Override
    public int bogo$getSlotIndex() {
        return slot.getSlotIndex();
    }

    @Override
    public IInventory bogo$getInventory() {
        return slot.inventory;
    }

    @Override
    public void bogo$putStack(ItemStack itemStack) {
        slot.putStack(itemStack);
    }

    @Override
    public ItemStack bogo$getStack() {
        return slot.getStack();
    }

    @Override
    public int bogo$getMaxStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    @Override
    public int bogo$getItemStackLimit(ItemStack itemStack) {
        return item.getItemStackLimit(itemStack);
    }

    @Override
    public boolean bogo$isEnabled() {
        return slot.func_111238_b();
    }

    @Override
    public boolean bogo$isItemValid(ItemStack stack) {
        return slot.isItemValid(stack);
    }

    @Override
    public boolean bogo$canTakeStack(EntityPlayer player) {
        return slot.canTakeStack(player);
    }

    @Override
    public void bogo$onSlotChanged() {
        slot.onSlotChanged();
    }

    @Override
    public void bogo$onSlotChanged(ItemStack oldItem, ItemStack newItem) {
        slot.onSlotChange(newItem, oldItem);
    }

    @Override
    public void bogo$onTake(EntityPlayer player, ItemStack itemStack) {
        slot.onPickupFromSlot(player, itemStack);
    }
}
