package com.cleanroommc.bogosorter.common.dropoff;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.network.CDropOff;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.utils.Color;

public class DropOffInvButton extends GuiButton {

    private final GuiContainer parent;
    private boolean hold = false;
    private static long timeDropoff = 0;
    public static final UITexture BUTTON_BACKGROUND = UITexture.builder()
        .location(BogoSorter.ID, "gui/base_button")
        .imageSize(18, 18)
        .adaptable(1)
        .build();

    public DropOffInvButton(GuiContainer parentGui) {
        super(
            394658248,
            parentGui.guiLeft + DropOffButtonHandler.buttonX,
            parentGui.guiTop + DropOffButtonHandler.buttonY,
            10,
            10,
            "d");
        parent = parentGui;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible && this.enabled) {
            if (hold) {
                drag(mouseX, mouseY);
            }

            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;

            Color.setGlColor(BogoSorterConfig.buttonColor);
            BUTTON_BACKGROUND.draw(this.xPosition, this.yPosition, this.width, this.height);
            Color.resetGlColor();
            int color = 14737632;

            if (packedFGColour != 0) {
                color = packedFGColour;
            } else if (!this.enabled) {
                color = 10526880;
            } else if (this.field_146123_n) {
                color = 16777120;
            }
            this.drawCenteredString(
                mc.fontRenderer,
                this.displayString,
                this.xPosition + this.width / 2,
                this.yPosition + 1,
                color);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            if (GuiScreen.isCtrlKeyDown() && isMouseOver(mouseX, mouseY)) {
                hold = true;
            } else {
                long t = Minecraft.getSystemTime();
                if (t - timeDropoff > DropOffHandler.dropoffPacketThrottleInMS) {
                    NetworkHandler.sendToServer(new CDropOff());
                    timeDropoff = t;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (hold) {
            hold = false;
            DropOffButtonHandler.buttonX = xPosition - parent.guiLeft;
            DropOffButtonHandler.buttonY = yPosition - parent.guiTop;
        }
        super.mouseReleased(mouseX, mouseY);
    }

    private void drag(int mouseX, int mouseY) {
        xPosition = mouseX - (this.width / 2);
        yPosition = mouseY - (this.height / 2);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.xPosition && mouseY >= this.yPosition
            && mouseX < this.xPosition + this.width
            && mouseY < this.yPosition + this.height;
    }

    @Override
    public void func_146113_a(SoundHandler soundHandlerIn) {
        // dont play click sound
    }

    private static final List<String> TOOLTIP = Arrays.asList(
        I18n.format("key.dropoff")
            .split(System.lineSeparator()));

    public void drawTooltip(int mouseX, int mouseY) {
        if (this.enabled && this.field_146123_n) {
            GuiScreen guiScreen = Objects.requireNonNull(Minecraft.getMinecraft().currentScreen);
            guiScreen.func_146283_a(TOOLTIP, mouseX, mouseY);
        }
    }
}
