package com.cleanroommc.bogosorter.client.keybinds.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;

public class BSGuiKeybindList extends GuiListExtended {

    private final BSGuiControls controlsScreen;
    private final Minecraft mc;
    private final IGuiListEntry[] listEntries;
    private final BSKeybinds.KeybindDefinition[] bogoSorterKeyBindings;
    private int maxListLabelWidth;

    public BSGuiKeybindList(BSGuiControls controls, Minecraft mc) {
        super(mc, controls.width, controls.height, 30, controls.height - 62, 20);
        this.controlsScreen = controls;
        this.mc = mc;
        this.bogoSorterKeyBindings = BSKeybinds.getAllKeybinds();
        this.listEntries = new IGuiListEntry[bogoSorterKeyBindings.length];

        for (int i = 0; i < bogoSorterKeyBindings.length; ++i) {
            BSKeybinds.KeybindDefinition keybinding = bogoSorterKeyBindings[i];
            int width = mc.fontRenderer.getStringWidth(keybinding.getDisplayName());
            if (width > this.maxListLabelWidth) {
                this.maxListLabelWidth = width;
            }
            this.listEntries[i] = new BSGuiKeybindList.KeyEntry(keybinding);
        }
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return this.listEntries[index];
    }

    @Override
    protected int getSize() {
        return this.listEntries.length;
    }

    @Override
    public int getListWidth() {
        return super.getListWidth() + 32;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        for (int i = 0; i < this.getSize(); ++i) {
            int entryTop = this.top + this.slotHeight * i - this.getAmountScrolled();
            int entryLeft = this.left;
            if (mouseY >= entryTop && mouseY <= entryTop + this.slotHeight) {
                if (this.getListEntry(i)
                    .mousePressed(i, mouseX, mouseY, mouseEvent, mouseX - entryLeft, mouseY - entryTop)) {
                    this.func_148143_b(false); // setEnabled(false)
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int mouseEvent) {
        for (int i = 0; i < this.getSize(); ++i) {
            int entryTop = this.top + this.slotHeight * i - this.getAmountScrolled();
            int entryLeft = this.left;
            this.getListEntry(i)
                .mouseReleased(i, mouseX, mouseY, mouseEvent, mouseX - entryLeft, mouseY - entryTop);
        }
        this.func_148143_b(true); // setEnabled(true)
        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (this.controlsScreen.selectedKeyBinding != null) {
            List<Integer> combo = BSKeybinds.getKeyCombo(this.controlsScreen.selectedKeyBinding);
            if (keyCode == Keyboard.KEY_ESCAPE) {
                BSKeybinds.resetToDefault(this.controlsScreen.selectedKeyBinding); // Revert changes on escape
                this.controlsScreen.selectedKeyBinding = null;
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (!combo.isEmpty()) {
                    combo.remove(combo.size() - 1);
                }
            } else if (keyCode != Keyboard.KEY_NONE) {
                if (!combo.contains(keyCode)) {
                    combo.add(keyCode);
                }
            }
            return true;
        }
        return false;
    }

    private class KeyEntry implements IGuiListEntry {

        private final BSKeybinds.KeybindDefinition keybinding;
        private final String keyDesc;
        private final GuiButton btnEdit;
        private final GuiButton btnDone;
        private final GuiButton btnReset;

        private KeyEntry(BSKeybinds.KeybindDefinition keybinding) {
            this.keybinding = keybinding;
            this.keyDesc = keybinding.getDisplayName();
            this.btnEdit = new GuiButton(0, 0, 0, 50, 20, "Edit");
            this.btnDone = new GuiButton(0, 0, 0, 50, 20, "Done");
            this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset"));
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator,
            int mouseX, int mouseY, boolean isSelected) {
            boolean isEditing = controlsScreen.selectedKeyBinding == this.keybinding;
            mc.fontRenderer.drawString(
                this.keyDesc,
                x + 70 - maxListLabelWidth,
                y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2,
                0xFFFFFF);

            String comboString = BSKeybinds.getComboDisplayString(this.keybinding);
            int stringWidth = mc.fontRenderer.getStringWidth(comboString);
            mc.fontRenderer.drawString(
                comboString,
                controlsScreen.width / 2 - stringWidth / 2,
                y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2,
                0xFFFFFF);

            int buttonX = x + 180;

            if (isEditing) {
                this.btnDone.xPosition = buttonX;
                this.btnDone.yPosition = y;
                this.btnDone.drawButton(mc, mouseX, mouseY);
            } else {
                this.btnEdit.xPosition = buttonX;
                this.btnEdit.yPosition = y;
                this.btnEdit.drawButton(mc, mouseX, mouseY);
            }

            this.btnReset.xPosition = buttonX + 55;
            this.btnReset.yPosition = y;
            this.btnReset.enabled = !BSKeybinds.isDefault(this.keybinding);
            this.btnReset.drawButton(mc, mouseX, mouseY);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX,
            int relativeY) {
            boolean isEditing = controlsScreen.selectedKeyBinding == this.keybinding;

            if (isEditing) {
                if (this.btnDone.mousePressed(mc, mouseX, mouseY)) {
                    controlsScreen.selectedKeyBinding = null;
                    return true;
                }
            } else {
                if (this.btnEdit.mousePressed(mc, mouseX, mouseY)) {
                    // Stop editing any other keybind before starting a new one.
                    if (controlsScreen.selectedKeyBinding != null) {
                        BSKeybinds.resetToDefault(controlsScreen.selectedKeyBinding);
                    }
                    controlsScreen.selectedKeyBinding = this.keybinding;
                    BSKeybinds.getKeyCombo(this.keybinding)
                        .clear();
                    return true;
                }
            }

            if (this.btnReset.mousePressed(mc, mouseX, mouseY)) {
                BSKeybinds.resetToDefault(this.keybinding);
                if (isEditing) {
                    controlsScreen.selectedKeyBinding = null;
                }
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnEdit.mouseReleased(x, y);
            this.btnDone.mouseReleased(x, y);
            this.btnReset.mouseReleased(x, y);
        }
    }
}
