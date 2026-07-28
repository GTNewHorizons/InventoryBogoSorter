package com.cleanroommc.bogosorter.common.config;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.dropoff.CoinDepositDestination;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = BogoSorter.ID)
public class BogoSorterConfig {

    @Config.Comment("DropOff Configuration")
    public static final DropOff dropOff = new DropOff();

    @Config.Comment("Drop Key Repeat Configuration")
    public static final DropKeyRepeat dropKeyRepeat = new DropKeyRepeat();

    @Config.Comment("Usage Ticker Configuration")
    public static final UsageTicker usageTicker = new UsageTicker();

    @Config.Comment("AE2 and Thaumic tooltip/search integration.")
    public static final Ae2Integration ae2Integration = new Ae2Integration();

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

    @Config.DefaultBoolean(false)
    @Config.Comment("Allow auto-refill to take items from pinned player inventory slots.")
    @Config.LangKey("bogosorter.config.autorefill.from_pinned")
    public static boolean autoRefillFromPinnedSlots;

    @Config.DefaultEnum("STAR_1")
    @Config.Comment("Texture set used to mark pinned inventory slots.")
    @Config.LangKey("bogosorter.config.pinned_slots.style")
    public static PinnedSlotStyle pinnedSlotStyle = PinnedSlotStyle.STAR_1;

    @Config.DefaultBoolean(true)
    @Config.Comment("Render the icon over pinned inventory slots. The outline is always shown.")
    @Config.LangKey("bogosorter.config.pinned_slots.show_icon")
    public static boolean showPinnedSlotIcon;

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

    @Config.DefaultBoolean(false)
    @Config.Comment({ "Enable the debug clear/randomize inventory tools (numpad 1/2 in a container GUI).",
        "These overwrite inventory contents, so they are restricted to server operators even when enabled." })
    @Config.LangKey("bogosorter.config.debug_tools.enable")
    @Config.Sync
    public static boolean enableDebugTools;

    public enum PinnedSlotStyle {

        STAR_0("star", 0, "star_outline.png"),
        STAR_1("star", 1, "star_outline.png"),
        STAR_2("star", 2, "star_outline.png"),
        DOT_0("dot", 0, "dot_outline.png"),
        DOT_1("dot", 1, "dot_outline.png"),
        DOT_2("dot", 2, "dot_outline.png"),
        LOCK_0("lock", 0, "lock_outline.png"),
        LOCK_1("lock", 1, "lock_outline.png"),
        LOCK_2("lock", 2, "lock_outline.png");

        private final String iconTexturePath;
        private final String outlineTexturePath;

        PinnedSlotStyle(String shape, int variant, String outlineFile) {
            String folder = "textures/gui/pins/" + shape + "/";
            this.iconTexturePath = folder + shape + "_" + variant + ".png";
            this.outlineTexturePath = folder + outlineFile;
        }

        public String getTexturePath(String part) {
            return "outline".equals(part) ? outlineTexturePath : iconTexturePath;
        }

        public String getLangKey() {
            return "bogosort.gui.pinned_slots.style." + name().toLowerCase();
        }
    }

    @Config.LangKey("bogosorter.config.dropkeyrepeat")
    public static class DropKeyRepeat {

        @Config.DefaultBoolean(true)
        @Config.Comment("Hold the vanilla drop key to keep dropping items (world and container GUIs).")
        @Config.LangKey("bogosorter.config.dropkeyrepeat.enable")
        public boolean enableDropKeyRepeat;

        @Config.DefaultInt(5)
        @Config.RangeInt(min = 0, max = 100)
        @Config.Comment("Ticks the drop key must stay held before the first repeat.")
        @Config.LangKey("bogosorter.config.dropkeyrepeat.initial_delay")
        public int initialDelayTicks;

        @Config.DefaultInt(1)
        @Config.RangeInt(min = 1, max = 100)
        @Config.Comment("Ticks between repeated drops while the key is held.")
        @Config.LangKey("bogosorter.config.dropkeyrepeat.interval")
        public int repeatIntervalTicks;
    }

    @Config.LangKey("bogosorter.config.ae2")
    public static class Ae2Integration {

        @Config.DefaultBoolean(true)
        @Config.Comment("Enable AE2 item/fluid amount lines in NEI and inventory tooltips.")
        @Config.LangKey("bogosorter.config.ae2.amount_tooltips")
        public boolean enableAmountTooltips;

        @Config.DefaultBoolean(true)
        @Config.Comment("Enable Thaumic Energistics essentia amount lookups in AE2 amount tooltips.")
        @Config.LangKey("bogosorter.config.ae2.thaumic_essentia")
        public boolean enableThaumicEssentia;

        @Config.DefaultBoolean(true)
        @Config.Comment("Enable searching an AE2 terminal from the configured hover-search key.")
        @Config.LangKey("bogosorter.config.ae2.terminal_search")
        public boolean enableTerminalHoverSearch;

        @Config.DefaultBoolean(false)
        @Config.Comment("Log optional AE2/NEI/Thaumic integration diagnostics.")
        @Config.LangKey("bogosorter.config.ae2.debug_logging")
        public boolean enableIntegrationDebugLogging;
    }

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

        @Config.DefaultBoolean(false)
        @Config.Comment("Enable drop-off from the hotbar.")
        @Config.LangKey("bogosorter.config.dropoff.hotbar_enable")
        public boolean enableHotbarDropOff;

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

        @Config.DefaultEnum("PERSONAL")
        @Config.Comment("Wallet used when dropping coins into a nearby Vending Machine.")
        @Config.LangKey("bogosorter.config.dropoff.coin_destination")
        @Config.RequiresMod("vendingmachine")
        public CoinDepositDestination coinDepositDestination = CoinDepositDestination.PERSONAL;

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
