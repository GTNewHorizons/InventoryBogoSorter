package com.cleanroommc.bogosorter.compat.screen;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WarningScreen extends GuiScreen {

    public static boolean wasOpened = false;

    public final List<String> messages;
    private int textHeight;

    public WarningScreen(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int i = this.height / 2 - this.textHeight / 2;
        if (this.messages != null) {
            for (String s : this.messages) {
                this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
                i += this.fontRendererObj.FONT_HEIGHT;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    public void initGui() {
        this.textHeight = this.messages.size() * this.fontRendererObj.FONT_HEIGHT;
        this.buttonList.add(
            new GuiButton(
                0,
                this.width / 2 - 100,
                Math.min(this.height / 2 + this.textHeight / 2 + this.fontRendererObj.FONT_HEIGHT, this.height - 30),
                I18n.format("gui.toMenu")));
    }
}
