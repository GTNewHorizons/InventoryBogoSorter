package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.bogosorter.client.drop.DropKeyRepeatHandler;

/** Arms drop key repeats only when vanilla itself drops from the hovered slot. */
@Mixin(GuiContainer.class)
public abstract class GuiContainerDropKeyMixin {

    @Shadow
    public Slot theSlot;

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void bogosorter$armDropKeyRepeat(char typedChar, int keyCode, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        int dropKey = mc.gameSettings.keyBindDrop.getKeyCode();
        if (dropKey == 0 || keyCode != dropKey || this.theSlot == null || !this.theSlot.getHasStack()) return;
        // pick-block bound to the same key wins in vanilla, so no drop happens.
        if (dropKey == mc.gameSettings.keyBindPickBlock.getKeyCode()) return;
        // bogosorter's own throw-all shortcuts take priority while held.
        if (DropKeyRepeatHandler.isThrowShortcutHeld()) return;
        DropKeyRepeatHandler.armGui();
    }
}
