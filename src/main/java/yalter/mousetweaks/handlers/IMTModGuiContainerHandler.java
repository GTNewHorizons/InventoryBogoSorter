package yalter.mousetweaks.handlers;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import yalter.mousetweaks.api.IMTModGuiContainer;
import yalter.mousetweaks.impl.IGuiScreenHandler;
import yalter.mousetweaks.impl.MouseButton;

import java.util.ArrayList;
import java.util.List;

public class IMTModGuiContainerHandler implements IGuiScreenHandler {
    @SuppressWarnings("deprecation")
    protected IMTModGuiContainer modGuiContainer;

    @SuppressWarnings("deprecation")
    public IMTModGuiContainerHandler(IMTModGuiContainer modGuiContainer) {
        this.modGuiContainer = modGuiContainer;
    }

    @Override
    public boolean isMouseTweaksDisabled() {
        return modGuiContainer.isMouseTweaksDisabled();
    }

    @Override
    public boolean isWheelTweakDisabled() {
        return modGuiContainer.isWheelTweakDisabled();
    }

    @Override
    public List<Slot> getSlots() {
        List<Slot> slots = new ArrayList<>();

        Object container = modGuiContainer.getModContainer();
        int count = modGuiContainer.getModSlotCount(container);
        for (int i = 0; i < count; ++i)
            slots.add((Slot) modGuiContainer.getModSlot(container, i));

        return slots;
    }

    @Override
    public Slot getSlotUnderMouse() {
        Object container = modGuiContainer.getModContainer();
        return (Slot) modGuiContainer.getModSelectedSlot(container, modGuiContainer.getModSlotCount(container));
    }

    @Override
    public boolean disableRMBDraggingFunctionality() {
        modGuiContainer.disableRMBDragIfRequired(modGuiContainer.getModContainer(), null, false);
        return false;
    }

    @Override
    public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
        modGuiContainer.clickModSlot(modGuiContainer.getModContainer(), slot, mouseButton.getValue(), shiftPressed);
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return modGuiContainer.isCraftingOutputSlot(modGuiContainer.getModContainer(), slot);
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return false;
    }

    @Override
    public int isSlotPrioritized(Slot slot, ItemStack stack) {
        return -1;
    }
}
