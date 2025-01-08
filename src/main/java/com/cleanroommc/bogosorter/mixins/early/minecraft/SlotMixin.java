package com.cleanroommc.bogosorter.mixins.early.minecraft;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.api.ISlot;

@Mixin(Slot.class)
public abstract class SlotMixin implements ISlot {

    @Shadow
    public int xDisplayPosition;

    @Shadow
    public int yDisplayPosition;

    @Shadow
    public int slotNumber;

    @Shadow
    @Final
    public IInventory inventory;

    @Override
    public Slot bogo$getRealSlot() {
        return (Slot) (Object) this;
    }

    @Override
    public int bogo$getX() {
        return xDisplayPosition;
    }

    @Override
    public int bogo$getY() {
        return yDisplayPosition;
    }

    @Override
    public int bogo$getSlotNumber() {
        return slotNumber;
    }

    @Override
    public int bogo$getSlotIndex() {
        return bogo$this().getSlotIndex();
    }

    @Override
    public IInventory bogo$getInventory() {
        return inventory;
    }

    @Override
    public void bogo$putStack(ItemStack itemStack) {
        bogo$this().putStack(itemStack);
    }

    @Override
    public ItemStack bogo$getStack() {
        return bogo$this().getStack();
    }

    @Override
    public int bogo$getMaxStackSize(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    @Override
    public int bogo$getItemStackLimit(ItemStack itemStack) {
        return bogo$this().getSlotStackLimit();
    }

    @Override
    public boolean bogo$isEnabled() {
        return bogo$this().func_111238_b();
    }

    @Override
    public boolean bogo$isItemValid(ItemStack stack) {
        return bogo$this().isItemValid(stack);
    }

    @Override
    public boolean bogo$canTakeStack(EntityPlayer player) {
        return bogo$this().canTakeStack(player);
    }

    @Override
    public void bogo$onSlotChanged() {
        bogo$this().onSlotChanged();
    }

    @Override
    public void bogo$onSlotChanged(ItemStack oldItem, ItemStack newItem) {
        bogo$this().onSlotChange(newItem, oldItem);
    }

    @Override
    public void bogo$onTake(EntityPlayer player, ItemStack itemStack) {
        bogo$this().onPickupFromSlot(player, itemStack);
    }

    public Slot bogo$this() {
        return (Slot) (Object) this;
    }
}
