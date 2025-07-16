package com.cleanroommc.bogosorter.mixins.late.forestry;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import forestry.core.gui.GuiForestry;
import forestry.factory.gui.ContainerCarpenter;
import forestry.factory.gui.ContainerCentrifuge;
import forestry.factory.gui.ContainerFabricator;
import forestry.factory.gui.ContainerMoistener;
import forestry.factory.gui.ContainerSqueezer;
import forestry.factory.gui.ContainerWorktable;
import yalter.mousetweaks.api.IMTModGuiContainer2;

@Mixin(GuiForestry.class)
public abstract class MixinGuiForestry extends GuiContainer implements IMTModGuiContainer2 {

    public MixinGuiForestry(Container p_i1072_1_) {
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
        if (inventorySlots instanceof ContainerSqueezer Squeezer) {
            return slot == Squeezer.getSlot(11) || slot == Squeezer.getSlot(9);
        }
        if (inventorySlots instanceof ContainerMoistener Moistener) {
            return slot == Moistener.getSlot(9);
        }
        if (inventorySlots instanceof ContainerCentrifuge Centrifuge) {
            for (int i = 1; i <= 9; i++) {
                if (slot == Centrifuge.getSlot(i)) {
                    return true;
                }
            }
        }
        if (inventorySlots instanceof ContainerCarpenter Carpenter) {
            return slot == Carpenter.getSlot(21);
        }
        if (inventorySlots instanceof ContainerFabricator Fabricator) {
            return slot == Fabricator.getSlot(20);
        }
        if (inventorySlots instanceof ContainerWorktable Worktable) {
            return slot == Worktable.getSlot(27);
        }
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
