package com.cleanroommc.bogosorter.client.usageticker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.compat.Mods;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.weaponry.ProjectileWeapon;
import tconstruct.weaponry.TinkerWeaponry;
import vazkii.botania.common.item.ModItems;

/**
 * A registry to handle the logic for different types of weapons that use ammunition.
 * This provides a centralized place to add compatibility for various items.
 */
@SideOnly(Side.CLIENT)
public class AmmoHandlerRegistry {

    private static final Map<Item, AmmoHandler> HANDLERS = new LinkedHashMap<>();

    /**
     * Registers a new handler for a specific weapon item.
     *
     * @param weapon  The of the weapon Item.
     * @param handler The handler containing the logic for this weapon.
     */
    public static void register(Item weapon, AmmoHandler handler) {
        if (weapon != null && handler != null) {
            HANDLERS.put(weapon, handler);
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

        private List<Item> validAmmo = new ArrayList<>();

        public SimpleAmmoHandler() {}

        public void addAmmo(Item validAmmo) {
            this.validAmmo.add(validAmmo);
        }

        @Override
        int getAmmoCount(EntityClientPlayerMP player, ItemStack weaponStack) {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, weaponStack) > 0){
                return 0;
            }
            int count = 0;
            for (ItemStack invStack : player.inventory.mainInventory) {
                if (invStack != null && this.validAmmo.contains(invStack.getItem())) {
                    count += invStack.stackSize;
                }

            }
            return count;
        }

        @Override
        ItemStack getDisplayStack(ItemStack weaponStack) {
            if (this.validAmmo != null && !this.validAmmo.isEmpty()) {
                for (ItemStack invStack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
                    if (invStack != null && this.validAmmo.contains(invStack.getItem())){
                        return invStack;
                    }
                }
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
        SimpleAmmoHandler simpleHandler = new SimpleAmmoHandler();
        simpleHandler.addAmmo(Items.arrow);
        // add mod arrows
//        if (Mods.Etfuturum.isLoaded()) simpleHandler.addAmmo(ganymedes01.etfuturum.ModItems.TIPPED_ARROW.get());
        register(Items.bow, simpleHandler);

        if(Mods.DraconicEvolution.isLoaded()){
            register(com.brandon3055.draconicevolution.common.ModItems.wyvernBow,simpleHandler);
            register(com.brandon3055.draconicevolution.common.ModItems.draconicBow,simpleHandler);
        }


        if (Mods.Botania.isLoaded()){
            register(ModItems.livingwoodBow, simpleHandler);
        }
        if (Mods.Tconstruct.isLoaded()) {
            TConstructCompat.register();
        }
    }
}
