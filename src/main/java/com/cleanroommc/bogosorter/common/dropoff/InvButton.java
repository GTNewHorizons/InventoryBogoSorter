package com.cleanroommc.bogosorter.common.dropoff;

import com.cleanroommc.bogosorter.common.network.CDropOff;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import cpw.mods.fml.client.config.GuiButtonExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.Objects;

public class InvButton extends GuiButtonExt {
    public InvButton(GuiContainer parentGui) {
        super(394658248, parentGui.guiLeft + 75, parentGui.guiTop + 7, 11, 11, "d");
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)){
            NetworkHandler.sendToServer(new CDropOff());
            return true;
        }

        return false;
    }

    @Override
    public void func_146113_a(SoundHandler soundHandlerIn) {
        // dont play click sound
    }

    public void drawTooltip(int mouseX, int mouseY) {
        if (this.enabled && this.field_146123_n) {
            GuiScreen guiScreen = Objects.requireNonNull(Minecraft.getMinecraft().currentScreen);
            guiScreen.func_146283_a(Collections.singletonList(I18n.format("key.dropoff")),mouseX, mouseY);
        }
    }
}
