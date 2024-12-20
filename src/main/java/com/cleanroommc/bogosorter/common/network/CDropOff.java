package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.dropoff.InteractionResult;
import com.cleanroommc.bogosorter.common.dropoff.InventoryData;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.cleanroommc.bogosorter.common.dropoff.tasks.MainTask;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CDropOff implements IPacket {

    public CDropOff() {
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {

    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {

    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        MainTask mainTask = new MainTask(handler.playerEntity);

        mainTask.run();

        List<InventoryData> inventoryDataList = mainTask.getInventoryDataList();
        List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
        int affectedContainers = 0;

        for (InventoryData inventoryData : inventoryDataList) {
            Color color;

            if (inventoryData.getInteractionResult() == InteractionResult.DROPOFF_SUCCESS) {
                ++affectedContainers;
                color = new Color(0, 255, 0);
            } else {
                color = new Color(255, 0, 0);
            }

            for (TileEntity entity : inventoryData.getEntities()) {
                BlockPos blockPos = new BlockPos(entity.xCoord, entity.yCoord, entity.zCoord);

                RendererCubeTarget rendererCubeTarget = new RendererCubeTarget(blockPos, color);

                rendererCubeTargets.add(rendererCubeTarget);
            }
        }

        return new SDropOffMessage(mainTask.getItemsCounter(), affectedContainers, inventoryDataList.size(), rendererCubeTargets);
    }

}
