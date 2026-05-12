package com.cleanroommc.bogosorter.client.favourite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.favourite.FavouriteHelper;
import com.cleanroommc.modularui.drawable.UITexture;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Highlights favourited stacks with a coloured frame drawn around the slot
 * (gold for the local player, red for other players' favourites) and appends
 * a "Favourited" line to the item's tooltip.
 *
 * The frame texture is a stacked 18x36 PNG with both colours baked in, picked
 * via subAreaXYWH instead of GL-tinting a white sprite, so no GL state has to
 * be touched at draw time.
 *
 * The in-GUI overlay pass is invoked from {@code GuiContainerFavouriteMixin},
 * which injects right after {@code drawGuiContainerForegroundLayer} inside
 * {@code GuiContainer.drawScreen}. Painting there lets vanilla's tooltip
 * render naturally on top of the overlay without any re-render hacks.
 */
@SideOnly(Side.CLIENT)
public class FavouriteRenderer {

    private static final UITexture OVERLAY_OWNED = UITexture.builder()
        .location(BogoSorter.ID, "gui/favourite_overlay")
        .imageSize(18, 36)
        .subAreaXYWH(0, 0, 18, 18)
        .build();

    private static final UITexture OVERLAY_OTHER = UITexture.builder()
        .location(BogoSorter.ID, "gui/favourite_overlay")
        .imageSize(18, 36)
        .subAreaXYWH(0, 18, 18, 18)
        .build();

    @SubscribeEvent
    public void onHotbarRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        ScaledResolution res = event.resolution;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            UITexture overlay = overlayFor(stack);
            if (overlay == null) continue;

            int x = res.getScaledWidth() / 2 - 90 + slot * 20 + 2;
            int y = res.getScaledHeight() - 20 + 1;
            drawSlotOverlay(x, y, overlay);
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
        String reset = EnumChatFormatting.RESET.toString();
        if (FavouriteHelper.isOwnedByLocalPlayer(stack)) {
            event.toolTip.add(EnumChatFormatting.GOLD + I18n.format("bogosort.tooltip.favourited") + reset);
        } else {
            String name = FavouriteHelper.getOwnerName(stack);
            String prefix = (name == null || name.isEmpty()) ? I18n.format("bogosort.tooltip.favourited_by_unknown")
                : name;
            event.toolTip.add(EnumChatFormatting.RED + I18n.format("bogosort.tooltip.favourited_by", prefix) + reset);
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
            UITexture overlay = overlayFor(s.getStack());
            if (overlay == null) continue;

            drawSlotOverlay(s.xDisplayPosition, s.yDisplayPosition, overlay);
        }
    }

    /**
     * Draws an 18x18 frame just outside the slot. Texture is pre-coloured, so no
     * Color.setGlColor / reset wrapper is needed.
     */
    private static void drawSlotOverlay(int x, int y, UITexture overlay) {
        overlay.draw(x - 1, y - 1, 18, 18);
    }

    /** Returns null (no overlay), {@link #OVERLAY_OWNED}, or {@link #OVERLAY_OTHER}. */
    private static UITexture overlayFor(ItemStack stack) {
        if (!FavouriteHelper.isFavourite(stack)) return null;
        return FavouriteHelper.isOwnedByLocalPlayer(stack) ? OVERLAY_OWNED : OVERLAY_OTHER;
    }
}
