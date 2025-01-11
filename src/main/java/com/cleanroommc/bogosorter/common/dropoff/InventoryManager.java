package com.cleanroommc.bogosorter.common.dropoff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.cleanroommc.bogosorter.compat.loader.Mods;

import serverutils.data.ClaimedChunks;

public class InventoryManager {

    private final int SCAN_RADIUS = 4;

    private final EntityPlayerMP player;
    private final World world;

    public InventoryManager(EntityPlayerMP player) {
        this.player = player;
        world = player.getEntityWorld();
    }

    EntityPlayerMP getPlayer() {
        return player;
    }

    public <T extends TileEntity & IInventory> List<InventoryData> getNearbyInventories() {
        int minX = (int) (player.posX - SCAN_RADIUS);
        int maxX = (int) (player.posX + SCAN_RADIUS);

        int minY = (int) (player.posY - SCAN_RADIUS);
        int maxY = (int) (player.posY + SCAN_RADIUS);

        int minZ = (int) (player.posZ - SCAN_RADIUS);
        int maxZ = (int) (player.posZ + SCAN_RADIUS);

        List<InventoryData> inventoryDataList = new ArrayList<>();

        // start the direction the player is facing
        Vec3 lookVec = player.getLookVec();
        int lookX = (int) Math.signum(lookVec.xCoord);
        int lookY = (int) Math.signum(lookVec.yCoord);
        int lookZ = (int) Math.signum(lookVec.zCoord);

        int[] xOrder = (lookX >= 0) ? range(minX, maxX + 1) : reverseRange(minX, maxX + 1);
        int[] yOrder = (lookY >= 0) ? range(minY, maxY + 1) : reverseRange(minY, maxY + 1);
        int[] zOrder = (lookZ >= 0) ? range(minZ, maxZ + 1) : reverseRange(minZ, maxZ + 1);

        for (int x : xOrder) {
            for (int y : yOrder) {
                for (int z : zOrder) {
                    TileEntity currentEntity = world.getTileEntity(x, y, z);

                    InventoryData currentInvData;

                    if (currentEntity instanceof IInventory) {
                        if (Mods.ServerUtilities.isLoaded()) {
                            if (ClaimedChunks.blockBlockInteractions(player, x, y, z, 0)) continue;
                        }
                        // noinspection unchecked
                        currentInvData = getInventoryData((T) currentEntity);
                    } else if (currentEntity instanceof TileEntityEnderChest) {
                        if (Mods.ServerUtilities.isLoaded()) {
                            if (ClaimedChunks.blockBlockInteractions(player, x, y, z, 0)) continue;
                        }
                        currentInvData = getInventoryData((TileEntityEnderChest) currentEntity);
                    } else {
                        continue;
                    }

                    int listSize = inventoryDataList.size();

                    if (listSize > 0) {
                        InventoryData previousInvData = inventoryDataList.get(listSize - 1);

                        // Check for duplicates generated from double chests.
                        if (previousInvData.getEntities()
                            .contains(currentEntity)) {
                            continue;
                        }
                    }

                    if (currentInvData.getInventory()
                        .isUseableByPlayer(player) && isInventoryValid(currentInvData)) {
                        inventoryDataList.add(currentInvData);
                    }
                }
            }
        }

        return inventoryDataList;
    }

    private int[] range(int start, int end) {
        return IntStream.range(start, end)
            .toArray();
    }

    private int[] reverseRange(int start, int end) {
        return IntStream.range(start, end)
            .map(i -> end - 1 - (i - start))
            .toArray();
    }

    boolean isStacksEqual(ItemStack left, ItemStack right) {
        NBTTagCompound leftTag = left.getTagCompound();
        NBTTagCompound rightTag = right.getTagCompound();

        return left.getItem() == right.getItem() && left.getItemDamage() == right.getItemDamage()
            && ((leftTag == null && rightTag == null) || (leftTag != null && leftTag.equals(rightTag)));
    }

    /**
     * This method returns the name of the block that appears in the tooltip when you move the mouse over the item that
     * corresponds to it.
     */
    String getItemStackName(IInventory inventory) {
        if (inventory instanceof InventoryLargeChest) {
            return Block.getBlockById(54)
                .getLocalizedName();
        }

        if (inventory instanceof TileEntityBrewingStand) {
            return StatCollector.translateToLocal(
                Item.getItemById(379)
                    .getUnlocalizedName() + ".name");
        }

        if (inventory instanceof TileEntity) {
            TileEntity entity = (TileEntity) inventory;
            ItemStack itemStack = new ItemStack(entity.getBlockType(), 1, entity.getBlockMetadata());

            return itemStack.getDisplayName();
        }

        return StatCollector.translateToLocal(inventory.getInventoryName());
    }

    int getMaxAllowedStackSize(IInventory inventory, ItemStack stack) {
        return Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
    }

    /**
     * This method checks the config to determine whether to process the inventory of the specified type or not.
     */
    private boolean isInventoryValid(InventoryData inventoryData) {
        TileEntity entity = inventoryData.getEntities()
            .get(0);

        if (entity instanceof TileEntityChest) {
            return true;
        }

        // TODO: limit the types of inventories?

        String inventoryName = getItemStackName(inventoryData.getInventory());

        return isInventoryNameValid(inventoryName);
    }

    private boolean isInventoryNameValid(String name) {
        List<String> containerNames = new ArrayList<>();
        containerNames.add("*Chest*");
        containerNames.add("*Barrel*");
        containerNames.add("*Drawer*");

        for (String containerName : containerNames) {
            String regex = containerName.replace("*", ".*")
                .trim();

            if (name.matches(regex)) {
                return true;
            }
        }

        return false;
    }

    // Implemented without a loop, because the order of the arguments in the "new InventoryLargeChest()" is important.
    private <T extends TileEntity & IInventory> InventoryData getInventoryData(T leftEntity) {
        List<TileEntity> entities = new ArrayList<>();

        if (leftEntity instanceof TileEntityChest) {
            String chestName = "container.chestDouble";

            TileEntity rightEntity = world.getTileEntity(leftEntity.xCoord - 1, leftEntity.yCoord, leftEntity.zCoord);

            // ----------------------------------------Check for trapped chests-----------------------------------------
            if (leftEntity.getBlockType()
                .canProvidePower()) {
                if (rightEntity instanceof TileEntityChest && rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        (IInventory) rightEntity,
                        leftEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }

                rightEntity = world.getTileEntity(leftEntity.xCoord + 1, leftEntity.yCoord, leftEntity.zCoord);

                if (rightEntity instanceof TileEntityChest && rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        leftEntity,
                        (IInventory) rightEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }

                rightEntity = world.getTileEntity(leftEntity.xCoord, leftEntity.yCoord, leftEntity.zCoord - 1);

                if (rightEntity instanceof TileEntityChest && rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        (IInventory) rightEntity,
                        leftEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }

                rightEntity = world.getTileEntity(leftEntity.xCoord, leftEntity.yCoord, leftEntity.zCoord + 1);

                if (rightEntity instanceof TileEntityChest && rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        leftEntity,
                        (IInventory) rightEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }
            } else { // ------------------------------------Check for regular chests------------------------------------
                if (rightEntity instanceof TileEntityChest && !rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        (IInventory) rightEntity,
                        leftEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }

                rightEntity = world.getTileEntity(leftEntity.xCoord + 1, leftEntity.yCoord, leftEntity.zCoord);

                if (rightEntity instanceof TileEntityChest && !rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        leftEntity,
                        (IInventory) rightEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }

                rightEntity = world.getTileEntity(leftEntity.xCoord, leftEntity.yCoord, leftEntity.zCoord - 1);

                if (rightEntity instanceof TileEntityChest && !rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        (IInventory) rightEntity,
                        leftEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }

                rightEntity = world.getTileEntity(leftEntity.xCoord, leftEntity.yCoord, leftEntity.zCoord + 1);

                if (rightEntity instanceof TileEntityChest && !rightEntity.getBlockType()
                    .canProvidePower()) {
                    InventoryLargeChest largeChest = new InventoryLargeChest(
                        chestName,
                        leftEntity,
                        (IInventory) rightEntity);

                    entities.add(leftEntity);
                    entities.add(rightEntity);

                    return new InventoryData(entities, largeChest, InteractionResult.DROPOFF_FAIL);
                }
            }
        }

        entities.add(leftEntity);

        return new InventoryData(entities, leftEntity, InteractionResult.DROPOFF_FAIL);
    }

    private InventoryData getInventoryData(TileEntityEnderChest entity) {
        List<TileEntity> entities = Collections.singletonList(entity);

        return new InventoryData(entities, player.getInventoryEnderChest(), InteractionResult.DROPOFF_FAIL);
    }

    public abstract class Slots {

        public static final int LAST = -1;
        public static final int FIRST = 0;
        public static final int FURNACE_FUEL = 1;
        public static final int FURNACE_OUT = 2;
        public static final int PLAYER_INVENTORY_FIRST = 9;
        public static final int PLAYER_INVENTORY_LAST = 36;

    }

}
