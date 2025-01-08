package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.cleanroommc.bogosorter.common.HotbarSwap;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    @Redirect(
        method = "runTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"))
    public void mouseInput(InventoryPlayer instance, int p_70453_1_) {
        if (!HotbarSwap.doCancelHotbarSwap()) {
            thePlayer.inventory.changeCurrentItem(p_70453_1_);
        }
    }
}
