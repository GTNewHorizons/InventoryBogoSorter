package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.bogosorter.client.PinnedSlotClient;

@Mixin(RenderItem.class)
public abstract class RenderItemPinnedSlotMixin {

    @Inject(method = "renderItemAndEffectIntoGUI", at = @At("RETURN"))
    private void bogosorter$drawPinnedSlotIcon(FontRenderer font, TextureManager textures, ItemStack stack, int x,
        int y, CallbackInfo ci) {
        PinnedSlotClient.drawIconAfterItem(x, y);
    }
}
