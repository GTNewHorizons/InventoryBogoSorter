package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all")
@Mixin(ContainerWorkbench.class)
public abstract class ContainerWorkbenchMixin extends Container {

    @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true)
    private void onTransferStackInSlot(EntityPlayer player, int index, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            cir.setReturnValue(null);
            return;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        final int CRAFT_END = 9;

        // Player to craftinggrid
        if (index >= 10) {
            boolean moved = this.mergeItemStack(stack, 1, CRAFT_END + 1, false);
            if (!moved) {
                cir.setReturnValue(null);
                return;
            }
        }
        // craftinggrid to player
        else {
            boolean moved = false;

            // main inv
            for (int i = CRAFT_END + 1; i < this.inventorySlots.size(); i++) {
                Slot target = this.inventorySlots.get(i);
                if (target.getSlotIndex() < 9) continue;
                moved |= tryInsert(stack, target);
                if (stack.stackSize <= 0) break;
            }

            // hotbart
            if (stack.stackSize > 0) {
                for (int i = CRAFT_END + 1; i < this.inventorySlots.size(); i++) {
                    Slot target = this.inventorySlots.get(i);
                    if (target.getSlotIndex() >= 9) continue;
                    moved |= tryInsert(stack, target);
                    if (stack.stackSize <= 0) break;
                }
            }
            if (!moved) {
                cir.setReturnValue(null);
                return;
            }
        }
        if (stack.stackSize <= 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }
        slot.onPickupFromSlot(player, stack);
        cir.setReturnValue(original);
    }

    @Unique
    private boolean tryInsert(ItemStack stack, Slot target) {
        if (!target.isItemValid(stack)) return false;
        ItemStack in = target.getStack();
        int limit = Math.min(target.getSlotStackLimit(), stack.getMaxStackSize());

        if (in == null) {
            int move = Math.min(limit, stack.stackSize);
            target.putStack(stack.splitStack(move));
            return move > 0;
        } else if (in.getItem() == stack.getItem() && ItemStack.areItemStackTagsEqual(in, stack)) {
            int space = limit - in.stackSize;
            if (space <= 0) return false;
            int move = Math.min(space, stack.stackSize);
            in.stackSize += move;
            stack.stackSize -= move;
            return true;
        }
        return false;
    }
}
