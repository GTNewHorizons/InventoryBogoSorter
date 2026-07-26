package com.cleanroommc.bogosorter.client.drop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import com.cleanroommc.bogosorter.ShortcutHandler;
import com.cleanroommc.bogosorter.client.keybinds.KeyBind;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

/**
 * Backport of the modern Minecraft drop key behaviour: holding the vanilla drop key keeps dropping items after a short
 * delay. Vanilla handles the initial press, this only adds the repeats.
 */
public class DropKeyRepeatHandler {

    private enum Context {
        NONE,
        WORLD,
        GUI
    }

    private static int heldTicks = 0;
    private static boolean guiArmed = false;
    private static GuiScreen armedScreen = null;
    private static Context lastContext = Context.NONE;
    private static boolean wasPressed = false;
    private static boolean worldArmed = false;

    /** Called from the GuiContainer mixin when vanilla drops from the hovered slot. */
    public static void armGui() {
        if (!guiArmed || armedScreen != Minecraft.getMinecraft().currentScreen) heldTicks = 0;
        guiArmed = true;
        armedScreen = Minecraft.getMinecraft().currentScreen;
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getMinecraft();
        int keyCode = mc.gameSettings != null ? mc.gameSettings.keyBindDrop.getKeyCode() : 0;
        boolean pressed = keyCode != 0 && KeyBind.isKeyPressed(keyCode);
        // Arm the world path only on a fresh press observed while no screen is open.
        if (mc.currentScreen == null && pressed && !wasPressed) {
            worldArmed = true;
        }
        wasPressed = pressed;
        if (!pressed) {
            reset();
            return;
        }
        if (!BogoSorterConfig.dropKeyRepeat.enableDropKeyRepeat || mc.thePlayer == null) {
            reset();
            return;
        }
        Context context = getContext(mc);
        if (context == Context.NONE) {
            reset();
            return;
        }
        // Each context re-pays the initial delay.
        if (context != lastContext) {
            lastContext = context;
            heldTicks = 0;
            return;
        }
        heldTicks++;
        int delay = Math.max(0, BogoSorterConfig.dropKeyRepeat.initialDelayTicks);
        int interval = Math.max(1, BogoSorterConfig.dropKeyRepeat.repeatIntervalTicks);
        if (heldTicks <= delay || (heldTicks - delay - 1) % interval != 0) return;
        if (context == Context.WORLD) {
            dropInWorld(mc);
        } else {
            dropInGui(mc);
        }
    }

    private static Context getContext(Minecraft mc) {
        if (mc.currentScreen == null) {
            guiArmed = false;
            armedScreen = null;
            if (!worldArmed || !mc.inGameHasFocus) return Context.NONE;
            return Context.WORLD;
        }
        if (guiArmed && mc.currentScreen == armedScreen && mc.currentScreen instanceof GuiContainer) return Context.GUI;
        guiArmed = false;
        armedScreen = null;
        return Context.NONE;
    }

    private static void dropInWorld(Minecraft mc) {
        mc.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
    }

    private static void dropInGui(Minecraft mc) {
        GuiContainer gui = (GuiContainer) mc.currentScreen;
        Slot slot = gui.theSlot;
        // Nothing to drop, but keep counting so drops resume if the slot refills.
        if (slot == null || !slot.getHasStack()) return;
        // Mode-4 clicks require an empty cursor; a held stack would be a guaranteed no-op.
        if (mc.thePlayer.inventory.getItemStack() != null) return;
        // Runs from the tick loop, not keyTyped, so SetCanTakeStack may still be stale false from a shortcut;
        // set it true like ClientEventHandler.handleInput does so SlotMixin lets the client-side click match the
        // packet we're about to send.
        ShortcutHandler.SetCanTakeStack = true;
        ((GuiContainerAccessor) gui).callHandleMouseClick(slot, slot.slotNumber, GuiScreen.isCtrlKeyDown() ? 1 : 0, 4);
    }

    private static void reset() {
        heldTicks = 0;
        guiArmed = false;
        armedScreen = null;
        lastContext = Context.NONE;
        worldArmed = false;
    }
}
