package yalter.mousetweaks.handlers;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import yalter.mousetweaks.api.IMTModGuiContainer2;
import yalter.mousetweaks.impl.IGuiScreenHandler;
import yalter.mousetweaks.impl.MouseButton;

public class IMTModGuiContainer2Handler implements IGuiScreenHandler {

    protected Minecraft mc;
    protected IMTModGuiContainer2 modGuiContainer;
    protected GuiContainerAccessor mixinGuiContainer;

    public IMTModGuiContainer2Handler(IMTModGuiContainer2 modGuiContainer) {
        this.mc = Minecraft.getMinecraft();
        this.modGuiContainer = modGuiContainer;
        this.mixinGuiContainer = (GuiContainerAccessor) modGuiContainer;

    }

    @Override
    public boolean isMouseTweaksDisabled() {
        return modGuiContainer.MT_isMouseTweaksDisabled();
    }

    @Override
    public boolean isWheelTweakDisabled() {
        return modGuiContainer.MT_isWheelTweakDisabled();
    }

    @Override
    public List<Slot> getSlots() {
        return modGuiContainer.MT_getContainer().inventorySlots;
    }

    @Override
    public Slot getSlotUnderMouse() {
        return modGuiContainer.MT_getSlotUnderMouse();
    }

    @Override
    public boolean disableRMBDraggingFunctionality() {
        return modGuiContainer.MT_disableRMBDraggingFunctionality();
    }

    @Override
    public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
        mixinGuiContainer.callHandleMouseClick(slot, slot.slotNumber, mouseButton.getValue(), shiftPressed ? 1 : 0);
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return modGuiContainer.MT_isCraftingOutput(slot);
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return modGuiContainer.MT_isIgnored(slot);
    }

    @Override
    public int isSlotPrioritized(Slot slot, ItemStack stack) {
        return -1;
    }
}
