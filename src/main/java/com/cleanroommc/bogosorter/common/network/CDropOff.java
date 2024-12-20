package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.dropoff.DropOffHandler;
import com.cleanroommc.bogosorter.common.dropoff.InteractionResult;
import com.cleanroommc.bogosorter.common.dropoff.InventoryData;
import com.cleanroommc.bogosorter.common.dropoff.InventoryManager;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

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
        EntityPlayerMP player = handler.playerEntity;
        InventoryManager inventoryManager = new InventoryManager(player);
        DropOffHandler dropOffHandler = new DropOffHandler(inventoryManager);
        dropOffHandler.setItemsCounter(0);

        List<InventoryData> inventoryDataList = inventoryManager.getNearbyInventories();

        // insert into smaller inventories first
        inventoryDataList.sort((a, b) -> a.getInventory().getSizeInventory() < b.getInventory().getSizeInventory() ? 1 : 0);

        for (InventoryData inventoryData : inventoryDataList) {
            IInventory inventory = inventoryData.getInventory();

            dropOffHandler.setStartSlot(InventoryManager.Slots.FIRST);
            dropOffHandler.setEndSlot(InventoryManager.Slots.LAST);

            if (inventory instanceof TileEntityFurnace) {
                if (inventory.getStackInSlot(InventoryManager.Slots.FIRST) == null) {
                    dropOffHandler.setStartSlot(InventoryManager.Slots.FURNACE_FUEL);
                }

                dropOffHandler.setEndSlot(InventoryManager.Slots.FURNACE_OUT);
            }

            dropOffHandler.dropOff(inventoryData);

            inventory.markDirty();
        }

        player.inventory.markDirty();

        player.inventoryContainer.detectAndSendChanges();

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

        return new SDropOffMessage(dropOffHandler.getItemsCounter(), affectedContainers, inventoryDataList.size(), rendererCubeTargets);
    }

}
