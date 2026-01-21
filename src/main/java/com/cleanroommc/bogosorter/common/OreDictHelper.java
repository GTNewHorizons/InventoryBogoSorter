package com.cleanroommc.bogosorter.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.common.config.SortRulesConfig;
import com.cleanroommc.bogosorter.compat.Mods;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.items.MetaGeneratedTool;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import tconstruct.library.util.IToolPart;
import vazkii.botania.common.item.brew.ItemBrewBase;

public class OreDictHelper {

    private static final Map<ItemStack, Set<String>> ORE_DICTS = new Object2ObjectOpenCustomHashMap<>(
        BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<ItemStack, String> MATERIALS = new Object2ObjectOpenCustomHashMap<>(
        BogoSortAPI.ITEM_META_HASH_STRATEGY);
    private static final Map<ItemStack, String> PREFIXES = new Object2ObjectOpenCustomHashMap<>(
        BogoSortAPI.ITEM_META_HASH_STRATEGY);

    private static final Map<String, String[]> orePrefixOwnerMap = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onItemRegistration(OreDictionary.OreRegisterEvent event) {
        ORE_DICTS.computeIfAbsent(event.Ore, key -> new ObjectOpenHashSet<>())
            .add(event.Name);

        String oreName = event.Name;
        // and try to transform registration name into OrePrefix + Material pair
        if (!SortRulesConfig.ORE_PREFIXES.containsKey(oreName)) {
            String material = null;
            String prefix = null;
            // split ore dict name to parts
            // oreBasalticMineralSand -> ore, Basaltic, Mineral, Sand
            ArrayList<String> splits = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (char character : oreName.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    if (builder.length() > 0) {
                        splits.add(builder.toString());
                        builder = new StringBuilder().append(character);
                    } else splits.add(Character.toString(character));
                } else builder.append(character);
            }
            if (builder.length() > 0) {
                splits.add(builder.toString());
            }
            // try to combine in different manners
            // oreBasaltic MineralSand , ore BasalticMineralSand
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < splits.size(); i++) {
                buffer.append(splits.get(i));
                String tryPrefix = buffer.toString();
                if (!SortRulesConfig.ORE_PREFIXES.containsKey(tryPrefix)) continue;
                prefix = tryPrefix;
                material = StringUtils.join(splits.subList(i + 1, splits.size()), StringUtils.EMPTY); // BasalticMineralSand
            }
            if (prefix != null && SortRulesConfig.ORE_PREFIXES.containsKey(prefix)) {
                MATERIALS.put(event.Ore, material);
                PREFIXES.put(event.Ore, prefix);
            }
        }
    }

    public static Set<String> getOreDicts(ItemStack item) {
        return ORE_DICTS.getOrDefault(item, Collections.emptySet());
    }

    public static String getMaterial(ItemStack item) {
        if (Mods.GT5u.isLoaded() && item.getItem() instanceof MetaGeneratedTool) {
            return getGtToolMaterial(item);
        }
        if (Mods.Tconstruct.isLoaded() && item.getItem() instanceof IToolPart) {
            return String.valueOf(((IToolPart) item.getItem()).getMaterialID(item));
        }
        return MATERIALS.get(item);
    }

    public static boolean getModCompoundTag(ItemStack brokenItem, ItemStack stack, ItemStack stack2) {
        if (Mods.GT5u.isLoaded() && brokenItem.getItem() instanceof MetaGeneratedTool) {
            return OreDictHelper.getGtToolMaterial(stack)
                .equals(OreDictHelper.getGtToolMaterial(stack2));
        }
        if (Mods.Botania.isLoaded() && brokenItem.getItem() instanceof ItemBrewBase) {
            return OreDictHelper.getBotaniabrew(stack)
                .equals(OreDictHelper.getBotaniabrew(stack2));
        }
        return false;
    }

    @Optional.Method(modid = "gregtech")
    @NotNull
    public static String getGtToolMaterial(ItemStack itemStack) {
        if (itemStack.stackTagCompound == null) {
            return "";
        }
        NBTTagCompound statsTag = itemStack.stackTagCompound.getCompoundTag("GT.ToolStats");
        if (statsTag == null) {
            return "";
        }
        if (statsTag.hasKey("PrimaryMaterial")) {
            return statsTag.getString("PrimaryMaterial");
        }
        return "";
    }

    @Optional.Method(modid = "Botania")
    @NotNull
    public static String getBotaniabrew(ItemStack itemStack) {
        NBTTagCompound statsTag = itemStack.getTagCompound();
        if (statsTag == null) {
            return "";
        }
        String Tag = statsTag.getString("brewKey");
        return Tag;
    }

    public static String getOrePrefix(ItemStack item) {
        return PREFIXES.get(item);
    }

    public static int getOrePrefixIndex(String prefix) {
        return SortRulesConfig.ORE_PREFIXES.containsKey(prefix) ? SortRulesConfig.ORE_PREFIXES.getInt(prefix)
            : Integer.MAX_VALUE;
    }

    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i))
                || (Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i))))) result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    private static void addOrePrefix(String[] owner, String... orePrefixes) {
        for (String prefix : orePrefixes) {
            if (orePrefixOwnerMap.containsKey(prefix)) {
                orePrefixOwnerMap.put(prefix, ArrayUtils.addAll(orePrefixOwnerMap.get(prefix), owner));
            } else {
                orePrefixOwnerMap.put(prefix, owner);
            }
        }
    }

    private static void addOrePrefix(String owner, String... orePrefixes) {
        addOrePrefix(new String[] { owner }, orePrefixes);
    }

    private static boolean isPrefixLoaded(String prefix) {
        String[] owners = orePrefixOwnerMap.get(prefix);
        if (owners == null || owners.length == 0) return true;
        for (String owner : owners) {
            if (Loader.isModLoaded(owner)) {
                return true;
            }
        }
        return false;
    }

    public static void init() {
        addOrePrefix(
            "gregtech",
            "ingotHot",
            "gemChipped",
            "gemFlawed",
            "gemFlawless",
            "gemExquisite",
            "dustTiny",
            "dustSmall",
            "plate",
            "plateDouble",
            "plateDense",
            "gear",
            "bolt",
            "stick",
            "stickLong",
            "ring",
            "screw",
            "round",
            "foil",
            "wireFine",
            "springSmall",
            "spring",
            "turbineBlade",
            "rotor",
            "lens",
            "dustImpure",
            "dustPure",
            "crushed",
            "crushedCentrifuged",
            "crushedPurified",
            "shard",
            "clump",
            "reduced",
            "crystalline",
            "dirtyGravel",
            "cleanGravel",
            "toolHeadSword",
            "toolHeadPickaxe",
            "toolHeadShovel",
            "toolHeadAxe",
            "toolHeadHoe",
            "toolHeadSense",
            "toolHeadFile",
            "toolHeadHammer",
            "toolHeadSaw",
            "toolHeadBuzzSaw",
            "toolHeadScrewdriver",
            "toolHeadDrill",
            "toolHeadChainsaw",
            "toolHeadWrench",
            "pipeTinyFluid",
            "pipeSmallFluid",
            "pipeNormalFluid",
            "pipeLargeFluid",
            "pipeHugeFluid",
            "pipeQuadrupleFluid",
            "pipeNonupleFluid",
            "pipeTinyItem",
            "pipeSmallItem",
            "pipeNormalItem",
            "pipeLargeItem",
            "pipeHugeItem",
            "pipeSmallRestrictive",
            "pipeNormalRestrictive",
            "pipeLargeRestrictive",
            "pipeHugeRestrictive",
            "wireGtSingle",
            "wireGtDouble",
            "wireGtQuadruple",
            "wireGtOctal",
            "wireGtHex",
            "cableGtSingle",
            "cableGtDouble",
            "cableGtQuadruple",
            "cableGtOctal",
            "cableGtHex",
            "frameGt",
            "oreGranite",
            "oreDiorite",
            "oreAndesite",
            "oreBlackgranite",
            "oreRedgranite",
            "oreMarble",
            "oreBasalt",
            "oreSand",
            "oreRedSand",
            "oreNetherrack",
            "oreEndstone");
        addOrePrefix("thermalfoundation", "gear", "stick", "plate", "fuel", "crystal", "rod", "coin");
        addOrePrefix("thaumcraft", "cluster", "oreCrystal");
        addOrePrefix("ic2", "plate", "plateDense", "crushed", "crushedPurified");
        addOrePrefix("immersiveengineering", "plate", "wire", "blockSheetmetal");
        addOrePrefix("enderio", "ball");

        String[] defaultOrePrefixOrder = { "ingot", "ingotHot", "gemChipped", "gemFlawed", "gem", "gemFlawless",
            "gemExquisite", "dustTiny", "dustSmall", "dust", "nugget", "block", "blockSheetmetal", "plate",
            "plateDouble", "plateDense", "gear", "bolt", "stick", "stickLong", "ring", "screw", "round", "foil",
            "wireFine", "wire", "springSmall", "spring", "ball", "crystal", "coin", "fuel", "turbineBlade", "rotor",
            "lens", "dustImpure", "dustPure", "crushed", "crushedCentrifuged", "crushedPurified", "shard", "clump",
            "reduced", "crystalline", "dirtyGravel", "cleanGravel", "toolHeadSword", "toolHeadPickaxe",
            "toolHeadShovel", "toolHeadAxe", "toolHeadHoe", "toolHeadSense", "toolHeadFile", "toolHeadHammer",
            "toolHeadSaw", "toolHeadBuzzSaw", "toolHeadScrewdriver", "toolHeadDrill", "toolHeadChainsaw",
            "toolHeadWrench", "pipeTinyFluid", "pipeSmallFluid", "pipeNormalFluid", "pipeLargeFluid", "pipeHugeFluid",
            "pipeQuadrupleFluid", "pipeNonupleFluid", "pipeTinyItem", "pipeSmallItem", "pipeNormalItem",
            "pipeLargeItem", "pipeHugeItem", "pipeSmallRestrictive", "pipeNormalRestrictive", "pipeLargeRestrictive",
            "pipeHugeRestrictive", "wireGtSingle", "wireGtDouble", "wireGtQuadruple", "wireGtOctal", "wireGtHex",
            "cableGtSingle", "cableGtDouble", "cableGtQuadruple", "cableGtOctal", "cableGtHex", "frameGt", "glass",
            "ore", "oreGranite", "oreDiorite", "oreAndesite", "oreBlackgranite", "oreRedgranite", "oreMarble",
            "oreBasalt", "oreSand", "oreRedSand", "oreNetherrack", "oreEndstone", "oreCrystal", "log", "rod" };

        SortRulesConfig.ORE_PREFIXES.clear();
        SortRulesConfig.ORE_PREFIXES_LIST.clear();
        int i = 0;
        for (String orePrefix : defaultOrePrefixOrder) {
            if (isPrefixLoaded(orePrefix)) {
                SortRulesConfig.ORE_PREFIXES.put(orePrefix, i++);
                SortRulesConfig.ORE_PREFIXES_LIST.add(orePrefix);
            }
        }
    }
}
