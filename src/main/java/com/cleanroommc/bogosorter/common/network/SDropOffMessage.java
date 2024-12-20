package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.cleanroommc.bogosorter.common.dropoff.tasks.ReportTask;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SDropOffMessage implements IPacket {

    private int itemsCounter;
    private int affectedContainers;
    private int totalContainers;
    private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();

    public SDropOffMessage() {
    }

    public SDropOffMessage(int itemsCounter, int affectedContainers, int totalContainers,
                           List<RendererCubeTarget> rendererCubeTargets) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(itemsCounter);
        buf.writeInt(affectedContainers);
        buf.writeInt(totalContainers);

        buf.writeInt(rendererCubeTargets.size());
        for (RendererCubeTarget target : rendererCubeTargets){
            buf.writeInt(target.getBlockPos().getX());
            buf.writeInt(target.getBlockPos().getY());
            buf.writeInt(target.getBlockPos().getZ());

            buf.writeInt(target.getColor().getRed());
            buf.writeInt(target.getColor().getGreen());
            buf.writeInt(target.getColor().getBlue());
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        itemsCounter = buf.readInt();
        affectedContainers = buf.readInt();
        totalContainers = buf.readInt();

        int targetsLen = buf.readInt();
        for (int i = 0; i < targetsLen; i++) {
            rendererCubeTargets.add(new RendererCubeTarget(
                new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()),
                new Color(buf.readInt(), buf.readInt(), buf.readInt())
            ));
        }
    }

    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        ReportTask reportTask = new ReportTask(itemsCounter, affectedContainers, totalContainers, rendererCubeTargets);

        reportTask.run();

        return null;
    }
}
