package com.cleanroommc.bogosorter.mixins.early.minecraft;

import java.util.List;

import com.cleanroommc.bogosorter.common.sort.IGuiContainerAccessor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiContainer.class)
public abstract class GuiContainerMixin extends GuiScreen implements IGuiContainerAccessor {

    @Shadow private boolean field_146995_H;
    @Shadow protected boolean field_147007_t;
    @Shadow private int field_146988_G;
    @Shadow protected abstract Slot getSlotAtPosition(int x, int y);
    @Shadow protected abstract void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType);

    @Shadow
    protected int guiTop;

    @Shadow
    protected int guiLeft;

    @Override
    public List<net.minecraft.client.gui.GuiButton> getButtons() {
        return buttonList;
    }

    @Override
    public int getGuiTop() {
        return guiTop;
    }

    @Override
    public int getGuiLeft() {
        return guiLeft;
    }

    @Override
    public void setIgnoreMouseUp(boolean ignoreMouseUp) {
        this.field_146995_H = ignoreMouseUp;

    }
    @Override
    public boolean getDragSplitting() {
        return field_147007_t;
    }

    @Override
    public void setDragSplitting(boolean dragSplitting) {
        this.field_147007_t = dragSplitting;

    }

    @Override
    public int getDragSplittingButton() {
        return field_146988_G;
    }

    @Override
    public Slot getSlotAt(int x, int y) {
        return this.getSlotAtPosition(x,y);
    }

    @Override
    public void mouseClick(Slot slot, int slotId, int mouseButton, int clickType) {
        this.handleMouseClick(slot, slotId, mouseButton, clickType);
    }
}
