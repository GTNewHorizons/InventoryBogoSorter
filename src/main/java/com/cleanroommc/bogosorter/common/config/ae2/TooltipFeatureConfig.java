package com.cleanroommc.bogosorter.common.config.ae2;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

public final class TooltipFeatureConfig {

    private TooltipFeatureConfig() {}

    public static boolean isTooltipEnabled() {
        return isAmountTooltipEnabled();
    }

    public static boolean isAmountTooltipEnabled() {
        return BogoSorterConfig.ae2Integration.enableAmountTooltips;
    }

    public static boolean isThaumicEnabled() {
        return BogoSorterConfig.ae2Integration.enableThaumicEssentia;
    }

    public static boolean isTerminalHoverSearchEnabled() {
        return BogoSorterConfig.ae2Integration.enableTerminalHoverSearch;
    }

    public static boolean isDebugLoggingEnabled() {
        return BogoSorterConfig.ae2Integration.enableIntegrationDebugLogging;
    }

    public static void setTooltipEnabled(boolean enabled) {
        setAmountTooltipEnabled(enabled);
        setThaumicEnabled(enabled);
    }

    public static void setAmountTooltipEnabled(boolean enabled) {
        BogoSorterConfig.ae2Integration.enableAmountTooltips = enabled;
    }

    public static void setThaumicEnabled(boolean enabled) {
        BogoSorterConfig.ae2Integration.enableThaumicEssentia = enabled;
    }

    public static void save() {
        ConfigurationManager.save(BogoSorterConfig.class);
    }
}
