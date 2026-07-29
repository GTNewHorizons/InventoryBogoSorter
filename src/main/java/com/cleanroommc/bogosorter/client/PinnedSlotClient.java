package com.cleanroommc.bogosorter.client;

import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.PinnedSlots;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig.PinnedSlotStyle;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PinnedSlotClient extends Gui {

    private static final ResourceLocation[] OUTLINES = createTextures(PinnedSlotStyle::getOutlinePath);
    private static final ResourceLocation[] ICONS = createTextures(PinnedSlotStyle::getIconPath);

    private static int playerMask;
    private static int backpackWindowId = -1;
    private static int[] backpackMask = new int[0];
    private static GuiContainer drawingGui;
    private static Slot drawingSlot;

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
        drawingGui = null;
        drawingSlot = null;
    }

    public static void toggle(GuiContainer gui, SlotAccessor slot) {
        if (PinnedSlots.isPinnable(slot)) {
            playerMask ^= 1 << slot.callGetSlotIndex() - PinnedSlots.FIRST_PLAYER_SLOT;
            return;
        }

        int index = PinnedSlots.getBackpackSlotIndex(gui.inventorySlots, slot);
        if (index < 0) return;
        if (backpackWindowId != gui.inventorySlots.windowId) {
            backpackWindowId = gui.inventorySlots.windowId;
            backpackMask = new int[0];
        }
        int word = index >>> 5;
        if (backpackMask.length <= word) backpackMask = Arrays.copyOf(backpackMask, word + 1);
        backpackMask[word] ^= 1 << (index & 31);
    }

    public static void beginSlot(GuiContainer gui, Slot slot) {
        drawingGui = null;
        drawingSlot = null;
        if (!isPinned(gui, slot)) return;
        draw(
            getTexture(OUTLINES, BogoSorterConfig.pinnedSlotStyle),
            slot.xDisplayPosition - 1,
            slot.yDisplayPosition - 1,
            18);
        if (!BogoSorterConfig.showPinnedSlotIcon) return;
        drawingGui = gui;
        drawingSlot = slot;
    }

    public static void drawIconAfterItem(int x, int y) {
        if (drawingSlot == null || drawingSlot.xDisplayPosition != x || drawingSlot.yDisplayPosition != y) return;
        draw(getTexture(ICONS, BogoSorterConfig.pinnedSlotStyle), x, y, 16);
        drawingGui = null;
        drawingSlot = null;
    }

    public static void endSlot(GuiContainer gui, Slot slot) {
        if (drawingGui != gui || drawingSlot != slot) return;
        draw(getTexture(ICONS, BogoSorterConfig.pinnedSlotStyle), slot.xDisplayPosition, slot.yDisplayPosition, 16);
        drawingGui = null;
        drawingSlot = null;
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

    private static ResourceLocation[] createTextures(Function<PinnedSlotStyle, String> pathGetter) {
        PinnedSlotStyle[] styles = PinnedSlotStyle.values();
        ResourceLocation[] textures = new ResourceLocation[styles.length];
        for (PinnedSlotStyle style : styles) {
            textures[style.ordinal()] = new ResourceLocation(BogoSorter.ID, pathGetter.apply(style));
        }
        return textures;
    }

    private static ResourceLocation getTexture(ResourceLocation[] textures, PinnedSlotStyle style) {
        return textures[style.ordinal()];
    }

    private static void draw(ResourceLocation texture, int x, int y, int size) {
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        Gui.func_152125_a(x, y, 0, 0, 1, 1, size, size, 1, 1);
        GL11.glPopAttrib();
    }
}
