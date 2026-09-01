package com.cleanroommc.bogosorter.common.sort;

import java.util.Comparator;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.Nullable;

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
        if (Mods.Thaumcraft.isLoaded()) {
            // sorts essentia containers (phials, jars, ...) by their aspect
            api.registerNbtSortingRule(
                "tc_essentia",
                "Aspects",
                Constants.NBT.TAG_LIST,
                Comparator.naturalOrder(),
                DefaultRules::getEssentiaAspect);
        }
        if (Mods.Forestry.isLoaded()) {
            // sorts genetic items (bees, larvae, trees, butterflies) by primary species
            api.registerNbtSortingRule(
                "forestry_genome",
                "Genome/Chromosomes",
                Constants.NBT.TAG_LIST,
                Comparator.naturalOrder(),
                DefaultRules::getForestrySpecies);
        }
    }

    private static String getPotionId(NBTBase nbt) {
        String[] potion = ((NBTTagString) nbt).toString()
            .split(":");
        return potion[potion.length - 1];
    }

    /**
     * Returns the aspect tag stored in a Thaumcraft {@code Aspects} list (e.g. {@code ignis}).
     */
    @Nullable
    private static String getEssentiaAspect(NBTBase nbt) {
        NBTTagList aspects = (NBTTagList) nbt;
        if (aspects.tagCount() == 0) return null;
        return aspects.getCompoundTagAt(0)
            .getString("key");
    }

    /**
     * Returns the primary species UID of a Forestry genome (chromosome at {@code Slot 0}, e.g.
     * {@code forestry.speciesForest}).
     */
    @Nullable
    private static String getForestrySpecies(NBTBase nbt) {
        NBTTagList chromosomes = (NBTTagList) nbt;
        for (int i = 0; i < chromosomes.tagCount(); i++) {
            NBTTagCompound chromosome = chromosomes.getCompoundTagAt(i);
            if (chromosome.getByte("Slot") == 0) {
                return chromosome.getString("UID0");
            }
        }
        return null;
    }
}
