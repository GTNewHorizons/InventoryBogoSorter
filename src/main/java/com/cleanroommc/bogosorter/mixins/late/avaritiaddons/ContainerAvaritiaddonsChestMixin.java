package com.cleanroommc.bogosorter.mixins.late.avaritiaddons;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wanion.avaritiaddons.block.chest.ContainerAvaritiaddonsChest;



 @Mixin(ContainerAvaritiaddonsChest.class)
 public abstract class ContainerAvaritiaddonsChestMixin {

     @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true)
     public void transferItem(EntityPlayer entityPlayer, int slotNumber, CallbackInfoReturnable<ItemStack> cir) {
         Container container = (Container) (Object) this;
         ISlot slot = BogoSortAPI.getSlot(container, slotNumber);

         ItemStack stack = slot.bogo$getStack();
         if (stack != null) {
             ItemStack toInsert = stack.copy();
             int amount = Math.min(stack.stackSize, stack.getMaxStackSize());
             toInsert.stackSize = (amount);
             GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

             SlotGroup slots = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
             SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() :
                 sortingContext.getPlayerSlotGroup();
             if (otherSlots == null || slots == otherSlots) {
                 cir.setReturnValue(null);
                 return;
             }

             toInsert = BogoSortAPI.insert(container, otherSlots.getSlots(), toInsert);
             if (toInsert == null){
                 slot.bogo$putStack(null);
             } else {
                stack.stackSize -= (amount - toInsert.stackSize);
                slot.bogo$putStack(toInsert.stackSize > 0 ? toInsert : null);
             }
             cir.setReturnValue(toInsert);
         }
         cir.setReturnValue(null);
     }
 }
