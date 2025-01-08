package com.cleanroommc.bogosorter.common.sort;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;

public interface IGuiContainerAccessor {

    List<GuiButton> getButtons();

    int getGuiTop();

    int getGuiLeft();

    void setIgnoreMouseUp(boolean ignoreMouseUp);

    boolean getDragSplitting();

    void setDragSplitting(boolean dragSplitting);

    int getDragSplittingButton();

    Slot getSlotAt(int x, int y);

    void mouseClick(Slot slot, int slotId, int mouseButton, int clickType);
}
