package com.cleanroommc.bogosorter.mixins.early.minecraft;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Accessor(value = "buttonList")
    List<GuiButton> getButtons();
}
