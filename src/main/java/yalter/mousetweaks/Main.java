package yalter.mousetweaks;

import static net.minecraft.item.ItemStack.copyItemStack;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import cpw.mods.fml.common.Loader;
import yalter.mousetweaks.api.IMTModGuiContainer;
import yalter.mousetweaks.api.IMTModGuiContainer2;
import yalter.mousetweaks.api.IMTModGuiContainer2Ex;
import yalter.mousetweaks.config.MTConfig;
import yalter.mousetweaks.handlers.GuiContainerCreativeHandler;
import yalter.mousetweaks.handlers.GuiContainerHandler;
import yalter.mousetweaks.handlers.IMTModGuiContainer2ExHandler;
import yalter.mousetweaks.handlers.IMTModGuiContainer2Handler;
import yalter.mousetweaks.handlers.IMTModGuiContainerHandler;
import yalter.mousetweaks.impl.IGuiScreenHandler;
import yalter.mousetweaks.impl.IMouseState;
import yalter.mousetweaks.impl.MouseButton;

public class Main {

    private static IMouseState mouseState = new SimpleMouseState();
    private static GuiScreen oldGuiScreen = null;
    private static Slot oldSelectedSlot = null;
    private static Slot firstRightClickedSlot = null;
    private static boolean oldRMBDown = false;
    private static boolean disableForThisContainer = false;
    private static boolean disableWheelForThisContainer = false;
    private static boolean lwjgl3Loaded = false;

    private static IGuiScreenHandler handler = null;

    private static boolean initialized = false;

    public static boolean initialize() {
        if (initialized) {
            return true;
        }

        lwjgl3Loaded = Loader.isModLoaded("lwjgl3ify");
        if (lwjgl3Loaded) {
            MouseTweaks.LOGGER.info("Detected lwjgl3!");
        }

        initialized = true;

        MouseTweaks.LOGGER.debug("Mouse Tweaks has been initialized.");

        return true;
    }

    public static void onUpdateInGame() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null) {
            // Reset stuff
            oldGuiScreen = null;
            oldSelectedSlot = null;
            firstRightClickedSlot = null;
            disableForThisContainer = false;
            disableWheelForThisContainer = false;
            handler = null;

        } else onUpdateInGui(currentScreen);

        oldRMBDown = mouseState.isButtonPressed(MouseButton.RIGHT);
    }

    public static boolean isLwjgl3Loaded() {
        return lwjgl3Loaded;
    }

    public static void onMouseInput() {
        mouseState.update();
    }

    private static void onUpdateInGui(GuiScreen currentScreen) {

        if (oldGuiScreen != currentScreen) {
            oldGuiScreen = currentScreen;
            handler = findHandler(currentScreen);

            // don't handle any mouse inputs that were started from the old gui
            mouseState.clear();

            if (handler == null) {
                disableForThisContainer = true;
                return;
            } else {
                disableForThisContainer = handler.isMouseTweaksDisabled();
                disableWheelForThisContainer = handler.isWheelTweakDisabled();

                if (isMouseWheelTransferActive() && !disableWheelForThisContainer) Mouse.getDWheel(); // reset the mouse
                                                                                                      // wheel delta
            }
        }

        // If everything is disabled there's nothing to do.
        if (!MTConfig.rmbTweak && !MTConfig.lmbTweakWithItem
            && !MTConfig.lmbTweakWithoutItem
            && !isMouseWheelTransferActive()) return;

        if (disableForThisContainer) return;

        Slot selectedSlot = handler.getSlotUnderMouse();

        if (mouseState.isButtonPressed(MouseButton.RIGHT)) {
            if (!oldRMBDown) firstRightClickedSlot = selectedSlot;

            if (MTConfig.rmbTweak && firstRightClickedSlot != null
                && !handler.isCraftingOutput(firstRightClickedSlot)
                && handler.disableRMBDraggingFunctionality()) {
                // Check some conditions to see if we really need to click the first slot.
                // This condition is here to prevent double-clicking.
                if ((firstRightClickedSlot != selectedSlot || oldSelectedSlot == selectedSlot)
                    && !handler.isIgnored(firstRightClickedSlot)) {
                    ItemStack targetStack = firstRightClickedSlot.getStack();
                    ItemStack stackOnMouse = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();

                    if (stackOnMouse != null && areStacksCompatible(stackOnMouse, targetStack)
                        && firstRightClickedSlot.isItemValid(stackOnMouse)) {
                        handler.clickSlot(firstRightClickedSlot, MouseButton.RIGHT, false);
                    }
                }
            }
        } else {
            firstRightClickedSlot = null;
        }

        if (oldSelectedSlot != selectedSlot) {
            oldSelectedSlot = selectedSlot;

            // Nothing to do if no slot is selected.
            if (selectedSlot == null) return;

            // Prevent double-clicking.
            if (firstRightClickedSlot == selectedSlot) firstRightClickedSlot = null;

            // Copy stacks, otherwise when we click stuff they get updated and mess up the logic.
            ItemStack targetStack = copyItemStack(selectedSlot.getStack());
            ItemStack stackOnMouse = copyItemStack(Minecraft.getMinecraft().thePlayer.inventory.getItemStack());

            boolean shiftIsDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

            if (mouseState.isButtonPressed(MouseButton.RIGHT)) {
                // Right mouse button
                if (MTConfig.rmbTweak) {
                    if (!handler.isIgnored(selectedSlot) && !handler.isCraftingOutput(selectedSlot)
                        && stackOnMouse != null
                        && areStacksCompatible(stackOnMouse, targetStack)
                        && selectedSlot.isItemValid(stackOnMouse)) {
                        handler.clickSlot(selectedSlot, MouseButton.RIGHT, false);
                    }
                }
            } else if (mouseState.isButtonPressed(MouseButton.LEFT)) {
                // Left mouse button
                if (stackOnMouse != null) {
                    if (MTConfig.lmbTweakWithItem) {
                        if (!handler.isIgnored(selectedSlot) && targetStack != null
                            && areStacksCompatible(stackOnMouse, targetStack)) {
                            if (shiftIsDown) {
                                // If shift is down, we just shift-click the slot and the item gets moved into another
                                // inventory.
                                handler.clickSlot(selectedSlot, MouseButton.LEFT, true);
                            } else {
                                // If shift is not down, we need to merge the item stack on the mouse with the one in
                                // the slot.
                                if ((((GuiContainerAccessor) currentScreen).getDragSplitting() == false)
                                    && (stackOnMouse.stackSize + targetStack.stackSize)
                                        <= stackOnMouse.getMaxStackSize()) {
                                    // We need to click on the slot so that our item stack gets merged with it, and
                                    // then click again to return the stack to the mouse. However, if the slot is
                                    // crafting output, then the item is added to the mouse stack on the first click
                                    // and we don't need to click the second time.
                                    handler.clickSlot(selectedSlot, MouseButton.LEFT, false);

                                    if (!handler.isCraftingOutput(selectedSlot))
                                        handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                                }
                            }
                        }
                    }
                } else if (MTConfig.lmbTweakWithoutItem) {
                    if (targetStack != null && shiftIsDown && !handler.isIgnored(selectedSlot)) {
                        handler.clickSlot(selectedSlot, MouseButton.LEFT, true);
                    }
                }
            }
        }
        handleWheel(selectedSlot);
    }

    private static void handleWheel(Slot selectedSlot) {
        if (!isMouseWheelTransferActive() || disableWheelForThisContainer) {
            return;
        }
        int wheel = mouseState.consumeScrollAmount();
        if ((wheel != 0) && (selectedSlot != null)) {
            int numItemsToMove = Math.abs(wheel);
            if (numItemsToMove == 0 || selectedSlot == null || handler.isIgnored(selectedSlot)) return;
            ItemStack stackOnMouse = copyItemStack(Minecraft.getMinecraft().thePlayer.inventory.getItemStack());
            ItemStack originalStack = copyItemStack(selectedSlot.getStack());
            boolean isCraftingOutput = handler.isCraftingOutput(selectedSlot);

            // Rather complex condition to determine when the wheel tweak can't be used.
            if (originalStack == null
                || (stackOnMouse != null && (isCraftingOutput != areStacksCompatible(originalStack, stackOnMouse))))
                return;

            List<Slot> slots = handler.getSlots();

            boolean pushItems = wheel < 0;
            if (MTConfig.wheelScrollDirection.isPositionAware() && otherInventoryIsAbove(selectedSlot, slots)) {
                pushItems = !pushItems;
            }
            if (MTConfig.wheelScrollDirection.isInverted()) {
                pushItems = !pushItems;
            }

            if (isCraftingOutput) {
                if (pushItems) {
                    if (originalStack == null) return;

                    Slot applicableSlot = findWheelApplicableSlot(slots, selectedSlot, pushItems);

                    for (int i = 0; i < numItemsToMove; i++) handler.clickSlot(selectedSlot, MouseButton.LEFT, false);

                    if (applicableSlot != null && stackOnMouse == null)
                        handler.clickSlot(applicableSlot, MouseButton.LEFT, false);
                }

                return;
            }
            int attemptsLeft = 100;
            do {
                attemptsLeft--;
                Slot applicableSlot = findWheelApplicableSlot(slots, selectedSlot, pushItems);
                if (applicableSlot == null) break;

                if (pushItems) {
                    Slot slotTo = applicableSlot;
                    Slot slotFrom = selectedSlot;
                    ItemStack stackTo = copyItemStack(slotTo.getStack());
                    ItemStack stackFrom = copyItemStack(slotFrom.getStack());

                    numItemsToMove = Math.min(numItemsToMove, stackFrom.stackSize);

                    if (stackTo != null && (stackTo.getMaxStackSize() - stackTo.stackSize) <= numItemsToMove) {
                        // The applicable slot fits in less items than we can move.
                        handler.clickSlot(slotFrom, MouseButton.LEFT, false);
                        handler.clickSlot(slotTo, MouseButton.LEFT, false);
                        handler.clickSlot(slotFrom, MouseButton.LEFT, false);

                        numItemsToMove -= stackTo.getMaxStackSize() - stackTo.stackSize;
                    } else {
                        handler.clickSlot(slotFrom, MouseButton.LEFT, false);

                        if (stackFrom.stackSize <= numItemsToMove) {
                            handler.clickSlot(slotTo, MouseButton.LEFT, false);
                        } else {
                            for (int i = 0; i < numItemsToMove; i++)
                                handler.clickSlot(slotTo, MouseButton.RIGHT, false);
                        }

                        handler.clickSlot(slotFrom, MouseButton.LEFT, false);

                        break;
                    }
                } else {
                    Slot slotTo = selectedSlot;
                    Slot slotFrom = applicableSlot;
                    ItemStack stackTo = copyItemStack(slotTo.getStack());
                    ItemStack stackFrom = copyItemStack(slotFrom.getStack());

                    if (stackTo.stackSize == stackTo.getMaxStackSize()) break;

                    if ((stackTo.getMaxStackSize() - stackTo.stackSize) <= numItemsToMove) {
                        handler.clickSlot(slotFrom, MouseButton.LEFT, false);
                        handler.clickSlot(slotTo, MouseButton.LEFT, false);

                        if (!handler.isCraftingOutput(slotFrom)) handler.clickSlot(slotFrom, MouseButton.LEFT, false);
                    } else {
                        handler.clickSlot(slotFrom, MouseButton.LEFT, false);

                        if (handler.isCraftingOutput(slotFrom)) {
                            handler.clickSlot(slotTo, MouseButton.LEFT, false);
                            --numItemsToMove;
                        } else if (stackFrom.stackSize <= numItemsToMove) {
                            handler.clickSlot(slotTo, MouseButton.LEFT, false);
                            numItemsToMove -= stackFrom.stackSize;
                        } else {
                            for (int i = 0; i < numItemsToMove; i++)
                                handler.clickSlot(slotTo, MouseButton.RIGHT, false);

                            numItemsToMove = 0;
                        }

                        if (!handler.isCraftingOutput(slotFrom)) handler.clickSlot(slotFrom, MouseButton.LEFT, false);
                    }
                }
            } while (numItemsToMove > 0 && attemptsLeft > 0);
        }
    }

    // Returns true if the other inventory is above the selected slot inventory.
    //
    // This is used for the inventory position aware scroll direction. To prevent any surprises, this should have the
    // same logic for what constitutes the "other" inventory as findWheelApplicableSlot().
    private static boolean otherInventoryIsAbove(Slot selectedSlot, List<Slot> slots) {
        boolean selectedIsInPlayerInventory = selectedSlot.inventory == Minecraft.getMinecraft().thePlayer.inventory;
        for (Slot slot : slots) {
            if ((slot.inventory == Minecraft.getMinecraft().thePlayer.inventory) != selectedIsInPlayerInventory
                && slot.yDisplayPosition < selectedSlot.yDisplayPosition) {
                return true;
            }
        }
        return false;
    }

    // Finds the appropriate handler to use with this GuiScreen. Returns null if no handler was found.
    @SuppressWarnings("deprecation")
    private static IGuiScreenHandler findHandler(GuiScreen currentScreen) {
        if (currentScreen instanceof IMTModGuiContainer2Ex) {
            return new IMTModGuiContainer2ExHandler((IMTModGuiContainer2Ex) currentScreen);
        } else if (currentScreen instanceof IMTModGuiContainer2) {
            return new IMTModGuiContainer2Handler((IMTModGuiContainer2) currentScreen);
        } else if (currentScreen instanceof IMTModGuiContainer) {
            return new IMTModGuiContainerHandler((IMTModGuiContainer) currentScreen);
        } else if (currentScreen instanceof GuiContainerCreative) {
            return new GuiContainerCreativeHandler((GuiContainerCreative) currentScreen);
        } else if (currentScreen instanceof GuiContainer) {
            return new GuiContainerHandler((GuiContainer) currentScreen);
        }

        return null;
    }

    // Returns true if we can put items from one stack into another.
    // This is different from ItemStack.areItemsEqual() because here empty stacks are compatible with anything.
    private static boolean areStacksCompatible(ItemStack a, ItemStack b) {
        return a == null || b == null || (a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b));
    }

    private static Slot findWheelApplicableSlot(List<Slot> slots, Slot selectedSlot, boolean pushItems) {
        int startIndex, endIndex, direction;
        if (pushItems || MTConfig.wheelSearchOrder == MTConfig.WheelSearchOrder.FIRST_TO_LAST) {
            startIndex = 0;
            endIndex = slots.size();
            direction = 1;
        } else {
            startIndex = slots.size() - 1;
            endIndex = -1;
            direction = -1;
        }

        ItemStack originalStack = selectedSlot.getStack();
        boolean findInPlayerInventory = (selectedSlot.inventory != Minecraft.getMinecraft().thePlayer.inventory);
        Slot rv = null;
        Slot firstAllowedEncounter = null;

        for (int i = startIndex; i != endIndex; i += direction) {
            Slot slot = slots.get(i);

            if (handler.isIgnored(slot)) continue;

            if (findInPlayerInventory) {
                if (slot.inventory != Minecraft.getMinecraft().thePlayer.inventory) continue;
            } else {
                if (slot.inventory == Minecraft.getMinecraft().thePlayer.inventory) continue;
            }

            ItemStack stack = slot.getStack();
            int priority = handler.isSlotPrioritized(slot, originalStack);

            if (stack == null) {
                if (rv == null && pushItems && slot.isItemValid(originalStack) && !handler.isCraftingOutput(slot)) {
                    switch (priority) {
                        case -1:
                        case 1:
                            rv = slot;
                            break;
                        case 0:
                            if (firstAllowedEncounter == null) firstAllowedEncounter = slot;
                            break;
                    }
                }
            } else if (areStacksCompatible(originalStack, stack)) {
                if (pushItems) {
                    if (!handler.isCraftingOutput(slot) && stack.stackSize < stack.getMaxStackSize())
                        switch (priority) {
                        case -1:
                        case 1:
                            return slot;
                        case 0:
                            firstAllowedEncounter = slot;
                    }
                } else {
                    return slot;
                }
            }
        }
        if (rv == null && firstAllowedEncounter != null) return firstAllowedEncounter;
        return rv;
    }

    private static final boolean isNEIPresent = Loader.isModLoaded("NotEnoughItems");

    private static boolean isMouseWheelTransferActive() {
        if (isNEIPresent) {
            return MTConfig.wheelTweak && !codechicken.nei.NEIClientConfig.isMouseScrollTransferEnabled();
        }
        return MTConfig.wheelTweak;
    }
}
