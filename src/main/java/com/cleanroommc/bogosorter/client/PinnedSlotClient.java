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

    private static final ResourceLocation OUTLINE = new ResourceLocation(
        BogoSorter.ID,
        "textures/gui/pinned_slot_outline.png");
    private static final ResourceLocation ICON = new ResourceLocation(
        BogoSorter.ID,
        "textures/gui/pinned_slot_icon.png");

    private static int playerMask;
    private static int backpackWindowId = -1;
    private static int[] backpackMask = new int[0];

    private PinnedSlotClient() {}

    public static void sync(int newWindowId, int newPlayerMask, int[] newBackpackMask) {
        playerMask = newPlayerMask;
        backpackWindowId = newWindowId;
        backpackMask = newBackpackMask;
    }

    public static void clear() {
        playerMask = 0;
        clearContainer();
    }

    public static void clearContainer() {
        backpackWindowId = -1;
        backpackMask = new int[0];
    }

    public static void drawOutline(GuiContainer gui, Slot slot) {
        if (isPinned(gui, slot)) draw(OUTLINE, slot.xDisplayPosition - 1, slot.yDisplayPosition - 1, 18, false);
    }

    public static void drawIcon(GuiContainer gui, Slot slot) {
        if (isPinned(gui, slot)) draw(ICON, slot.xDisplayPosition, slot.yDisplayPosition, 16, true);
    }

    private static boolean isPinned(GuiContainer gui, Slot slot) {
        if (playerMask == 0 && backpackMask.length == 0) return false;

        SlotAccessor slotAccessor = (SlotAccessor) slot;
        int index = slotAccessor.callGetSlotIndex();
        boolean playerPinned = PinnedSlots.isPinnable(slotAccessor)
            && (playerMask & 1 << index - PinnedSlots.FIRST_PLAYER_SLOT) != 0;
        boolean backpackPinned = gui.inventorySlots.windowId == backpackWindowId
            && PinnedSlots.isBackpackPinned(backpackMask, gui.inventorySlots, slotAccessor);
        return playerPinned || backpackPinned;
    }

    private static void draw(ResourceLocation texture, int x, int y, int size, boolean aboveItem) {
        if (aboveItem) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glPushMatrix();
            GL11.glTranslatef(0, 0, 200);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        Gui.func_152125_a(x, y, 0, 0, 1, 1, size, size, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        if (aboveItem) {
            GL11.glPopMatrix();
        } else {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
    }
}
