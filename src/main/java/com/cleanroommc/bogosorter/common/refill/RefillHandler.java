package com.cleanroommc.bogosorter.common.refill;

import java.util.Set;
import java.util.function.BiPredicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.network.CRefill;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.cleanroommc.bogosorter.common.network.SRefillSound;
import com.cleanroommc.bogosorter.compat.Mods;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.items.GTGenericItem;
import gregtech.api.items.MetaGeneratedTool;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import vazkii.botania.common.item.brew.ItemBrewBase;

public class RefillHandler {

    public RefillHandler() {}

    private static final int[][] INVENTORY_PROXIMITY_MAP = {
        { 1, 2, 3, 4, 5, 6, 7, 8, 27, 18, 9, 28, 19, 10, 29, 20, 11, 30, 21, 12, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34,
            25, 16, 35, 26, 17 },
        { 0, 2, 3, 4, 5, 6, 7, 8, 28, 19, 10, 27, 18, 9, 29, 20, 11, 30, 21, 12, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34,
            25, 16, 35, 26, 17 },
        { 1, 3, 0, 4, 5, 6, 7, 8, 29, 20, 11, 28, 19, 10, 30, 21, 12, 27, 18, 9, 31, 22, 13, 32, 23, 14, 33, 24, 15, 34,
            25, 16, 35, 26, 17 },
        { 2, 4, 1, 5, 0, 6, 7, 8, 30, 21, 12, 29, 20, 11, 31, 22, 13, 28, 19, 10, 32, 23, 14, 27, 18, 9, 33, 24, 15, 34,
            25, 16, 35, 26, 17 },
        { 3, 5, 2, 6, 1, 7, 0, 8, 31, 22, 13, 30, 21, 12, 32, 23, 14, 29, 20, 11, 33, 24, 15, 28, 19, 10, 34, 25, 16,
            27, 18, 9, 35, 26, 17 },
        { 4, 6, 3, 7, 2, 8, 1, 0, 32, 23, 14, 31, 22, 13, 33, 24, 15, 30, 21, 12, 34, 25, 16, 29, 20, 11, 35, 26, 17,
            28, 19, 10, 27, 18, 9 },
        { 5, 7, 4, 8, 3, 2, 1, 0, 33, 24, 15, 32, 23, 14, 34, 25, 16, 31, 22, 13, 35, 26, 17, 30, 21, 12, 29, 20, 11,
            28, 19, 10, 27, 18, 9 },
        { 6, 8, 5, 4, 3, 2, 1, 0, 34, 25, 16, 33, 24, 15, 35, 26, 17, 32, 23, 14, 31, 22, 13, 30, 21, 12, 29, 20, 11,
            28, 19, 10, 27, 18, 9 },
        { 7, 6, 5, 4, 3, 2, 1, 0, 35, 26, 17, 34, 25, 16, 33, 24, 15, 32, 23, 14, 31, 22, 13, 30, 21, 12, 29, 20, 11,
            28, 19, 10, 27, 18, 9 }, };

    @SubscribeEvent
    public void onDestroyItem(PlayerDestroyItemEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null
            || !event.entityPlayer.worldObj.isRemote
            || !BogoSorterConfig.enableAutoRefill_server
            || !BogoSorterConfig.enableAutoRefill) return;

        if (event.original == null) {
            BogoSorter.LOGGER.info("Cannot refill destroyed item as it is now null");
            return;
        }

        if (event.original.getItem() != null && shouldHandleRefill(event.entityPlayer, event.original, true)) {
            int index = event.entityPlayer.inventory.currentItem;
            if (index < 9) {
                NetworkHandler.sendToServer(new CRefill(event.original, index, false));
            }
        }
    }

    public static boolean handle(int hotbarIndex, ItemStack brokenItem, EntityPlayer player, boolean swap) {
        return new RefillHandler(hotbarIndex, brokenItem, player, swap).handleRefill();
    }

    public static boolean shouldHandleRefill(EntityPlayer player, ItemStack brokenItem) {
        return shouldHandleRefill(player, brokenItem, false);
    }

    public static boolean shouldHandleRefill(EntityPlayer player, ItemStack brokenItem, boolean allowClient) {
        Container container = player.openContainer;
        return (allowClient || !NetworkUtils.isClient(player))
            && (container == null || container == player.inventoryContainer)
            && brokenItem != null;
    }

    private BiPredicate<ItemStack, ItemStack> similarItemMatcher = (stack,
        stack2) -> stack.getItem() == stack2.getItem() && stack.getItemDamage() == stack2.getItemDamage();
    private BiPredicate<ItemStack, ItemStack> exactItemMatcher = RefillHandler::matchTags;
    private int hotbarIndex;
    private IntList slots;
    private ItemStack brokenItem;
    private EntityPlayer player;
    private InventoryPlayer inventory;
    private boolean swapItems;
    private boolean isDamageable;

    public RefillHandler(int hotbarIndex, ItemStack brokenItem, EntityPlayer player, boolean swapItems) {
        this.hotbarIndex = hotbarIndex;
        this.slots = new IntArrayList(
            INVENTORY_PROXIMITY_MAP[hotbarIndex == 40 ? player.inventory.currentItem : hotbarIndex]);
        this.brokenItem = brokenItem;
        this.player = player;
        this.inventory = player.inventory;
        this.swapItems = swapItems;
    }

    public RefillHandler(int hotbarIndex, ItemStack brokenItem, EntityPlayer player) {
        this(hotbarIndex, brokenItem, player, false);
    }

    public boolean handleRefill() {
        if (brokenItem.getItem() instanceof ItemBlock) {
            return findItem(false);
        } else if (brokenItem.isItemStackDamageable()
            || (Mods.GT5u.isLoaded() && brokenItem.getItem() instanceof GTGenericItem)) {
                if (isExactItem(brokenItem)) {
                    exactItemMatcher = (stack, stack2) -> {
                        if (stack.hasTagCompound() != stack2.hasTagCompound()) return false;
                        if (!stack.hasTagCompound()) return true;
                        return OreDictHelper.getModCompoundTag(brokenItem, stack, stack2);
                    };
                } else {
                    similarItemMatcher = (stack, stack2) -> stack.getItem() == stack2.getItem();
                }
                isDamageable = true;
                return findNormalDamageable();
            } else {
                return findItem(true);
            }
    }

    private static boolean isExactItem(ItemStack itemStack) {
        if (Mods.GT5u.isLoaded() && itemStack.getItem() instanceof MetaGeneratedTool) {
            return true;
        }
        if (Mods.Botania.isLoaded() && itemStack.getItem() instanceof ItemBrewBase) {
            return true;
        }
        return false;
    }

    private boolean findItem(boolean exactOnly) {
        ItemStack firstItemMatch = null;
        int firstItemMatchSlot = -1;
        IntListIterator slotsIterator = slots.iterator();
        while (slotsIterator.hasNext()) {
            int slot = slotsIterator.next();
            ItemStack found = inventory.mainInventory[slot];
            if (found == null || (this.swapItems && this.isDamageable
                && DamageHelper.getDurability(found) <= BogoSorterConfig.autoRefillDamageThreshold)) {
                slotsIterator.remove();
                continue;
            }
            if (similarItemMatcher.test(brokenItem, found)) {
                if (exactItemMatcher.test(brokenItem, found)) {
                    refillItem(found, slot);
                    return true;
                }
                if (firstItemMatch == null) {
                    firstItemMatch = found;
                    firstItemMatchSlot = slot;
                }
            }
        }
        if (firstItemMatch != null && !exactOnly) {
            refillItem(firstItemMatch, firstItemMatchSlot);
            return true;
        }
        return false;
    }

    private boolean findNormalDamageable() {
        if (findItem(false)) {
            return true;
        }
        if (slots.isEmpty()) return false;

        Set<String> brokenToolClasses = brokenItem.getItem()
            .getToolClasses(brokenItem);
        if (brokenToolClasses.isEmpty()) return false;

        // try match tool type
        for (int slot : slots) {
            ItemStack found = inventory.mainInventory[slot];
            Set<String> toolTypes = found.getItem()
                .getToolClasses(found);
            if (brokenToolClasses.equals(toolTypes)) {
                refillItem(found, slot);
                return true;
            }
        }

        int brokenTools = brokenToolClasses.size();
        for (int slot : slots) {
            ItemStack found = inventory.mainInventory[slot];
            if (found == null) continue;
            Set<String> toolTypes = found.getItem()
                .getToolClasses(found);
            int tools = toolTypes.size();
            if (tools == 0 || tools == brokenTools) continue;
            if (tools > brokenTools) {
                if (toolTypes.containsAll(brokenToolClasses)) {
                    refillItem(found, slot);
                    return true;
                }
            } else {
                if (brokenToolClasses.containsAll(toolTypes)) {
                    refillItem(found, slot);
                    return true;
                }
            }
        }

        return false;
    }

    private void refillItem(ItemStack refill, int refillIndex) {
        ItemStack current = null;
        if (!this.swapItems) current = getItem(this.hotbarIndex);
        setAndSyncSlot(hotbarIndex, refill.copy());
        setAndSyncSlot(refillIndex, swapItems ? brokenItem.copy() : null);
        player.inventoryContainer.detectAndSendChanges();
        if (current != null) {
            // the broken item replaced itself with something
            // insert the item into another slot to prevent it from being lost
            this.inventory.addItemStackToInventory(current);
        }

        // the sound should be played for this player
        if (!NetworkUtils.isClient(player)) {
            NetworkHandler.sendToPlayer(new SRefillSound(), (EntityPlayerMP) player);
        }
    }

    private void setAndSyncSlot(int index, ItemStack item) {
        if (index < 0 || index > 40) return;
        int slot = index;
        if (index < 36) {
            inventory.mainInventory[index] = item;
            if (index < 9) slot += 36;
            else slot += 9;
        } else if (index < 40) {
            inventory.armorInventory[index - 36] = item;
            slot += 5;
        }
        if (item != null) {
            player.inventoryContainer.inventoryItemStacks.set(slot, null);
        }
    }

    private ItemStack getItem(int index) {
        if (index < 36) return this.inventory.mainInventory[index];
        if (index < 40) return this.inventory.mainInventory[index - 36];
        return null;
    }

    private static boolean matchTags(ItemStack stackA, ItemStack stackB) {
        if (stackA.getTagCompound() == null && stackB.getTagCompound() != null) {
            return false;
        } else {
            return (stackA.getTagCompound() == null || stackA.getTagCompound()
                .equals(stackB.getTagCompound()));
        }
    }
}
