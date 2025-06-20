package com.cleanroommc.bogosorter.common.sort;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.compat.Mods;

public class DefaultRules {

    public static void init(BogoSortAPI api) {
        api.registerItemSortingRule("mod", ItemCompareHelper::compareMod);
        api.registerItemSortingRule("id", ItemCompareHelper::compareId);
        api.registerItemSortingRule("meta", ItemCompareHelper::compareMeta);
        api.registerItemSortingRule("registry_order", ItemCompareHelper::compareRegistryOrder);
        api.registerClientItemSortingRule(
            "display_name",
            ItemCompareHelper::compareDisplayName,
            ItemCompareHelper::compareDisplayName);
        api.registerItemSortingRule("nbt_size", ItemCompareHelper::compareNbtSize);
        api.registerItemSortingRule("nbt_has", ItemCompareHelper::compareHasNbt);
        api.registerItemSortingRule("nbt_rules", ItemCompareHelper::compareNbtValues);
        api.registerItemSortingRule("nbt_all_values", ItemCompareHelper::compareNbtAllValues);
        api.registerItemSortingRule("count", ItemCompareHelper::compareCount);
        api.registerItemSortingRule("ore_dict", ItemCompareHelper::compareOreDict);
        api.registerItemSortingRule("material", ItemCompareHelper::compareMaterial);
        api.registerItemSortingRule("ore_prefix", ItemCompareHelper::compareOrePrefix);
        api.registerItemSortingRule("burn_time", ItemCompareHelper::compareBurnTime);
        api.registerItemSortingRule("block_type", ItemCompareHelper::compareBlockType);
        api.registerItemSortingRule("hunger", ItemCompareHelper::compareHunger);
        api.registerItemSortingRule("saturation", ItemCompareHelper::compareSaturation);
        api.registerClientItemSortingRule("color", ItemCompareHelper::compareColor, ItemCompareHelper::compareColor);

        if (Mods.ProjectE.isLoaded()) {
            api.registerItemSortingRule("emc", ItemCompareHelper::compareEMC);
        }

        api.registerNbtSortingRule(
            "potion",
            "Potion",
            Constants.NBT.TAG_STRING,
            ItemCompareHelper::comparePotionId,
            DefaultRules::getPotionId);
        api.registerNbtSortingRule(
            "enchantment",
            "ench",
            Constants.NBT.TAG_LIST,
            ItemCompareHelper::compareEnchantments,
            nbtBase -> (NBTTagList) nbtBase);
        api.registerNbtSortingRule(
            "enchantment_book",
            "StoredEnchantments",
            Constants.NBT.TAG_LIST,
            ItemCompareHelper::compareEnchantments,
            nbtBase -> (NBTTagList) nbtBase);
        if (Mods.GT5u.isLoaded()) {
            api.registerNbtSortingRule("gt_circ_config", "Configuration", Constants.NBT.TAG_INT);
            api.registerNbtSortingRule("gt_item_damage", "GT.ToolStats/Dmg", Constants.NBT.TAG_INT);
        }
    }

    private static String getPotionId(NBTBase nbt) {
        String[] potion = ((NBTTagString) nbt).toString()
            .split(":");
        return potion[potion.length - 1];
    }
}
