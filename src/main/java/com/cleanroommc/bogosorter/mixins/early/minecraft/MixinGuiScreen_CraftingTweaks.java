package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.blay09.mods.craftingtweaks.CraftingTweaks;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(GuiScreen.class)
public class MixinGuiScreen_CraftingTweaks {

    @Inject(
        method = "handleMouseInput",
        at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false),
        cancellable = true)
    private void bogosorter$handleMouseInput(CallbackInfo ci, @Local(ordinal = 0) int mouseX,
        @Local(ordinal = 1) int mouseY, @Local(ordinal = 2) int btn) {
        if (CraftingTweaks.onGuiClick(mouseX, mouseY, btn)) {
            ci.cancel();
        }
    }
}
