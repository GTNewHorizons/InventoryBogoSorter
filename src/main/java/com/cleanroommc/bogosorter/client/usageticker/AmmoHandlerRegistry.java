package com.cleanroommc.bogosorter.client.usageticker;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.compat.Mods;

import tconstruct.library.weaponry.ProjectileWeapon;
import tconstruct.weaponry.weapons.Crossbow;
import tconstruct.weaponry.weapons.LongBow;
import tconstruct.weaponry.weapons.ShortBow;

/**
 * A registry to handle the logic for different types of weapons that use ammunition.
 * This provides a centralized place to add compatibility for various items.
 */
public class AmmoHandlerRegistry {

    private static final Map<Class<? extends Item>, AmmoHandler> HANDLERS = new LinkedHashMap<>();

    /**
     * Registers a new handler for a specific weapon item class.
     * 
     * @param weaponClass The class of the weapon Item.
     * @param handler     The handler containing the logic for this weapon.
     */
    public static void register(Class<? extends Item>[] weaponClass, AmmoHandler handler) {
        if (weaponClass != null && handler != null) {
            for (Class<? extends Item> wc : weaponClass) {
                HANDLERS.put(wc, handler);
            }
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
        AmmoHandler handler = HANDLERS.get(
            stack.getItem()
                .getClass());
        if (handler != null) {
            return handler;
        }
        for (Map.Entry<Class<? extends Item>, AmmoHandler> entry : HANDLERS.entrySet()) {
            if (entry.getKey()
                .isInstance(stack.getItem())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * An abstract base class for weapon and ammo logic.
     */
    public static class AmmoHandler {

        public int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack) {
            int count = 0;
            if (Mods.Tconstruct.isLoaded()) {
                if (weaponStack.getItem() instanceof ProjectileWeapon PW) {
                    ItemStack ammo = PW.searchForAmmo(player, weaponStack);
                    if (ammo != null) {
                        NBTTagCompound tags = ammo.getTagCompound()
                            .getCompoundTag("InfiTool");
                        count += tags.getInteger("Ammo");
                    }
                }
            }

            for (AmmoWeaponDefinition def : AmmoWeaponDefinition.values()) {
                for (ItemStack invStack : player.inventory.mainInventory) {
                    if (invStack != null && def.validAmmo != null) {
                        for (Item ammo : def.validAmmo) {
                            if (invStack.getItem() == ammo) {
                                count += invStack.stackSize;
                                break;
                            }
                        }
                    }
                }
            }
            return count;
        }

        public ItemStack getDisplayStack(ItemStack weaponStack) {
            if (Mods.Tconstruct.isLoaded()) {
                if (weaponStack.getItem() instanceof ProjectileWeapon PW) {
                    ItemStack ammo = PW.searchForAmmo(Minecraft.getMinecraft().thePlayer, weaponStack);
                    if (ammo != null) {
                        return ammo;
                    }
                }
            }

            for (AmmoWeaponDefinition def : AmmoWeaponDefinition.values()) {
                Item arrow = def.validAmmo != null && def.validAmmo.length > 0 ? def.validAmmo[0] : Items.arrow;
                ItemStack ammo = new ItemStack(arrow);
                return ammo;
            }
            return null;
        }
    }

    /**
     * A declarative enum for defining and registering all known weapon compatibilities.
     */
    public enum AmmoWeaponDefinition {

        VANILLA_BOW(true, new Class[] { ItemBow.class }, Items.arrow),
        TINKERS_BOW(Mods.Tconstruct.isLoaded(), Crossbow.class, LongBow.class, ShortBow.class),
        // ExampleMod(true, com.examplemod.items.ItemModBow.class, com.examplemod.items.ItemModArrow);
        ;

        private final boolean shouldRegister;
        private final Class<? extends Item>[] weaponClasses;
        private final Item[] validAmmo;

        // get the ammo from the weapon's data
        AmmoWeaponDefinition(boolean shouldRegister, Class<? extends Item>... weaponClasses) {
            this(shouldRegister, weaponClasses, null);
        }

        AmmoWeaponDefinition(boolean shouldRegister, Class<? extends Item>[] weaponClasses, Item... validAmmo) {
            this.shouldRegister = shouldRegister;
            this.weaponClasses = weaponClasses;
            this.validAmmo = validAmmo;
        }

        public static void registerAll() {
            for (AmmoWeaponDefinition def : values()) {
                if (!def.shouldRegister) continue;

                AmmoHandler handler = new AmmoHandler();

                if (def.weaponClasses != null) {
                    register(def.weaponClasses, handler);
                }
            }
        }
    }

    static {
        AmmoWeaponDefinition.registerAll();
    }
}
