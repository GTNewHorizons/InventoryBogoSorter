package net.blay09.mods.craftingtweaks.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

public class GuiTweakButton extends GuiImageButton implements ITooltipProvider {

    public enum TweakOption {
        Rotate,
        Balance,
        Clear
    }

    private final TweakOption tweakOption;
    private final int tweakId;
    private final GuiContainer parentGui;
    private int lastGuiLeft;
    private int lastGuiTop;

    public GuiTweakButton(GuiContainer parentGui, int xPosition, int yPosition, int texCoordX, int texCoordY,
        TweakOption tweakOption, int tweakId) {
        super(-1, xPosition, yPosition, texCoordX, texCoordY);
        this.parentGui = parentGui;
        this.tweakOption = tweakOption;
        this.tweakId = tweakId;
    }

    public TweakOption getTweakOption() {
        return tweakOption;
    }

    public int getTweakId() {
        return tweakId;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        int oldX = xPosition;
        int oldY = yPosition;
        // If parentGui is set, we only store the relative position in the button for mods that do hacky things where
        // guiLeft/guiTop constantly changes
        if (parentGui != null) {
            xPosition += lastGuiLeft;
            yPosition += lastGuiTop;
        }
        boolean result = super.mousePressed(mc, mouseX, mouseY);
        xPosition = oldX;
        yPosition = oldY;
        return result;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        int oldX = xPosition;
        int oldY = yPosition;
        // If parentGui is set, we only store the relative position in the button for mods that do hacky things where
        // guiLeft/guiTop constantly changes
        if (parentGui != null) {
            lastGuiLeft = parentGui.guiLeft;
            lastGuiTop = parentGui.guiTop;
            xPosition += lastGuiLeft;
            yPosition += lastGuiTop;
        }
        int oldTexCoordX = texCoordX;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            texCoordX += 48;
        }
        super.drawButton(mc, mouseX, mouseY);
        texCoordX = oldTexCoordX;
        xPosition = oldX;
        yPosition = oldY;
    }

    @Override
    public void addInformation(List<String> tooltip) {
        switch (tweakOption) {
            case Rotate:
                tooltip.add(I18n.format("tooltip.craftingtweaks.rotate"));
                break;
            case Clear:
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    tooltip.add(I18n.format("tooltip.craftingtweaks.forceClear"));
                    tooltip.add("\u00a77" + I18n.format("tooltip.craftingtweaks.forceClearInfo"));
                } else {
                    tooltip.add(I18n.format("tooltip.craftingtweaks.clear"));
                }
                break;
            case Balance:
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    tooltip.add(I18n.format("tooltip.craftingtweaks.spread"));
                } else {
                    tooltip.add(I18n.format("tooltip.craftingtweaks.balance"));
                }
                break;
        }
    }
}
