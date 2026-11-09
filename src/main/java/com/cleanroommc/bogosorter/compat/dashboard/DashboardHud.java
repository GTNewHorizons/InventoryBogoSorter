package com.cleanroommc.bogosorter.compat.dashboard;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.client.usageticker.UsageTicker;
import com.cleanroommc.bogosorter.compat.Mods;
import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;
import com.gtnewhorizons.dashboard.api.HudShift;

/**
 * Lets players move, resize and hide the HUD in Dashboard's editor. The only class using Dashboard, so only call it
 * while Dashboard is loaded.
 */
public final class DashboardHud {

    private static final int ITEM_SIZE = 16;
    /** The usage ticker row reaches 219 pixels left of the screen middle, its right end 97. */
    private static final int ROW_LEFT = 219;
    private static final int ROW_WIDTH = ROW_LEFT - 97;
    /** Raised this high once fully slid in. */
    private static final int RAISED = 19;
    private static final int BACKHAND_SHIFT = 30;
    private static final int SAMPLE_HELMET_X = 66;
    private static final int SAMPLE_HELD_X = 104;

    private static final HudElement USAGE_TICKER = new HudElement(
        "bogosorter:usage_ticker",
        (screenWidth, screenHeight) -> new HudBounds(
            screenWidth / 2 - (Mods.Backhand.isLoaded() ? BACKHAND_SHIFT : 0) - ROW_LEFT,
            screenHeight - RAISED,
            ROW_WIDTH,
            ITEM_SIZE));

    private static final String HOTBAR_ID = "minecraft:hotbar";
    private static final HudShift HOTBAR_SHIFT = new HudShift();

    private static final ItemStack SAMPLE_HELMET = new ItemStack(Items.iron_helmet);
    private static final ItemStack SAMPLE_HELD = new ItemStack(Blocks.cobblestone, 48);

    private DashboardHud() {}

    public static void register() {
        USAGE_TICKER.setPreview((screenWidth, screenHeight) -> HudRegistry.render(USAGE_TICKER, () -> {
            HudBounds bounds = USAGE_TICKER.getDefaultBounds(screenWidth, screenHeight);
            UsageTicker.drawStack(SAMPLE_HELMET, bounds.x + SAMPLE_HELMET_X, bounds.y);
            UsageTicker.drawStack(SAMPLE_HELD, bounds.x + SAMPLE_HELD_X, bounds.y);
        }));
        HudRegistry.register(USAGE_TICKER);
    }

    /** False means the player hid it, so skip drawing and do not call {@link #endUsageTicker()}. */
    public static boolean beginUsageTicker(ScaledResolution resolution) {
        return HudRegistry.beginRender(USAGE_TICKER, resolution);
    }

    public static void endUsageTicker() {
        HudRegistry.endRender();
    }

    /**
     * Moves the hotbar swap column with the hotbar, since it sits above the selected slot. False means the player hid
     * the hotbar, so skip drawing. Call {@link #endHotbarSwap()} after drawing either way.
     */
    public static boolean beginHotbarSwap(ScaledResolution resolution) {
        HudElement hotbar = HudRegistry.getElement(HOTBAR_ID);
        return hotbar == null || HOTBAR_SHIFT.begin(hotbar, resolution);
    }

    public static void endHotbarSwap() {
        HOTBAR_SHIFT.end();
    }
}
