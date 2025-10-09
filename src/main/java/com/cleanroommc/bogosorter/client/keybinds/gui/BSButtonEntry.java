package com.cleanroommc.bogosorter.client.keybinds.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;

import com.blamejared.controlling.client.gui.GuiNewKeyBindingList;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiKeyBindingListKeyEntryAccessor;
import com.cleanroommc.bogosorter.mixins.late.controlling.GuiNewKeyBindingListKeyEntryAccessor;

/**
 * A custom list entry that renders as a button to open the BogoSorter controls screen.
 */
public class BSButtonEntry implements GuiListExtended.IGuiListEntry {

    private final Minecraft mc;
    private final GuiButton button;
    private final String keyDesc;
    private final int maxListLabelWidth;

    public BSButtonEntry(Minecraft mc, GuiListExtended.IGuiListEntry originalEntry, int maxListLabelWidth) {
        this.mc = mc;
        int y = Mods.Controlling.isLoaded() ? 20 : 0; // y position will be set during rendering
        this.button = new GuiButton(0, 0, 0, 136 + y, 20, "Configure Multi-Keybinds");
        KeyBinding keybinding = null;

        if (Mods.Controlling.isLoaded() && originalEntry instanceof GuiNewKeyBindingList.KeyEntry) {
            keybinding = ((GuiNewKeyBindingListKeyEntryAccessor) originalEntry).getKeybinding();
        } else if (originalEntry instanceof GuiKeyBindingList.KeyEntry) {
            keybinding = ((GuiKeyBindingListKeyEntryAccessor) originalEntry).getKeybinding();
        }

        if (keybinding == null) {
            System.err.println("Failed to get keybinding from list entry!");
            this.keyDesc = "ERROR";
        } else {
            this.keyDesc = I18n.format(keybinding.getKeyDescription());
        }
        this.maxListLabelWidth = maxListLabelWidth;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator,
        int mouseX, int mouseY, boolean isSelected) {
        // Manually draw the keybind description text, replicating the vanilla alignment logic.
        this.mc.fontRenderer.drawString(
            this.keyDesc,
            x + 90 - this.maxListLabelWidth,
            y + slotHeight / 2 - this.mc.fontRenderer.FONT_HEIGHT / 2,
            0xFFFFFF);

        // Draw our custom button on the right side, where the keybind buttons would normally be.
        this.button.xPosition = x + 105;
        this.button.yPosition = y;
        this.button.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        if (this.button.mousePressed(mc, mouseX, mouseY)) {
            // When clicked, open the custom controls screen.
            mc.displayGuiScreen(new BSGuiControls(mc.currentScreen));
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        this.button.mouseReleased(x, y);
    }
}
