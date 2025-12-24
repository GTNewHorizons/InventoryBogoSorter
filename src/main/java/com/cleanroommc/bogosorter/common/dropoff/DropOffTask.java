package com.cleanroommc.bogosorter.common.dropoff;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.SDropOffMessage;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

public class DropOffTask implements Runnable {

    // Final references needed for the duration of the job
    public final EntityPlayerMP player;
    public final List<InventoryData> inventoryDataList;
    private final InventoryManager inventoryManager;
    private final DropOffHandler dropOffHandler;

    // State Variables: These persist across multiple ticks
    public int currentContainerIndex = 0;
    private int itemsMoved = 0;
    private int containersAffected = 0;
    private int quotaReachedCount = 0;

    // The visual outlines to send to the client when the job finishes
    private final List<RendererCubeTarget> targets;

    private boolean isFinished = false;

    public DropOffTask(EntityPlayerMP player, List<InventoryData> list) {
        this.player = player;
        this.inventoryDataList = list;
        this.inventoryManager = new InventoryManager(player);
        this.dropOffHandler = new DropOffHandler(inventoryManager);
        this.targets = new ArrayList<>();
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        // Calculate allowed time per tick in nanoseconds (e.g. 5ms = 5,000,000ns)
        long quotaNanos = TimeUnit.MILLISECONDS.toNanos(BogoSorterConfig.dropOff.dropoffQuotaInMS);

        // Resume loop from where we left off in the previous tick
        for (int i = currentContainerIndex; i < inventoryDataList.size(); i++) {

            InventoryData inventoryData = inventoryDataList.get(i);
            IInventory inventory = inventoryData.getInventory();

            // --- Slot Setup ---
            // Configure which slots in the target container we can touch
            dropOffHandler.setStartSlot(InventoryManager.Slots.FIRST);
            dropOffHandler.setEndSlot(InventoryManager.Slots.LAST);

            // Special handling for Furnaces (Fuel vs Input)
            if (inventory instanceof TileEntityFurnace) {
                dropOffHandler.setEndSlot(InventoryManager.Slots.FURNACE_OUT);
                if (inventory.getStackInSlot(InventoryManager.Slots.FIRST) == null) {
                    dropOffHandler.setStartSlot(InventoryManager.Slots.FURNACE_FUEL);
                } else {
                    dropOffHandler.setStartSlot(InventoryManager.Slots.FIRST);
                }
            }

            // --- Drop Off ---
            // Attempt to move items from player -> container
            int itemsBeforeDropOff = dropOffHandler.getItemsCounter();
            dropOffHandler.dropOff(inventoryData);
            int itemsMovedThisContainer = dropOffHandler.getItemsCounter() - itemsBeforeDropOff;

            // Save changes to the target container
            inventory.markDirty();

            // --- State Updates ---
            itemsMoved += itemsMovedThisContainer;

            if (itemsMovedThisContainer > 0) {
                inventoryData.setInteractionResult(InteractionResult.DROPOFF_SUCCESS);
            }

            InteractionResult result = inventoryData.getInteractionResult();

            if (result == InteractionResult.DROPOFF_SUCCESS) {
                containersAffected++;
            }

            // --- Visual Generation (Red/Green) ---
            Color color = (result == InteractionResult.DROPOFF_SUCCESS) ? new Color(0, 255, 0) : // Green for SUCCESS
                new Color(255, 0, 0); // Red for FAIL (Checked but no items moved)

            for (TileEntity ent : inventoryData.getEntities()) {
                BlockPos blockPos = new BlockPos(ent.xCoord, ent.yCoord, ent.zCoord);
                targets.add(new RendererCubeTarget(blockPos, color));
            }

            // --- Quota Check ---
            // Check how much time has passed since this specific run() call started
            long elapsedTime = System.nanoTime() - startTime;
            if (elapsedTime >= quotaNanos) {
                // Time limit reached!
                // Save our current position (i + 1) so we resume correctly next tick.
                quotaReachedCount++;
                currentContainerIndex = i + 1;
                return;
            }
            // Advance index for the loop
            currentContainerIndex = i + 1;
        }

        // If we exit the loop naturally, it means we processed all containers.
        finishJob();
    }

    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Clean up and notify client.
     */
    private void finishJob() {
        // Sync player inventory changes to client
        player.inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
        isFinished = true;
        // Send the final result packet with all collected data (items moved, visual targets)
        SDropOffMessage finalPacket = new SDropOffMessage(
            itemsMoved,
            containersAffected,
            inventoryDataList.size(),
            targets,
            quotaReachedCount);

        NetworkHandler.sendToPlayer(finalPacket, player);
        DropOffScheduler.INSTANCE.dropOffTasks.remove(this);
    }
}
