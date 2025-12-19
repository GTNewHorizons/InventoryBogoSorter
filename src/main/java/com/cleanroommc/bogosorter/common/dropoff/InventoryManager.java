package com.cleanroommc.bogosorter.common.dropoff;

import static com.cleanroommc.bogosorter.common.dropoff.LocalizationHelper.getDisplayNameEnglish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.compat.Mods;
import com.gtnewhorizon.gtnhlib.datastructs.space.ArrayProximityMap4D;
import com.gtnewhorizon.gtnhlib.datastructs.space.VolumeShape;

import serverutils.data.ClaimedChunks;

public class InventoryManager {

    private final EntityPlayerMP player;
    private final World world;

    // Map of nearby tile entities with quick proximity lookup
    private final ArrayProximityMap4D<TileEntity> NEARBY_INV_MAP = new ArrayProximityMap4D<>(VolumeShape.CUBE);

    public InventoryManager(EntityPlayerMP player) {
        this.player = player;
        world = player.getEntityWorld();
    }

    EntityPlayerMP getPlayer() {
        return player;
    }

    public List<InventoryData> getNearbyInventories() {
        int radius = BogoSorterConfig.dropOff.dropoffRadius;
        int dim = player.dimension;
        double radiusSq = radius * radius;

        // Player's current chunk coordinates
        int playerChunkX = (int) player.posX >> 4;
        int playerChunkZ = (int) player.posZ >> 4;

        // Number of chunks to scan around player (+1 for edges)
        int chunkRadius = Math.max((radius - 1) / 16, 0) + 1;

        // Iterate chunks in range
        for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
            for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
                if (!world.getChunkProvider()
                    .chunkExists(cx, cz)) continue; // skip unloaded chunks

                Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
                for (TileEntity te : chunk.chunkTileEntityMap.values()) {
                    if (te == null || te.isInvalid()) continue;
                    // Only consider inventories
                    if (!(te instanceof IInventory || te instanceof TileEntityEnderChest)) continue;

                    // Only in same dimension
                    if (te.getWorldObj().provider.dimensionId != dim) continue;

                    // Respect claimed-chunk restrictions (if ServerUtilities is present)
                    if (Mods.ServerUtilities.isLoaded()
                        && ClaimedChunks.blockBlockInteractions(player, te.xCoord, te.yCoord, te.zCoord, 0)) {
                        continue;
                    }

                    // Quick spherical radius check
                    double dx = te.xCoord - player.posX;
                    double dy = te.yCoord - player.posY;
                    double dz = te.zCoord - player.posZ;
                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq > radiusSq) continue;

                    // Add tile entity to proximity map
                    NEARBY_INV_MAP.put(te, dim, te.xCoord, te.yCoord, te.zCoord, radius);
                }
            }
        }

        List<InventoryData> result = new ArrayList<>();

        // Iterate through all inventories that fall inside the search radius
        NEARBY_INV_MAP.forEachInRange(dim, player.posX, player.posY, player.posZ, te -> {

            if (te == null) {
                return;
            }

            InventoryData data;

            if (te instanceof IInventory) {
                data = getInventoryData((TileEntity & IInventory) te);
            } else if (te instanceof TileEntityEnderChest) {
                data = getInventoryData((TileEntityEnderChest) te);
            } else {
                return;
            }

            // Avoid duplicates (double chest halves)
            if (!result.isEmpty()) {
                for (int i = 0; i < result.size(); i++) {
                    InventoryData existingData = result.get(i);
                    if (existingData.getEntities()
                        .contains(te)) {
                        return;
                    }
                }
            }

            if (isInventoryValid(data)) {
                result.add(data);
            }
        });

        return result;
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
            // Always return English name for blocks even if the game is in another language
            return getDisplayNameEnglish(itemStack);
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
        String[] containerNames = BogoSorterConfig.dropOff.dropoffTargetNames;
        for (String containerName : containerNames) {
            if (name.toLowerCase()
                .contains(containerName.toLowerCase())) {
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
