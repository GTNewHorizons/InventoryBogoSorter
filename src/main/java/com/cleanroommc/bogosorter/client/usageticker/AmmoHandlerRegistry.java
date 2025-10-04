package com.cleanroommc.bogosorter.client.usageticker;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.compat.Mods;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.weaponry.ProjectileWeapon;
import tconstruct.weaponry.TinkerWeaponry;

/**
 * A registry to handle the logic for different types of weapons that use ammunition.
 * This provides a centralized place to add compatibility for various items.
 */
@SideOnly(Side.CLIENT)
public class AmmoHandlerRegistry {

    private static final Map<Item, AmmoHandler> HANDLERS = new LinkedHashMap<>();

    /**
     * Registers a new handler for a specific weapon item class.
     *
     * @param weaponClass The class of the weapon Item.
     * @param handler     The handler containing the logic for this weapon.
     */
    public static void register(Item weaponClass, AmmoHandler handler) {
        if (weaponClass != null && handler != null) {
            HANDLERS.put(weaponClass, handler);
        }
    }

    /**
     * Finds the appropriate handler for a given ItemStack.
     *
     * @param stack The ItemStack to check.
     * @return The corresponding AmmoHandler, or null if none is found.
     */
    @Nullable
    public static AmmoHandler getHandler(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        AmmoHandler handler = HANDLERS.get(stack.getItem());
        if (handler != null) {
            return handler;
        }
        for (Map.Entry<Item, AmmoHandler> entry : HANDLERS.entrySet()) {
            if (entry.getKey()
                .equals(stack.getItem())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * An abstract base class for weapon and ammo logic. Each handler must provide its own implementation.
     */
    public static abstract class AmmoHandler {

        abstract int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack);

        abstract ItemStack getDisplayStack(ItemStack weaponStack);
    }

    /**
     * A simple handler for weapons that use a specific, countable item as ammunition.
     */
    private static class SimpleAmmoHandler extends AmmoHandler {

        private final Item[] validAmmo;

        public SimpleAmmoHandler(Item... validAmmo) {
            this.validAmmo = validAmmo;
        }

        @Override
        int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack) {
            int count = 0;
            for (ItemStack invStack : player.inventory.mainInventory) {
                if (invStack != null && validAmmo[0] != null) {
                    for (Item ammo : validAmmo) {
                        if (invStack.getItem() == ammo) {
                            count += invStack.stackSize;
                            break;
                        }
                    }
                }
            }
            return count;
        }

        @Override
        ItemStack getDisplayStack(ItemStack weaponStack) {
            if (validAmmo[0] != null && validAmmo.length > 0) {
                ItemStack validarrow = new ItemStack(validAmmo[0]);
                return validarrow;
            }
            return null;
        }
    }

    /**
     * A dedicated compatibility class for Tinker's Construct.
     * This class is only loaded if TConstruct is present, preventing crashes.
     */
    private static class TConstructCompat {

        public static void register() {
            TConstructAmmoHandler handler = new TConstructAmmoHandler();
            AmmoHandlerRegistry.register(TinkerWeaponry.shortbow, handler);
            AmmoHandlerRegistry.register(TinkerWeaponry.longbow, handler);
            AmmoHandlerRegistry.register(TinkerWeaponry.crossbow, handler);
        }

        private static class TConstructAmmoHandler extends AmmoHandler {

            // This is to refresh the counter for the usage ticker
            @Override
            int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack) {
                if (weaponStack.getItem() instanceof ProjectileWeapon projectileWeapon) {
                    ItemStack ammo = projectileWeapon.searchForAmmo(player, weaponStack);
                    if (ammo != null && ammo.hasTagCompound()) {
                        NBTTagCompound tags = ammo.getTagCompound()
                            .getCompoundTag("InfiTool");
                        return tags.getInteger("Ammo");
                    }
                }
                return 0;
            }

            @Override
            ItemStack getDisplayStack(ItemStack weaponStack) {
                if (weaponStack.getItem() instanceof ProjectileWeapon projectileWeapon) {
                    return projectileWeapon.searchForAmmo(Minecraft.getMinecraft().thePlayer, weaponStack);
                }
                return null;
            }
        }
    }

    static {
        // Register vanilla handlers directly
        register(Items.bow, new SimpleAmmoHandler(Items.arrow));

        // Register mod compatibility handlers safely
        if (Mods.Tconstruct.isLoaded()) {
            TConstructCompat.register();
        }
    }
}
