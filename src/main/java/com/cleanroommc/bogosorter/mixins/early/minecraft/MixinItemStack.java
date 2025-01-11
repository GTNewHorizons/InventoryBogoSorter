package com.cleanroommc.bogosorter.mixins.early.minecraft;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.bogosorter.common.refill.DamageHelper;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Unique
    EntityPlayer bogo$entityPlayer = null;

    @Inject(
        method = "attemptDamageItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setItemDamage(I)V", shift = At.Shift.AFTER))
    private void bogo$damageItemCount(int p_96631_1_, Random p_96631_2_, CallbackInfoReturnable<Boolean> cir) {
        if (bogo$entityPlayer != null) DamageHelper.damageItemHook(bogo$entityPlayer, (ItemStack) (Object) this);
    }

    @Inject(
        method = "damageItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isItemStackDamageable()Z",
            shift = At.Shift.AFTER))
    private void bogo$damageItem(int p_77972_1_, EntityLivingBase p_77972_2_, CallbackInfo ci) {
        if (p_77972_2_ instanceof EntityPlayer player) {
            if (!(player instanceof FakePlayer)) {
                bogo$entityPlayer = player;
            }
        } else bogo$entityPlayer = null;
    }
}
