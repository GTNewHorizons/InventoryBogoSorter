package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.bogosorter.client.favourite.FavouriteRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Draws favourite-slot outlines inside GuiContainer.drawScreen, just before vanilla
 * paints the hovered slot's tooltip. Doing it here (instead of in DrawScreenEvent.Post)
 * means the tooltip naturally lands on top of the outline without any re-render hacks.
 */
@SideOnly(Side.CLIENT)
@Mixin(GuiContainer.class)
public abstract class GuiContainerFavouriteMixin {

    @Inject(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiContainer;func_146979_b(II)V",
            shift = At.Shift.AFTER),
        require = 1)
    private void bogo$drawFavouriteOutlines(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        FavouriteRenderer.drawContainerOutlines((GuiContainer) (Object) this);
    }
}
