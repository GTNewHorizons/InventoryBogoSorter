package com.cleanroommc.bogosorter.mixins.late.codechicken.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;

import codechicken.nei.recipe.GuiRecipe;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@Mixin(GuiRecipe.class)
public abstract class MixinGuiRecipe extends GuiContainer implements IMTModGuiContainer2 {

    public MixinGuiRecipe(Container p_i1072_1_) {
        super(p_i1072_1_);
    }

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
        return inventorySlots;
    }

    @Override
    public Slot MT_getSlotUnderMouse() {
        return theSlot;
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
