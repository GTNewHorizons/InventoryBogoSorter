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
 * The frame texture is a stacked 18x36 PNG with both colours baked in, with
 * each half exposed as its own UITexture sub-view via getSubArea so a single
 * source sprite covers both states.
 *
 * The in-GUI overlay pass is invoked from {@code GuiContainerFavouriteMixin},
 * which injects right after {@code drawGuiContainerForegroundLayer} inside
 * {@code GuiContainer.drawScreen}. Painting there lets vanilla's tooltip
 * render naturally on top of the overlay without any re-render hacks.
 */
@SideOnly(Side.CLIENT)
public class FavouriteRenderer {

    private static final UITexture OVERLAY_SHEET = UITexture.builder()
        .location(BogoSorter.ID, "gui/favourite_overlay")
        .imageSize(18, 36)
        .fullImage()
        .build();

    private static final UITexture OVERLAY_OWNED = OVERLAY_SHEET.getSubArea(0f, 0f, 1f, 0.5f);
    private static final UITexture OVERLAY_OTHER = OVERLAY_SHEET.getSubArea(0f, 0.5f, 1f, 1f);

    @SubscribeEvent
    public void onHotbarRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        ScaledResolution res = event.resolution;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            if (!FavouriteHelper.isFavourite(stack)) continue;

            int x = res.getScaledWidth() / 2 - 90 + slot * 20 + 2;
            int y = res.getScaledHeight() - 20 + 1;
            drawSlotOverlay(x, y, FavouriteHelper.isOwnedByLocalPlayer(stack));
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
            ItemStack stack = s.getStack();
            if (!FavouriteHelper.isFavourite(stack)) continue;

            drawSlotOverlay(s.xDisplayPosition, s.yDisplayPosition, FavouriteHelper.isOwnedByLocalPlayer(stack));
        }
    }

    /**
     * Draws an 18x18 frame just outside the slot, picking the gold or red sub-view
     * of the stacked texture. Texture is pre-coloured, so no Color.setGlColor / reset
     * wrapper is needed.
     */
    private static void drawSlotOverlay(int x, int y, boolean owned) {
        (owned ? OVERLAY_OWNED : OVERLAY_OTHER).draw(x - 1, y - 1, 18, 18);
    }
}
