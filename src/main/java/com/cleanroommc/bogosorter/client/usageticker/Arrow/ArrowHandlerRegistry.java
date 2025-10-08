package com.cleanroommc.bogosorter.client.usageticker.Arrow;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.client.usageticker.Arrow.compat.TConstructCompat;
import com.cleanroommc.bogosorter.client.usageticker.Arrow.compat.VanillaBowHandler;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.compat.Mods;

import cpw.mods.fml.common.registry.GameRegistry;

public class ArrowHandlerRegistry {

    private static final Map<Item, ArrowHandler> HANDLERS = new LinkedHashMap<>();

    /**
     * Registers a new handler for a specific weapon item.
     *
     * @param weapon  The of the weapon Item.
     * @param handler The handler containing the logic for this weapon.
     */
    public static void register(Item weapon, ArrowHandler handler) {
        if (weapon != null && handler != null) {
            HANDLERS.put(weapon, handler);
        }
    }

    /**
     * Finds the appropriate handler for a given ItemStack.
     *
     * @param stack The ItemStack to check.
     * @return The corresponding ArrowHandlerRegistry, or null if none is found.
     */
    @Nullable
    public static ArrowHandler getHandler(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        ArrowHandler handler = HANDLERS.get(stack.getItem());
        return handler;
    }

    /**
     * An abstract base class for weapon and Arrow logic. Each handler must provide its own implementation.
     */
    public static abstract class ArrowHandler {

        public abstract int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack);

        public abstract ItemStack getDisplayStack(EntityClientPlayerMP player, ItemStack weaponStack);
    }

    static {
        VanillaBowHandler handler = new VanillaBowHandler();

        // Parse the list of valid arrow items from the config.
        for (String itemID : BogoSorterConfig.usageTicker.arrow.arrowItems) {
            Item arrowItem = parseItemFromString(itemID);
            if (arrowItem != null) {
                handler.addModdedArrows(arrowItem);
            }
        }

        // Register this handler for every bow item listed in the config.
        for (String itemID : BogoSorterConfig.usageTicker.arrow.bowItems) {
            Item bowItem = parseItemFromString(itemID);
            if (bowItem != null) {
                ArrowHandlerRegistry.register(bowItem, handler);
            }
        }

        // Tinkers' Construct support
        if (Mods.Tconstruct.isLoaded()) {
            TConstructCompat.register();
        }
    }

    /**
     * parses a string item ID (e.g., "minecraft:arrow") into an Item object.
     *
     * @param itemID The string identifier for the item.
     * @return The Item object, or null if not found.
     */
    private static Item parseItemFromString(String itemID) {
        if (itemID == null || itemID.isEmpty()) {
            return null;
        }
        String[] parts = itemID.split(":");
        if (parts.length == 2) {
            return GameRegistry.findItem(parts[0], parts[1]);
        }
        return null;
    }
}
