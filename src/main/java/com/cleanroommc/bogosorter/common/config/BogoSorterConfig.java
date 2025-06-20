package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = BogoSorter.ID)
public class BogoSorterConfig {

    @Config.Comment("DropOff Configuration")
    public static final DropOff dropOff = new DropOff();

    @Config.Comment("Usage Ticker Configuration")
    public static final UsageTicker usageTicker = new UsageTicker();

    @Config.DefaultString("gui.button.press")
    @Config.Comment("Sound played when the sort button is pressed.")
    @Config.LangKey("bogosorter.config.sort.sound")
    public static String sortSound;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the hotbar sort feature.")
    @Config.LangKey("bogosorter.config.hotbarsort.enable")
    public static boolean enableHotbarSort;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the auto-refill feature.")
    @Config.LangKey("bogosorter.config.autorefill.enable")
    @Config.Sync
    public static boolean enableAutoRefill;

    @Config.DefaultInt(1)
    @Config.Comment("The damage threshold for auto-refill. If the item has less than this amount of durability, it will be refilled.")
    @Config.LangKey("bogosorter.config.autorefill.damage_threshold")
    @Config.Sync
    public static int autoRefillDamageThreshold;

    @Config.DefaultString("#FFFFFFFF")
    @Config.Comment("The color of the sort button in hex format (e.g. #FFFFFFFF or 0xFFFFFFFF).")
    @Config.LangKey("bogosorter.config.button.color")
    public static String buttonColor;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the hotbar swap feature.")
    @Config.LangKey("bogosorter.config.hotbarswap.enable")
    public static boolean enableHotbarSwap;

    @Config.LangKey("bogosorter.config.usage_ticker")
    public static class UsageTicker {

        @Config.DefaultBoolean(true)
        @Config.Comment("Enable usage ticker module.")
        @Config.LangKey("bogosorter.config.usage_ticker.enable")
        public boolean enableModule;

        @Config.DefaultBoolean(true)
        @Config.Comment("Show usage ticker for main hand.")
        @Config.LangKey("bogosorter.config.usage_ticker.mainhand")
        public boolean enableMainHand;

        @Config.DefaultBoolean(true)
        @Config.Comment("Show usage ticker for off hand.")
        @Config.LangKey("bogosorter.config.usage_ticker.offhand")
        public boolean enableOffHand;

        @Config.DefaultBoolean(true)
        @Config.Comment("Show usage ticker for armor.")
        @Config.LangKey("bogosorter.config.usage_ticker.armor")
        public boolean enableArmor;
    }

    @Config.LangKey("bogosorter.config.dropoff")
    public static class DropOff {

        @Config.Comment("DropOff Button Configuration")
        @Config.LangKey("bogosorter.config.dropoff.button")
        public final DropOffButton button = new DropOffButton();

        @Config.DefaultInt(4)
        @Config.Comment("The radius (in blocks) around the player to scan for drop-off targets.")
        @Config.LangKey("bogosorter.config.dropoff.scan_radius")
        @Config.Sync
        public int dropoffRadius;

        @Config.DefaultBoolean(true)
        @Config.Comment("Enable the drop-off button in the player inventory.")
        @Config.LangKey("bogosorter.config.dropoff.enable")
        public boolean enableDropOff;

        @Config.DefaultBoolean(true)
        @Config.Comment("Render a highlight on eligible drop-off containers.")
        @Config.LangKey("bogosorter.config.dropoff.render")
        public boolean dropoffRender;

        @Config.DefaultBoolean(true)
        @Config.Comment("Show a chat message after dropping off items.")
        @Config.LangKey("bogosorter.config.dropoff.chat_message")
        public boolean dropoffChatMessage;

        @Config.DefaultInt(1)
        @Config.Comment("Time quota for drop-off in milliseconds.")
        @Config.LangKey("bogosorter.config.dropoff.quota")
        public int dropoffQuotaInMS;

        @Config.DefaultInt(500)
        @Config.Comment("Throttle drop-off packets in milliseconds.")
        @Config.LangKey("bogosorter.config.dropoff.throttle")
        public int dropoffPacketThrottleInMS;

        @Config.DefaultStringList({ "Chest", "Barrel", "Drawer", "Crate" })
        @Config.Comment("Valid inventory names for drop-off targeting (substring match).")
        @Config.LangKey("bogosorter.config.dropoff.targets")
        public String[] dropoffTargetNames;

        public static class DropOffButton {

            @Config.DefaultInt(160)
            @Config.Comment("X position of the drop-off button in the player inventory.")
            @Config.LangKey("bogosorter.config.dropoff.button.x")
            public int buttonX;

            @Config.DefaultInt(5)
            @Config.Comment("Y position of the drop-off button in the player inventory.")
            @Config.LangKey("bogosorter.config.dropoff.button.y")
            public int buttonY;

            @Config.DefaultBoolean(true)
            @Config.Comment("Show the drop-off button in the player inventory.")
            @Config.LangKey("bogosorter.config.dropoff.button.visible")
            public boolean showButton;
        }
    }

    /**
     * Converts the button color from hex string to an integer.
     * The format is expected to be "#RRGGBBAA" or "0xAARRGGBB".
     * If the format is incorrect, it returns a fallback color (white).
     *
     * @return the button color as an integer.
     */
    public static int getButtonColor() {
        try {
            return (int) Long.parseLong(
                buttonColor.replace("#", "")
                    .replace("0x", ""),
                16);
        } catch (NumberFormatException e) {
            return 0xFFFAAFFF; // fallback white
        }
    }
}
