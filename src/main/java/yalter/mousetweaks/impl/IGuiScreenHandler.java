package yalter.mousetweaks.impl;

import java.util.List;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public interface IGuiScreenHandler {

    boolean isMouseTweaksDisabled();

    boolean isWheelTweakDisabled();

    List<Slot> getSlots();

    Slot getSlotUnderMouse();

    boolean disableRMBDraggingFunctionality();

    void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed);

    boolean isCraftingOutput(Slot slot);

    boolean isIgnored(Slot slot);

    int isSlotPrioritized(Slot slot, ItemStack stack);
}
