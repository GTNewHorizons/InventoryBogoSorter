package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.cleanroommc.bogosorter.common.refill.DamageHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @ModifyExpressionValue(
        method = "damageItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;attemptDamageItem(ILjava/util/Random;)Z"))
    private boolean tryRefillItem(boolean original, int p_77972_1_, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer player && !(player instanceof FakePlayer)) {
            DamageHelper.damageItemHook(player, (ItemStack) (Object) this);
        }
        return original;
    }

}
