package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mixin(Slot.class)
public interface SlotAccessor {

    @Accessor(value = "xDisplayPosition")
    int bogo$getX();

    @Accessor(value = "yDisplayPosition")
    int bogo$getY();

    @Accessor
    int getSlotNumber();

    @Invoker(remap = false)
    int callGetSlotIndex();

    @Accessor
    IInventory getInventory();

    @Invoker
    void callPutStack(ItemStack itemStack);

    @Invoker
    ItemStack callGetStack();

    @Invoker
    int callGetSlotStackLimit();

    @SideOnly(Side.CLIENT)
    @Invoker(value = "func_111238_b")
    boolean callIsEnabled();

    @Invoker
    boolean callIsItemValid(ItemStack stack);

    @Invoker
    boolean callCanTakeStack(EntityPlayer player);

    @Invoker
    void callOnSlotChanged();

    @Invoker
    void callOnSlotChange(ItemStack oldItem, ItemStack newItem);

    @Invoker
    void callOnPickupFromSlot(EntityPlayer player, ItemStack itemStack);
}
