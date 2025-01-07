package yalter.mousetweaks.handlers;

import com.cleanroommc.bogosorter.common.sort.IGuiContainerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import yalter.mousetweaks.impl.IGuiScreenHandler;
import yalter.mousetweaks.impl.MouseButton;
import yalter.mousetweaks.api.IMTModGuiContainer2;

import java.util.List;

public class IMTModGuiContainer2Handler implements IGuiScreenHandler {
    protected Minecraft mc;
    protected IMTModGuiContainer2 modGuiContainer;
    protected IGuiContainerAccessor mixinGuiContainer;


    public IMTModGuiContainer2Handler(IMTModGuiContainer2 modGuiContainer) {
        this.mc = Minecraft.getMinecraft();
        this.modGuiContainer = modGuiContainer;
        try {
            this.mixinGuiContainer = (IGuiContainerAccessor) modGuiContainer;
        } catch (ClassCastException e) {
            CrashReport crashreport = CrashReport.makeCrashReport(e, "Mod GuiContainer could not be cast to IGuiContainerAccessor.");
            throw new ReportedException(crashreport);
        }
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
        mixinGuiContainer.mouseClick(slot,
            slot.slotNumber,
            mouseButton.getValue(),
            shiftPressed ? 1 : 0);
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
