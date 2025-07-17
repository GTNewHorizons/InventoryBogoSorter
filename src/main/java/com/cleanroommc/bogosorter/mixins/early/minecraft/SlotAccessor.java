package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Slot.class)
public interface SlotAccessor {

    @Accessor(value = "xDisplayPosition")
    int bogo$getX();

    @Accessor(value = "yDisplayPosition")
    int bogo$getY();

    @Accessor(value = "slotNumber")
    int bogo$getSlotNumber();

    @Invoker(value = "getSlotIndex", remap = false)
    int bogo$getSlotIndex();

    @Accessor(value = "inventory")
    IInventory bogo$getInventory();

    @Invoker(value = "putStack")
    void bogo$putStack(ItemStack itemStack);

    @Invoker(value = "getStack")
    ItemStack bogo$getStack();

    @Invoker(value = "getSlotStackLimit")
    int bogo$getItemStackLimit();

    @Invoker(value = "func_111238_b")
    boolean bogo$isEnabled();

    @Invoker(value = "isItemValid")
    boolean bogo$isItemValid(ItemStack stack);

    @Invoker(value = "canTakeStack")
    boolean bogo$canTakeStack(EntityPlayer player);

    @Invoker(value = "onSlotChanged")
    void bogo$onSlotChanged();

    @Invoker(value = "onSlotChange")
    void bogo$onSlotChanged(ItemStack oldItem, ItemStack newItem);

    @Invoker(value = "onPickupFromSlot")
    void bogo$onTake(EntityPlayer player, ItemStack itemStack);
}
