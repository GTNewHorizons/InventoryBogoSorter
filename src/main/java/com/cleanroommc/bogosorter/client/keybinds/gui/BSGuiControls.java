package com.cleanroommc.bogosorter.client.keybinds.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A custom controls screen that displays and allows editing of multi-key combinations for BogoSorter keybinds.
 */
@SideOnly(Side.CLIENT)
public class BSGuiControls extends GuiScreen {

    private final GuiScreen parentScreen;
    protected String screenTitle = "BogoSorter Shortcut Controls";
    private BSGuiKeybindList keyBindingList;

    /**
     * This will track which keybind is currently being edited.
     */
    public BSKeybinds.KeybindDefinition selectedKeyBinding = null;

    public BSGuiControls(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        this.keyBindingList = new BSGuiKeybindList(this, this.mc);
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height - 29, I18n.format("gui.done")));
        this.buttonList
            .add(new GuiButton(201, this.width / 2 - 100, this.height - 52, I18n.format("controls.resetAll")));
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.selectedKeyBinding != null) {
            // We are in edit mode.
            // First, check if the click was on one of the list's buttons (which can stop edit mode).
            if (this.keyBindingList.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }

            // Second, check if the click was on a main screen button (Done/Reset All).
            for (Object buttonObj : this.buttonList) {
                GuiButton button = (GuiButton) buttonObj;
                if (button.mousePressed(this.mc, mouseX, mouseY)) {
                    // Let the default handler call actionPerformed for these buttons.
                    super.mouseClicked(mouseX, mouseY, mouseButton);
                    return;
                }
            }

            // If the click wasn't on any button, add the mouse key to the current combo.
            List<Integer> combo = BSKeybinds.getKeyCombo(this.selectedKeyBinding);
            int mouseKeyCode = mouseButton - 100; // Convert LWJGL index (0,1,2) to KeyBind format (-100,-99,-98)
            if (!combo.contains(mouseKeyCode)) {
                combo.add(mouseKeyCode);
            }
        } else {
            // Not in edit mode. Let the screen and list handle clicks normally.
            super.mouseClicked(mouseX, mouseY, mouseButton);
            this.keyBindingList.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (this.keyBindingList.mouseReleased(mouseX, mouseY, state)) {
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.keyBindingList.keyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) { // Done button
            if (this.selectedKeyBinding != null) {
                this.selectedKeyBinding = null;
            }
            BSKeybinds.saveKeyCombos();
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == 201) { // Reset All Keys button
            for (BSKeybinds.KeybindDefinition keybinding : BSKeybinds.getAllKeybinds()) {
                BSKeybinds.resetToDefault(keybinding);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.keyBindingList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 8, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
