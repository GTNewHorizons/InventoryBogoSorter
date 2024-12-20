package com.cleanroommc.bogosorter.common.dropoff.tasks;

import com.cleanroommc.bogosorter.common.dropoff.DropOffHandler;
import com.cleanroommc.bogosorter.common.dropoff.InventoryData;
import com.cleanroommc.bogosorter.common.dropoff.InventoryManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityFurnace;

import java.util.ArrayList;
import java.util.List;

public class MainTask implements Runnable {

    private final EntityPlayerMP player;
    private final InventoryManager inventoryManager;
    private final DropOffHandler dropOffHandler;
    private List<InventoryData> inventoryDataList = new ArrayList<>();

    public MainTask(EntityPlayerMP player) {
        this.player = player;
        inventoryManager = new InventoryManager(player);
        dropOffHandler = new DropOffHandler(inventoryManager);
    }

    public EntityPlayerMP getPlayer() {
        return player;
    }

    public int getItemsCounter() {
        return dropOffHandler.getItemsCounter();
    }

    public List<InventoryData> getInventoryDataList() {
        return inventoryDataList;
    }

    @Override
    public void run() {
        dropOffHandler.setItemsCounter(0);

        List<InventoryData> inventoryDataList = inventoryManager.getNearbyInventories();

        for (InventoryData inventoryData : inventoryDataList) {
            IInventory inventory = inventoryData.getInventory();

            //if (DropOffConfig.INSTANCE.dropOff) {
                dropOffHandler.setStartSlot(InventoryManager.Slots.FIRST);
                dropOffHandler.setEndSlot(InventoryManager.Slots.LAST);

                if (inventory instanceof TileEntityFurnace) {
                    if (inventory.getStackInSlot(InventoryManager.Slots.FIRST) == null) {
                        dropOffHandler.setStartSlot(InventoryManager.Slots.FURNACE_FUEL);
                    }

                    dropOffHandler.setEndSlot(InventoryManager.Slots.FURNACE_OUT);
                }

                dropOffHandler.dropOff(inventoryData);
            //}


            inventory.markDirty();
        }

        this.inventoryDataList = inventoryDataList;

        player.inventory.markDirty();

        player.inventoryContainer.detectAndSendChanges();
    }

}
