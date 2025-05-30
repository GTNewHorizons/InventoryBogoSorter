package yalter.mousetweaks.api;

import java.util.List;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * This is the extended version of the interface you want to implement in your GuiScreen to make it compatible
 * with Mouse Tweaks. It has MT_getSlots() instead of MT_getContainer(), and adds MT_clickSlot().
 */
public interface IMTModGuiContainer2Ex {

    /**
     * If you want to disable Mouse Tweaks in your GuiScreen, return true from this method.
     *
     * @return True if Mouse Tweaks should be disabled, false otherwise.
     */
    boolean MT_isMouseTweaksDisabled();

    /**
     * If you want to disable the Wheel Tweak in your GuiScreen, return true from this method.
     *
     * @return True if the Wheel Tweak should be disabled, false otherwise.
     */
    boolean MT_isWheelTweakDisabled();

    /**
     * Returns a list of Slots currently present in the inventory.
     * For vanilla containers it is this.inventorySlots.inventorySlots.
     *
     * @return List of Slots currently present in the inventory.
     */
    List<Slot> MT_getSlots();

    /**
     * Returns the Slot that is currently selected by the player, or null if no Slot is selected.
     *
     * @return Slot that is located under the mouse, or null if no Slot it currently under the mouse.
     */
    Slot MT_getSlotUnderMouse();

    /**
     * Return true if the given Slot behaves like the vanilla crafting output slots (inside the crafting table,
     * or the furnace output slot, or the anvil output slot, etc.). These slots are handled differently by Mouse Tweaks.
     *
     * @param slot the slot to check
     * @return True if slot is a crafting output slot.
     */
    boolean MT_isCraftingOutput(Slot slot);

    /**
     * Return true if the given Slot should be ignored by Mouse Tweaks. Examples of ignored slots are the item select
     * slots and the Destroy Item slot in the vanilla creative inventory.
     *
     * @param slot the slot to check
     * @return True if slot should be ignored by Mouse Tweaks.
     */
    boolean MT_isIgnored(Slot slot);

    /**
     * If your container has an RMB dragging functionality (like vanilla containers), disable it inside this method.
     * This method is called every frame (render tick), which is after all mouseClicked / mouseClickMove / mouseReleased
     * events are handled (although note these events are handled every game tick, which is far less frequent than every
     * render tick).<br>
     * <br>
     * <p>
     * If true is returned from this method, Mouse Tweaks (after checking other conditions like isIgnored) will click
     * the slot on which the right mouse button was initially pressed (in most cases this is the slot currently under
     * mouse). This is needed because the vanilla RMB dragging functionality prevents the initial slot click.<br>
     * <br>
     * <p>
     * For vanilla containers this method looks like this:
     * 
     * <pre>
     * this.ignoreMouseUp = true;
     *
     * if (this.dragSplitting) {
     *     if (this.dragSplittingButton == 1) {
     *         this.dragSplitting = false;
     *         return true;
     *     }
     * }
     *
     * return false;
     * </pre>
     *
     * @return True if Mouse Tweaks should click the slot on which the RMB was pressed.
     */
    boolean MT_disableRMBDraggingFunctionality();

    /**
     * Click the given slot.
     * <p>
     * For vanilla containers this method looks like this (mc is Minecraft):
     * 
     * <pre>
     * this.handleMouseClick(slot, slot.slotNumber, mouseButton, clickType);
     * </pre>
     *
     * @param slot        the slot to click
     * @param mouseButton the mouse button to click, left is 0 and right is 1
     * @param clickType   click type, most frequently used ones are 0 (normal left or right click)
     *                    and 1 (shift click)
     */
    void MT_clickSlot(Slot slot, int mouseButton, int clickType);

    /**
     * Custom behavior for the scroll-wheel functionality to prioritize an ItemStack to being passed to a
     * specific slot. This is used by MouseTweaks to prioritize fuels in the Fuel Slot in the Furnace, for example.
     * <p>
     * An example of how this could be implemented for the Furnace:
     * 
     * <pre>
     * if (stack.getItem()
     *     .getItemBurnTime(stack) != 0) {
     *     if (slot instanceof SlotFurnaceFuel) {
     *         return 1;
     *     }
     *     return 0;
     * }
     * return -1;
     * </pre>
     * 
     * <br>
     *
     * @param slot  The Slot to test
     * @param stack The ItemStack attempting to be passed to the slot
     * @return 1 if high-priority,
     *         0 if low priority,
     *         -1 if priority is to be ignored
     */
    int MT_scrollIsItemPrioritizedForSlot(Slot slot, ItemStack stack);
}
