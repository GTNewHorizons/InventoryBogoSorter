package com.cleanroommc.bogosorter.client.ae2;

import java.util.List;

import com.cleanroommc.bogosorter.BogoSorter;
import com.github.bsideup.jabel.Desugar;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Ae2ClientBridge {

    private static Handler handler = Handler.NOOP;
    private static boolean serverAmountTooltipsAllowed = true;
    private static boolean serverThaumicAllowed = true;

    private Ae2ClientBridge() {}

    public static void register(Handler newHandler) {
        handler = newHandler == null ? Handler.NOOP : newHandler;
        handler.setServerFeatures(serverAmountTooltipsAllowed, serverThaumicAllowed);
    }

    public static void handleBatchResponse(int contextStatus, List<Response> responses) {
        handler.handleBatchResponse(contextStatus, responses);
    }

    public static void setServerFeatures(boolean amountTooltipsAllowed, boolean thaumicAllowed) {
        serverAmountTooltipsAllowed = amountTooltipsAllowed;
        serverThaumicAllowed = thaumicAllowed;
        handler.setServerFeatures(amountTooltipsAllowed, thaumicAllowed);
    }

    public static void resetConnectionState() {
        serverAmountTooltipsAllowed = true;
        serverThaumicAllowed = true;
        handler.setServerFeatures(true, true);
        handler.reset();
    }

    public static void initializeOptionalNeiIntegration() {
        try {
            Class<?> integration = Class.forName("com.cleanroommc.bogosorter.compat.nei.Ae2TooltipClient");
            integration.getMethod("init")
                .invoke(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            BogoSorter.LOGGER.error("Failed to initialize the optional NEI AE2 integration", e);
        }
    }

    public interface Handler {

        Handler NOOP = new Handler() {

            @Override
            public void handleBatchResponse(int contextStatus, List<Response> responses) {}

            @Override
            public void setServerFeatures(boolean amountTooltipsAllowed, boolean thaumicAllowed) {}

            @Override
            public void reset() {}
        };

        void handleBatchResponse(int contextStatus, List<Response> responses);

        void setServerFeatures(boolean amountTooltipsAllowed, boolean thaumicAllowed);

        void reset();
    }

    @Desugar
    public record Response(int requestId, int status, long amount, int retryAfterMs) {

    }

}
