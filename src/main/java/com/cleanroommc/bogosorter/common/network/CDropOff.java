package com.cleanroommc.bogosorter.common.network;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.dropoff.DropOffHandler;
import com.cleanroommc.bogosorter.common.dropoff.InteractionResult;
import com.cleanroommc.bogosorter.common.dropoff.InventoryData;
import com.cleanroommc.bogosorter.common.dropoff.InventoryManager;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

public class CDropOff implements IPacket {

    static final HashMap<UUID, Long> playerThrottles = new HashMap<>();

    public CDropOff() {}

    @Override
    public void encode(PacketBuffer buf) throws IOException {

    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {

    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.playerEntity;
        if (MinecraftServer.getServer()
            .isDedicatedServer()) {
            long lastPlayerTime = playerThrottles.computeIfAbsent(player.getPersistentID(), p -> 0L);
            final long throttleTime = System.nanoTime();
            if ((throttleTime - lastPlayerTime)
                < TimeUnit.MILLISECONDS.toNanos(BogoSorterConfig.dropOff.dropoffPacketThrottleInMS)) {
                return new SDropOffThrottled();
            }
            playerThrottles.replace(player.getPersistentID(), throttleTime);
        }

        InventoryManager inventoryManager = new InventoryManager(player);
        DropOffHandler dropOffHandler = new DropOffHandler(inventoryManager);
        dropOffHandler.setItemsCounter(0);

        List<InventoryData> inventoryDataList = inventoryManager.getNearbyInventories();

        boolean timeLimitExceeded = false;
        long startTime = System.nanoTime();
        for (InventoryData inventoryData : inventoryDataList) {
            if (timeLimitExceeded) {
                inventoryData.setInteractionResult(InteractionResult.DROPOFF_QUOTA_MET);
                continue;
            }
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

            long elapsedTime = System.nanoTime() - startTime;
            if (elapsedTime >= TimeUnit.MILLISECONDS.toNanos(BogoSorterConfig.dropOff.dropoffQuotaInMS)) {
                timeLimitExceeded = true;
            }
        }

        player.inventory.markDirty();

        player.inventoryContainer.detectAndSendChanges();

        List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
        int affectedContainers = 0;

        for (InventoryData inventoryData : inventoryDataList) {
            InteractionResult result = inventoryData.getInteractionResult();
            if (result == InteractionResult.DROPOFF_QUOTA_MET) continue;

            Color color;

            if (result == InteractionResult.DROPOFF_SUCCESS) {
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

        return new SDropOffMessage(
            dropOffHandler.getItemsCounter(),
            affectedContainers,
            inventoryDataList.size(),
            rendererCubeTargets,
            timeLimitExceeded);
    }

}
