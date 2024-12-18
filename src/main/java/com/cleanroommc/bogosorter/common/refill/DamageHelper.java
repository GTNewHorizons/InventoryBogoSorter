package com.cleanroommc.bogosorter.common.refill;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import net.minecraft.client.Minecraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ISpecialArmor;


public class DamageHelper {

    public static boolean damageItemHook(ItemStack itemStack) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        PlayerConfig playerConfig = PlayerConfig.get(player);
        if (!playerConfig.enableAutoRefill || playerConfig.autoRefillDamageThreshold <= 0) return false;

        if (RefillHandler.shouldHandleRefill(player, itemStack) && isNotArmor(itemStack)) {
            ItemStack handItem = player.getHeldItem();
            if (handItem != itemStack) {
//                handItem = player.getHeldItemOffhand();
//                if (handItem != itemStack) {
//                    return false;
//                }
                return false;
            }

            int durabilityLeft = itemStack.getMaxDamage() - itemStack.getItemDamage();
            if (durabilityLeft >= 0 && durabilityLeft < playerConfig.autoRefillDamageThreshold) {
                return RefillHandler.handle(player.inventory.currentItem, itemStack, player, true);
            }
        }
        return false;
    }

    private static boolean isNotArmor(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ISpecialArmor) return false;
//        EntityEquipmentSlot slot = itemStack.getItem().getEquipmentSlot(itemStack);
//        return slot == null || slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND;
        return false;
    }

    public static int getDurability(ItemStack item) {
        if (item == null) return 0;
        if (item.getMaxDamage() <= 0) return 0;
        if (isUnbreakable(item)) {
            return item.getMaxDamage() + 1;
        }
        return item.getMaxDamage() - item.getItemDamage() + 1;
    }

    public static boolean isUnbreakable(ItemStack item) {
        return item != null && item.hasTagCompound() && item.getTagCompound().getBoolean("Unbreakable");
    }
}
