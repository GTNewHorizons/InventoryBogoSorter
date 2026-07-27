package com.cleanroommc.bogosorter.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.PinnedSlots;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PinnedSlotClient extends Gui {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BogoSorter.ID, "textures/gui/pinned_slot.png");

    private static int mask;

    private PinnedSlotClient() {}

    public static void sync(int newWindowId, int newMask) {
        mask = newMask;
    }

    public static void clear() {
        mask = 0;
    }

    public static void draw(GuiContainer gui, Slot slot) {
        if (mask == 0) return;

        SlotAccessor slotAccessor = (SlotAccessor) slot;
        int index = slotAccessor.callGetSlotIndex();
        if (!PinnedSlots.isPinnable(slotAccessor) || (mask & 1 << index - PinnedSlots.FIRST_PLAYER_SLOT) == 0) return;

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TEXTURE);
        Gui.func_152125_a(slotAccessor.bogo$getX() - 1, slotAccessor.bogo$getY() - 1, 0, 0, 32, 32, 18, 18, 32, 32);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
