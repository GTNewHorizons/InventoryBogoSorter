package com.cleanroommc.bogosorter.common.dropoff;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class DropOffScheduler {

    public static final DropOffScheduler INSTANCE = new DropOffScheduler();

    public final List<DropOffTask> dropOffTasks = new ArrayList<>();

    public void startTask(EntityPlayerMP playerMP) {

        List<InventoryData> dataList = new InventoryManager(playerMP).getNearbyInventories();

        DropOffTask task = new DropOffTask(playerMP, dataList);
        task.run();

        // the task did not finish so add it to the list, this will continue to ran on the server ticks
        if (!task.isFinished()) {
            dropOffTasks.add(task);
        }
    }

    public boolean isPlayerTaskActive(EntityPlayerMP playerMP) {
        for (int i = 0; i < dropOffTasks.size(); i++) {
            DropOffTask task = dropOffTasks.get(i);
            if (task.player == playerMP || task.player.getPersistentID() == playerMP.getPersistentID()) return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (dropOffTasks.isEmpty()) return;

        for (int i = 0; i < dropOffTasks.size(); i++) {
            DropOffTask task = dropOffTasks.get(i);
            if (task.isFinished() || !task.player.playerNetServerHandler.func_147362_b()
                .isChannelOpen()) {
                dropOffTasks.remove(task);
            }
            task.run();
        }
    }

}
