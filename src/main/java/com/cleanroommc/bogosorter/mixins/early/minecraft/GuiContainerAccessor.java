package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor extends GuiScreenAccessor {

    @Accessor
    int getXSize();

    @Accessor
    int getYSize();

    @Accessor
    int getGuiLeft();

    @Accessor
    int getGuiTop();

    @Accessor(value = "field_146995_H")
    void setIgnoreMouseUp(boolean ignoreMouseUp);

    @Accessor(value = "field_147007_t")
    boolean getDragSplitting();

    @Accessor(value = "field_147007_t")
    void setDragSplitting(boolean dragSplitting);

    @Accessor(value = "field_146988_G")
    int getDragSplittingButton();

    @Invoker
    Slot callGetSlotAtPosition(int x, int y);

    @Invoker
    void callHandleMouseClick(Slot slot, int slotId, int mouseButton, int clickType);
}
