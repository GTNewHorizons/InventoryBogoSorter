package yalter.mousetweaks.handlers;

import static net.minecraftforge.event.ForgeEventFactory.getFuelBurnTime;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.inventory.SlotMerchantResult;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import yalter.mousetweaks.ClientEventHandler;
import yalter.mousetweaks.api.MouseTweaksDisableWheelTweak;
import yalter.mousetweaks.api.MouseTweaksIgnore;
import yalter.mousetweaks.impl.IGuiScreenHandler;
import yalter.mousetweaks.impl.MouseButton;

public class GuiContainerHandler implements IGuiScreenHandler {

    protected Minecraft mc;
    protected GuiContainer guiContainer;
    protected GuiContainerAccessor mixinGuiContainer;

    public GuiContainerHandler(GuiContainer guiContainer) {
        this.mc = Minecraft.getMinecraft();
        this.guiContainer = guiContainer;
        this.mixinGuiContainer = (GuiContainerAccessor) guiContainer;
    }

    private int getDisplayWidth() {
        return mc.displayWidth;
    }

    private int getDisplayHeight() {
        return mc.displayHeight;
    }

    public int getRequiredMouseX() {
        return (Mouse.getX() * guiContainer.width) / getDisplayWidth();
    }

    public int getRequiredMouseY() {
        return guiContainer.height - ((Mouse.getY() * guiContainer.height) / getDisplayHeight()) - 1;
    }

    @Override
    public boolean isMouseTweaksDisabled() {
        return guiContainer.getClass()
            .isAnnotationPresent(MouseTweaksIgnore.class) || (mixinGuiContainer == null)
            || ClientEventHandler.isMouseTweakDisabled(guiContainer.getClass());
    }

    @Override
    public boolean isWheelTweakDisabled() {
        return guiContainer.getClass()
            .isAnnotationPresent(MouseTweaksDisableWheelTweak.class)
            || ClientEventHandler.isWheelTweakDisabled(guiContainer.getClass());
    }

    @Override
    public List<Slot> getSlots() {
        return guiContainer.inventorySlots.inventorySlots;
    }

    @Override
    public Slot getSlotUnderMouse() {
        return mixinGuiContainer.getSlotAt(getRequiredMouseX(), getRequiredMouseY());
    }

    @Override
    public boolean disableRMBDraggingFunctionality() {
        mixinGuiContainer.setIgnoreMouseUp(true);

        if (mixinGuiContainer.getDragSplitting()) {
            if (mixinGuiContainer.getDragSplittingButton() == 1) {
                mixinGuiContainer.setDragSplitting(false);
                return true;
            }
        }

        return false;
    }

    @Override
    public void clickSlot(Slot slot, MouseButton mouseButton, boolean shiftPressed) {
        mixinGuiContainer.mouseClick(slot, slot.slotNumber, mouseButton.getValue(), shiftPressed ? 1 : 0);
    }

    @Override
    public boolean isCraftingOutput(Slot slot) {
        return (slot instanceof SlotCrafting || slot instanceof SlotFurnace
            || slot instanceof SlotMerchantResult
            || (guiContainer.inventorySlots instanceof ContainerRepair && slot.slotNumber == 2));
    }

    @Override
    public boolean isIgnored(Slot slot) {
        return false;
    }

    @Override
    public int isSlotPrioritized(Slot slot, ItemStack stack) {
        if (getFuelBurnTime(stack) != 0) {
            if (slot instanceof SlotFurnace) return 1;
            return 0;
        }
        return -1;
    }
}
