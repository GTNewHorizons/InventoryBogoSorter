//package com.cleanroommc.bogosorter.mixins.early.minecraft;
//
//import java.util.Random;
//
//import net.minecraft.item.ItemStack;
//
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//import com.cleanroommc.bogosorter.common.refill.DamageHelper;
//
//@Mixin(ItemStack.class)
//public abstract class ItemStackMixin {
//
//    @Inject(
//        method = "attemptDamageItem",
//        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER),
//        cancellable = true)
//    private void damageItem(int p_96631_1_, Random p_96631_2_,  CallbackInfoReturnable<Boolean> cir) {
//            DamageHelper.damageItemHook((ItemStack) (Object) this);
//        }
//    }
