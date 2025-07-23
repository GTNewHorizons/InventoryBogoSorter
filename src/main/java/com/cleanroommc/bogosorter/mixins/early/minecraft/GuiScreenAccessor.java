package com.cleanroommc.bogosorter.mixins.early.minecraft;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Accessor
    List<GuiButton> getButtonList();

    @Invoker(value = "func_146283_a")
    void drawHoveringText(List<String> textLines, int x, int y);
}
