package com.cleanroommc.bogosorter.mixins.late.codechicken.core;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;

import codechicken.core.inventory.GuiContainerWidget;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@Mixin(GuiContainerWidget.class)
public abstract class MixinGuiContainerWidget implements IMTModGuiContainer2 {

    @Override
    public boolean MT_isMouseTweaksDisabled() {
        return true;
    }

    @Override
    public boolean MT_isWheelTweakDisabled() {
        return true;
    }

    @Override
    public Container MT_getContainer() {
        return null;
    }

    @Override
    public Slot MT_getSlotUnderMouse() {
        return null;
    }

    @Override
    public boolean MT_isCraftingOutput(Slot slot) {
        return false;
    }

    @Override
    public boolean MT_isIgnored(Slot slot) {
        return true;
    }

    @Override
    public boolean MT_disableRMBDraggingFunctionality() {
        return false;
    }
}
