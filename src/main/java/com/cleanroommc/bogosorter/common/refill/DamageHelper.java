package com.cleanroommc.bogosorter.common.refill;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ISpecialArmor;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;

public class DamageHelper {

    public static boolean damageItemHook(EntityPlayer player, ItemStack itemStack) {
        PlayerConfig playerConfig = PlayerConfig.get(player);
        if (!playerConfig.enableAutoRefill || playerConfig.autoRefillDamageThreshold <= 0) return false;

        if (RefillHandler.shouldHandleRefill(player, itemStack) && isNotArmor(itemStack)) {
            ItemStack handItem = player.getHeldItem();
            if (handItem != itemStack) {
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
        return true;
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
        return item != null && item.hasTagCompound()
            && item.getTagCompound()
                .getBoolean("Unbreakable");
    }
}
