package com.cleanroommc.bogosorter.mixins.early.minecraft;

import static com.cleanroommc.bogosorter.ShortcutHandler.SetCanTakeStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(Slot.class)
public class SlotMixin {

    // Temporary fix #45 until we determine the cause of the issue
    @ModifyReturnValue(method = "canTakeStack", at = @At("RETURN"))
    private boolean bogo$modifyCanTakeStack(boolean original, EntityPlayer p_82869_1_) {
        if (p_82869_1_.worldObj.isRemote && !SetCanTakeStack) {
            return false;
        }
        return original;
    }
}
