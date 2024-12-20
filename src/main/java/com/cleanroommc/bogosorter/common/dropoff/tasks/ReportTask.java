package com.cleanroommc.bogosorter.common.dropoff.tasks;

import com.cleanroommc.bogosorter.common.McUtils;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.gtnewhorizon.gtnhlib.util.AboveHotbarHUD;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ReportTask implements Runnable {

    private final int itemsCounter;
    private final int affectedContainers;
    private final int totalContainers;
    private final List<RendererCubeTarget> rendererCubeTargets;

    public ReportTask(int itemsCounter, int affectedContainers, int totalContainers,
                      List<RendererCubeTarget> rendererCubeTargets) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
    }

    @Override
    public void run() {
        //if (DropOffConfig.INSTANCE.highlightContainers) {
        RendererCube.INSTANCE.draw(rendererCubeTargets);
        //}


        String message = String.valueOf(EnumChatFormatting.RED) +
            itemsCounter +
            EnumChatFormatting.RESET +
            " items moved to " +
            EnumChatFormatting.RED +
            affectedContainers +
            EnumChatFormatting.RESET +
            " containers of " +
            EnumChatFormatting.RED +
            totalContainers +
            EnumChatFormatting.RESET +
            " checked in total.";

        AboveHotbarHUD.renderTextAboveHotbar(message, 60, false, false);

        McUtils.playSound("gui.button.press");
    }


}
