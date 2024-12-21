package com.cleanroommc.bogosorter.mixins.early.minecraft;

import com.cleanroommc.bogosorter.common.refill.DamageHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "attemptDamageItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER), cancellable = true)
    private void damageItemCount(int p_96631_1_, Random p_96631_2_, CallbackInfoReturnable<Boolean> cir) {
            DamageHelper.damageItemHook((ItemStack) (Object) this);
    }
}
