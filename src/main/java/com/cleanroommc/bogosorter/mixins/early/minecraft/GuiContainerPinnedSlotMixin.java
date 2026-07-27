package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.bogosorter.client.PinnedSlotClient;

@Mixin(GuiContainer.class)
public abstract class GuiContainerPinnedSlotMixin {

    @Inject(method = "func_146977_a", at = @At("HEAD"))
    private void bogosorter$drawPinnedSlot(Slot slot, CallbackInfo ci) {
        PinnedSlotClient.draw((GuiContainer) (Object) this, slot);
    }
}
