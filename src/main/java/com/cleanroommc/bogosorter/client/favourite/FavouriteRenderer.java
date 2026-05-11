package com.cleanroommc.bogosorter.client.favourite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.bogosorter.common.favourite.FavouriteHelper;
import com.cleanroommc.modularui.drawable.GuiDraw;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Highlights favourited stacks with a 1px coloured outline drawn just outside the
 * slot frame (gold for the local player, coral for other players' favourites) and
 * appends a "Favourite Item" line to the item's tooltip.
 *
 * The in-GUI outline pass is invoked from {@code GuiContainerFavouriteMixin}, which
 * injects right before vanilla's {@code renderToolTip} call inside
 * {@code GuiContainer.drawScreen}. Painting there lets vanilla's tooltip render
 * naturally on top of our outline without any re-render hacks.
 */
@SideOnly(Side.CLIENT)
public class FavouriteRenderer {

    private static final int COL_OWNED = 0xFFFFCC33; // warm gold
    private static final int COL_OTHER = 0xFFE03B3B; // coral red

    @SubscribeEvent
    public void onHotbarRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        ScaledResolution res = event.resolution;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            int tint = colourFor(stack);
            if (tint == 0) continue;

            int x = res.getScaledWidth() / 2 - 90 + slot * 20 + 2;
            int y = res.getScaledHeight() - 20 + 2;
            drawSlotOutline(x, y, tint);
        }
    }

    /**
     * Appends a "Favourited" line to favourited stacks. The hook fires for every
     * tooltip render (inventories, JEI/NEI lookups, etc.), so the indicator follows
     * the stack everywhere it shows up, not just inside our outline overlay.
     */
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.itemStack;
        if (!FavouriteHelper.isFavourite(stack)) return;
        if (FavouriteHelper.isOwnedByLocalPlayer(stack)) {
            event.toolTip.add(EnumChatFormatting.GOLD + "Favourited");
        } else {
            String name = FavouriteHelper.getOwnerName(stack);
            String prefix = (name == null || name.isEmpty()) ? "Someone" : name;
            event.toolTip.add(EnumChatFormatting.RED + prefix + " Favourited");
        }
    }

    /**
     * Called by {@link com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerFavouriteMixin}
     * from inside GuiContainer.drawScreen, after drawGuiContainerForegroundLayer. At that
     * point the GL matrix is still translated by (guiLeft, guiTop), so slots draw using
     * their raw xDisplayPosition/yDisplayPosition without any extra offset.
     */
    public static void drawContainerOutlines(GuiContainer container) {
        for (Object o : container.inventorySlots.inventorySlots) {
            if (!(o instanceof Slot)) continue;
            Slot s = (Slot) o;
            int tint = colourFor(s.getStack());
            if (tint == 0) continue;

            drawSlotOutline(s.xDisplayPosition, s.yDisplayPosition, tint);
        }
    }

    /**
     * Draws a 1px outline hugging the outside of the slot's 16x16 frame. Putting
     * the line outside the frame keeps it clear of the item icon and the stack-count
     * numerals.
     *
     * GuiDraw's untextured rect path disables GL_TEXTURE_2D and tints with glColor4f;
     * neither gets reset, so the next textured draw (e.g. the XP bar a few frames
     * later) would render as a flat coloured smear. Reset both before we return.
     */
    private static void drawSlotOutline(int x, int y, int colourARGB) {
        GuiDraw.drawBorderOutsideXYWH(x, y, 16, 16, colourARGB);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    /** Returns 0 (no outline), {@link #COL_OWNED}, or {@link #COL_OTHER}. */
    private static int colourFor(ItemStack stack) {
        if (!FavouriteHelper.isFavourite(stack)) return 0;
        return FavouriteHelper.isOwnedByLocalPlayer(stack) ? COL_OWNED : COL_OTHER;
    }
}
