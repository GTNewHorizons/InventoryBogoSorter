package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
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

    @Config.DefaultBoolean(false)
    @Config.Comment("Allow player hotbar to be sorted.")
    @Config.LangKey("bogosorter.config.hotbarsort.enable")
    public static boolean enableHotbarSort;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the auto-refill feature (Client Side Toggle).")
    @Config.LangKey("bogosorter.config.autorefill.enable")
    public static boolean enableAutoRefill;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the auto-refill feature. (Server Side Toggle)")
    @Config.LangKey("bogosorter.config.autorefill.enable_server")
    @Config.Sync
    public static boolean enableAutoRefill_server;

    @Config.DefaultInt(1)
    @Config.Comment("The damage threshold for auto-refill. If the item has less than this amount of durability, it will be refilled.")
    @Config.LangKey("bogosorter.config.autorefill.damage_threshold")
    public static int autoRefillDamageThreshold;

    @Config.DefaultBoolean(true)
    @Config.Comment({ "If enabled, items with max stack size of 1 (e.g., tools, armor, etc.)",
        "will not be split when sorting. This helps avoid cluttering the inventory with duplicate single-item stacks." })
    @Config.LangKey("bogosorter.config.preventSplit")
    @Config.Sync
    public static boolean preventSplit;

    @Config.DefaultInt(0xFFFFFFFF)
    @Config.Comment({ "The color of the sort button.",
        "Display format: 0xAARRGGBB (e.g. 0xFFFFFFFF for white, 0xFF0000FF for red).",
        "Value is displayed in decimal here but interpreted as hex internally." })
    @Config.LangKey("bogosorter.config.button.color")
    public static int buttonColor;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the sort button in the player inventory.")
    @Config.LangKey("bogosorter.config.button.enable")
    public static boolean buttonEnabled;

    @Config.DefaultBoolean(true)
    @Config.Comment("Enable the hotbar swap feature.")
    @Config.LangKey("bogosorter.config.hotbarswap.enable")
    public static boolean enableHotbarSwap;

    @Config.LangKey("bogosorter.config.usage_ticker")
    public static class UsageTicker {

        @Config.Comment("Arrow Configuration")
        @Config.LangKey("bogosorter.config.usage_ticker.arrow")
        public final Arrow arrow = new Arrow();

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

        public static class Arrow {

            @Config.DefaultBoolean(true)
            @Config.Comment("Show usage ticker for arrow.")
            @Config.LangKey("bogosorter.config.usage_ticker.arrow.enable")
            public boolean enableArrow;

            @Config.DefaultStringList({ "Thaumcraft:PrimalArrow", "etfuturum:tipped_arrow" })
            @Config.Comment("List of item IDs to consider as valid arrows for the usage ticker. The order matters; the first match is used.")
            @Config.LangKey("bogosorter.config.usage_ticker.arrow.arrowItems")
            public String[] arrowItems;

            @Config.DefaultStringList({ "minecraft:bow", "Botania:crystalBow", "Botania:livingwoodBow",
                "DraconicEvolution:draconicBow", "DraconicEvolution:wyvernBow", "BloodArsenal:bound_bow",
                "EnderZoo:guardiansBow", "GalaxySpace:item.QuantBow", "Natura:natura.bow.ghostwood",
                "Natura:natura.bow.bloodwood", "Natura:natura.bow.darkwood", "Natura:natura.bow.fusewood",
                "battlegear2:bow.iron", "battlegear2:bow.diamond", "Thaumcraft:ItemBowBone",
                "TwilightForest:item.tripleBow", "TwilightForest:item.seekerBow", "TwilightForest:item.iceBow",
                "TwilightForest:item.enderBow" })
            @Config.Comment("List of bow item IDs to enable arrow ticker for. Add modded bows here.")
            @Config.LangKey("bogosorter.config.usage_ticker.arrow.bowItems")
            public String[] bowItems;

        }
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

        @Config.Comment("The style of the render highlight (CUBE or BRACE).")
        @Config.LangKey("bogosorter.config.dropoff.render_style")
        public RendererCube.RenderStyle dropoffRenderStyle = RendererCube.RenderStyle.CUBE;

        @Config.DefaultBoolean(true)
        @Config.Comment("Enable fading out animation for the highlight.")
        @Config.LangKey("bogosorter.config.dropoff.render_fade_out")
        public boolean dropoffRenderFadeOut;

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

        @Config.DefaultStringList({ "Chest", "Barrel", "Drawer", "Crate", "Present", "Cabinet", "Counter", "Fridge",
            "Filing", "Compartment", "Shulker" })
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
}
