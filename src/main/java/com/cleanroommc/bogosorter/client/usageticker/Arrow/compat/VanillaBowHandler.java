package com.cleanroommc.bogosorter.client.usageticker.Arrow.compat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.client.usageticker.Arrow.ArrowHandlerRegistry;

/**
 * A handler for the vanilla bow that correctly prioritizes modded arrows.
 */
public class VanillaBowHandler extends ArrowHandlerRegistry.ArrowHandler {

    private final List<Item> displayPriority = new ArrayList<>();

    public VanillaBowHandler() {}

    public void addModdedArrows(Item moddedArrow) {
        if (moddedArrow != null) {
            displayPriority.add(moddedArrow);
        }
    }

    @Nullable
    private ItemStack findCurrentAmmoStack(EntityClientPlayerMP player, ItemStack weaponStack) {
        if (player == null) return null;
        // Scan inventory based on the priority list.
        for (Item priorityItem : this.displayPriority) {
            // Main inventory
            for (ItemStack invStack : player.inventory.mainInventory) {
                if (invStack != null && invStack.getItem() == priorityItem) return invStack;
            }
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, weaponStack) > 0) {
                return null;
            }
            // add baubles here later if needed
        }
        // Fallback to regular arrows if nothing else is found.
        ItemStack Arrow = new ItemStack(Items.arrow);
        return Arrow;

    }

    @Override
    public int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack) {
        ItemStack currentAmmo = findCurrentAmmoStack(player, weaponStack);
        if (currentAmmo == null) return 0;
        int count = 0;
        for (ItemStack invStack : player.inventory.mainInventory) {
            if (invStack != null
                && (ItemStack.areItemStacksEqual(currentAmmo, invStack) || invStack.getItem() == Items.arrow)) {
                count += invStack.stackSize;
            }
        }
        return count;
    }

    @Override
    public ItemStack getDisplayStack(EntityClientPlayerMP player, ItemStack weaponStack) {
        return findCurrentAmmoStack(player, weaponStack);
    }
}
