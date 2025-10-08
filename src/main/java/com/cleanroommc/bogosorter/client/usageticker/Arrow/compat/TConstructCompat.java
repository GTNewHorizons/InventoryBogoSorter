package com.cleanroommc.bogosorter.client.usageticker.Arrow.compat;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.client.usageticker.Arrow.ArrowHandlerRegistry;

import tconstruct.library.weaponry.ProjectileWeapon;
import tconstruct.weaponry.TinkerWeaponry;

/**
 * A dedicated compatibility class for Tinker's Construct.
 * This class is only loaded if TConstruct is present, preventing crashes.
 */
public class TConstructCompat {

    public static void register() {
        TConstructAmmoHandler handler = new TConstructAmmoHandler();
        ArrowHandlerRegistry.register(TinkerWeaponry.shortbow, handler);
        ArrowHandlerRegistry.register(TinkerWeaponry.longbow, handler);
        ArrowHandlerRegistry.register(TinkerWeaponry.crossbow, handler);
    }

    private static class TConstructAmmoHandler extends ArrowHandlerRegistry.ArrowHandler {

        // This is to refresh the counter for the usage ticker
        @Override
        public int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack) {
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
        public ItemStack getDisplayStack(EntityClientPlayerMP player, ItemStack weaponStack) {
            if (weaponStack.getItem() instanceof ProjectileWeapon projectileWeapon) {
                return projectileWeapon.searchForAmmo(player, weaponStack);
            }
            return null;
        }
    }
}
