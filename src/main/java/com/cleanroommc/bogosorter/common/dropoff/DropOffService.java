package com.cleanroommc.bogosorter.common.dropoff;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Singleton service that manages multi-tick drop-off jobs.
 * This class ensures that large sorting operations are split across multiple server ticks
 * to prevent lag spikes (TPS drop).
 */
public class DropOffService {

    private static final DropOffService INSTANCE = new DropOffService();

    // ConcurrentHashMap allows safe modification (adding/removing jobs) while iterating in the tick loop.
    public final Map<UUID, DropOffTask> activeTasks = new ConcurrentHashMap<>();

    public static DropOffService getInstance() {
        return INSTANCE;
    }

    /**
     * Initiates a new drop-off job for a player.
     */
    public void startJob(EntityPlayerMP player) {
        // Scan the world for nearby inventories
        List<InventoryData> inventoryDataList = new InventoryManager(player).getNearbyInventories();

        // Create the task object
        DropOffTask task = new DropOffTask(player, inventoryDataList);
        activeTasks.put(player.getPersistentID(), task);

        // Immediately run the first "slice" of work.
        // If it finishes instantly, task.run() will call finishJob() and send the packet.
        task.run();
    }

    /**
     * Checks if a player already has a job running.
     * Used to throttle packet spam from the client.
     */
    public boolean isJobActive(UUID playerID) {
        return activeTasks.containsKey(playerID);
    }

    /**
     * Runs once per server tick (usually 20 times a second).
     */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        // Run at the END of the tick to ensure world data is stable.
        if (event.phase != TickEvent.Phase.END) return;
        if (activeTasks.isEmpty()) return;

        activeTasks.values()
            .forEach(task -> {
                // Verify the player is still online before processing
                if (task.player.playerNetServerHandler.func_147362_b()
                    .isChannelOpen()) {
                    // Execute the next time-slice of the job
                    task.run();
                } else {
                    // If player disconnected, cancel the job to free memory
                    activeTasks.remove(task.player.getPersistentID());
                }
            });
    }
}
