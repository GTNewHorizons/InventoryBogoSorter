package yalter.mousetweaks.config;

import static yalter.mousetweaks.Main.isLwjgl3Loaded;

import com.gtnewhorizon.gtnhlib.config.Config;

import yalter.mousetweaks.MouseTweaks;

@Config(modid = MouseTweaks.MOD_ID)
public class MTConfig {

    @Config.Comment("Like vanilla right click dragging, but dragging over a slot multiple times puts the item there multiple times.")
    @Config.Name("RMB tweak")
    public static boolean rmbTweak = true;

    @Config.Comment("Left click and drag with an item to \"left click\" items of the same type.")
    @Config.Name("LMB tweak with item")
    public static boolean lmbTweakWithItem = true;

    @Config.Comment("Hold shift, left click and drag without an item to \"shift left click\" items.")
    @Config.Name("LMB tweak without item")
    public static boolean lmbTweakWithoutItem = true;

    @Config.Comment("Scroll over items to move them between inventories.")
    @Config.Name("Wheel tweak")
    public static boolean wheelTweak = true;

    @Config.Comment("How to pick the source slot when pulling items via scrolling.")
    @Config.Name("Wheel tweak search order")
    public static WheelSearchOrder wheelSearchOrder = WheelSearchOrder.LAST_TO_FIRST;

    @Config.Comment("Inventory position aware means scroll up to push items from the bottom inventory and pull into the top inventory, and vice versa.")
    @Config.Name("Wheel tweak scroll direction")
    public static WheelScrollDirection wheelScrollDirection = WheelScrollDirection.NORMAL;

    @Config.Comment("This determines how many items are moved when you scroll. On some setups (notably macOS), scrolling the wheel with different speeds results in different distances scrolled per wheel \"bump\". To make those setups play nicely with Mouse Tweaks, set this option to \"Always exactly one item\".")
    @Config.Name("Scroll item scaling")
    public static ScrollItemScaling scrollItemScaling = ScrollItemScaling.PROPORTIONAL;

    /**
     * Config Enums
     */

    public enum ScrollItemScaling {

        PROPORTIONAL("Relative to scroll amount"),
        ALWAYS_ONE("Always exactly one item");

        public static final int scrollStep = 120;

        private final String id;

        ScrollItemScaling(String id) {
            this.id = id;
        }

        public String getValue() {
            return this.id;
        }

        public static ScrollItemScaling fromId(int ordinal) {
            return ordinal == 0 ? PROPORTIONAL : ALWAYS_ONE;
        }

        /**
         * scales the given scroll distance, resulting in the number of items to move, the sign representing the
         * direction
         */
        public int scale(int scrollDelta) {
            switch (this) {
                case PROPORTIONAL:
                    return !isLwjgl3Loaded() ? scrollDelta : Integer.signum(scrollDelta) * scrollStep;
                case ALWAYS_ONE:
                    return Integer.signum(scrollDelta) * scrollStep;
                default:
                    throw new AssertionError();
            }
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public enum WheelScrollDirection {

        NORMAL("Down to push, up to pull"),
        INVERTED("Up to push, down to pull"),
        INVENTORY_POSITION_AWARE("Inventory position aware"),
        INVENTORY_POSITION_AWARE_INVERTED("Inventory position aware, inverted");

        private final String id;
        private static final WheelScrollDirection[] values = WheelScrollDirection.values();

        WheelScrollDirection(String id) {
            this.id = id;
        }

        public String getValue() {
            return this.id;
        }

        public static WheelScrollDirection fromId(int ordinal) {
            switch (ordinal) {
                case 0:
                    return NORMAL;
                case 1:
                    return INVERTED;
                case 2:
                    return INVENTORY_POSITION_AWARE;
                default:
                    return INVENTORY_POSITION_AWARE_INVERTED;
            }
        }

        public boolean isInverted() {
            return this.ordinal() % 2 == 1;
        }

        public boolean isPositionAware() {
            return this.ordinal() > 1;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    public enum WheelSearchOrder {

        FIRST_TO_LAST("First to last"),
        LAST_TO_FIRST("Last to first");

        private final String id;

        WheelSearchOrder(String id) {
            this.id = id;
        }

        public String getValue() {
            return id;
        }

        public static WheelSearchOrder fromId(int ordinal) {
            return ordinal == 0 ? FIRST_TO_LAST : LAST_TO_FIRST;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }
}
