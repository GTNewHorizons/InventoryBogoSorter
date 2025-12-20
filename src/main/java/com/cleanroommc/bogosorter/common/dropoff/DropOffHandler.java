package com.cleanroommc.bogosorter.common.dropoff;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.compat.Mods;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;

import xonin.backhand.api.core.BackhandUtils;

public class DropOffHandler {

    private final InventoryManager inventoryManager;
    private final InventoryPlayer playerInventory;
    private final ItemStack[] playerStacks;
    private int itemsCounter;
    private int startSlot;
    private int endSlot;

    public DropOffHandler(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.playerInventory = inventoryManager.getPlayer().inventory;
        this.playerStacks = playerInventory.mainInventory;
    }

    public void setStartSlot(int value) {
        startSlot = value;
    }

    public void setEndSlot(int value) {
        endSlot = value;
    }

    public int getItemsCounter() {
        return itemsCounter;
    }

    public void dropOff(InventoryData toInventoryData) {
        IInventory toInventory = toInventoryData.getInventory();

        if (endSlot == InventoryManager.Slots.LAST) {
            endSlot = toInventory.getSizeInventory();
        }

        int offhandSlot = -1;
        if (Mods.Backhand.isLoaded()) {
            offhandSlot = BackhandUtils.getOffhandSlot(inventoryManager.getPlayer());
        }

        for (int i = InventoryManager.Slots.PLAYER_INVENTORY_FIRST; i < playerStacks.length; ++i) {
            if (Mods.Backhand.isLoaded() && i == offhandSlot) {
                // Dont take the offhand item
                continue;
            }
            if (playerStacks[i] != null && isItemValid(playerStacks[i].getDisplayName())) {
                // if (DropOffConfig.INSTANCE.dropOffOnlyFullStacks &&
                // playerStacks[i].stackSize <
                // inventoryManager.getMaxAllowedStackSize(playerInventory, playerStacks[i])) {
                // continue;
                // }

                int oldPlayerStackSize = playerStacks[i].stackSize;

                movePlayerStack(i, toInventory);

                int newPlayerStackSize = (playerStacks[i] == null) ? 0 : playerStacks[i].stackSize;
                int itemsMoved = oldPlayerStackSize - newPlayerStackSize;
                itemsCounter += itemsMoved;

                if (itemsMoved > 0) {
                    toInventoryData.setInteractionResult(InteractionResult.DROPOFF_SUCCESS);
                }
            }
        }
    }

    /**
     * This method checks the config text field to determine whether to DropOff the item with the specified name or not.
     */
    private boolean isItemValid(String name) {
        // String[] itemNames = StringUtils.split(DropOffConfig.INSTANCE.excludeItemsWithNames,
        // DropOffConfig.INSTANCE.delimiter);
        //
        // for (String itemName : itemNames) {
        // String regex = itemName.replace("*", ".*").trim();
        //
        // if (name.matches(regex)) {
        // return false;
        // }
        // }

        return true;
    }

    private void movePlayerStack(int playerStackIndex, IInventory toInventory) {
        Integer emptySlotIndex = null;
        boolean hasSameStack = false;

        for (int i = startSlot; i < endSlot; ++i) {
            ItemStack toCurrentStack = toInventory.getStackInSlot(i);

            if (toCurrentStack == null) {
                if (emptySlotIndex == null) {
                    emptySlotIndex = i;
                }

                continue;
            }

            if (inventoryManager.isStacksEqual(toCurrentStack, playerStacks[playerStackIndex])) {
                hasSameStack = true;
                int toCurrentStackMaxSize = inventoryManager.getMaxAllowedStackSize(toInventory, toCurrentStack);
                if (Mods.StorageDrawers.isLoaded() && toInventory instanceof TileEntityDrawers drawer) {
                    int drawerSlot = drawer.getDrawerInventory()
                        .getDrawerSlot(i);
                    // side is ignored
                    if (drawer.canInsertItem(drawerSlot, playerStacks[playerStackIndex], 0)) {
                        // handles reducing the player stack size
                        int countAdded = drawer.putItemsIntoSlot(
                            drawerSlot,
                            playerStacks[playerStackIndex],
                            playerStacks[playerStackIndex].stackSize);

                        if (countAdded == 0) {
                            continue;
                        }

                        if (playerStacks[playerStackIndex].stackSize <= 0) {
                            playerStacks[playerStackIndex] = null;
                        }
                    }
                    return;
                }

                if (toCurrentStack.stackSize + playerStacks[playerStackIndex].stackSize <= toCurrentStackMaxSize) {
                    toCurrentStack.stackSize += playerStacks[playerStackIndex].stackSize;
                    playerStacks[playerStackIndex] = null;

                    return;
                } else {
                    int leftToMax = toCurrentStackMaxSize - toCurrentStack.stackSize;

                    toCurrentStack.stackSize = toCurrentStackMaxSize;
                    playerStacks[playerStackIndex].stackSize -= leftToMax;
                }
            }
        }

        if (hasSameStack && emptySlotIndex != null
            && toInventory.isItemValidForSlot(emptySlotIndex, playerStacks[playerStackIndex])) {
            toInventory.setInventorySlotContents(emptySlotIndex, playerStacks[playerStackIndex]);
            playerStacks[playerStackIndex] = null;
        }
    }

}
