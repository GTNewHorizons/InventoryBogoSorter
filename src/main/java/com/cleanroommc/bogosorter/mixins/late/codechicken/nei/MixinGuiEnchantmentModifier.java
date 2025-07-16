package com.cleanroommc.bogosorter.mixins.late.codechicken.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import codechicken.nei.GuiEnchantmentModifier;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@Mixin(GuiEnchantmentModifier.class)
public abstract class MixinGuiEnchantmentModifier extends GuiContainer implements IMTModGuiContainer2 {

    public MixinGuiEnchantmentModifier(Container p_i1072_1_) {
        super(p_i1072_1_);
    }

    @Override
    public boolean MT_isMouseTweaksDisabled() {
        return false;
    }

    @Override
    public boolean MT_isWheelTweakDisabled() {
        return false;
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
        return false;
    }

    @Override
    public boolean MT_disableRMBDraggingFunctionality() {
        if (field_147007_t && ((GuiContainerAccessor) this).getDragSplittingButton() == 1) {
            field_147007_t = false;
            // Don't ignoreMouseUp on slots that can't accept the item. (crafting output, ME slot, etc.)
            if (theSlot != null && theSlot.isItemValid(mc.thePlayer.inventory.getItemStack())) {
                ((GuiContainerAccessor) this).setIgnoreMouseUp(true);
            }
            return true;
        }
        return false;
    }
}
