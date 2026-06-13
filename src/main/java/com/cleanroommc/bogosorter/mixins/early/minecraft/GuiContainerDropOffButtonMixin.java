package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.bogosorter.common.dropoff.DropOffInvButton;

@Mixin(GuiContainer.class)
public abstract class GuiContainerDropOffButtonMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void bogosorter$toggleCoinDepositDestination(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton != 1 || !GuiScreen.isShiftKeyDown() || !((Object) this instanceof GuiInventory)) {
            return;
        }

        for (GuiButton button : ((GuiScreenAccessor) this).getButtonList()) {
            if (button instanceof DropOffInvButton dropOffButton && dropOffButton.isMouseOver(mouseX, mouseY)) {
                if (dropOffButton.toggleCoinDepositDestination()) {
                    ci.cancel();
                }
                return;
            }
        }
    }
}
