package com.cleanroommc.bogosorter.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.core.BogoSorterCore;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class SortRulesConfig {

    public static final List<SortRule<ItemStack>> sortRules = new ArrayList<>();
    public static final List<NbtSortRule> nbtSortRules = new ArrayList<>();

    public static final Object2IntOpenHashMap<String> ORE_PREFIXES = new Object2IntOpenHashMap<>();
    public static final List<String> ORE_PREFIXES_LIST = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    public static void save(JsonObject json) {
        JsonArray jsonRules = new JsonArray();
        for (SortRule<ItemStack> rule : sortRules) {
            if (rule != BogoSortAPI.EMPTY_ITEM_SORT_RULE) {
                JsonObject ruleJson = new JsonObject();
                ruleJson.addProperty("name", rule.getKey());
                ruleJson.addProperty("inverted", rule.isInverted());
                jsonRules.add(ruleJson);
            }
        }
        json.add("ItemSortRules", jsonRules);

        jsonRules = new JsonArray();
        for (NbtSortRule rule : nbtSortRules) {
            if (rule != BogoSortAPI.EMPTY_NBT_SORT_RULE) {
                JsonObject ruleJson = new JsonObject();
                ruleJson.addProperty("name", rule.getKey());
                ruleJson.addProperty("inverted", rule.isInverted());
                jsonRules.add(ruleJson);
            }
        }
        json.add("NbtSortRules", jsonRules);

    }

    @SideOnly(Side.CLIENT)
    public static void load(JsonObject json) {
        sortRules.clear();
        if (json.has("ItemSortRules")) {
            JsonArray sortRules = json.getAsJsonArray("ItemSortRules");
            for (JsonElement jsonElement : sortRules) {
                String key;
                boolean inverted = false;
                if (jsonElement.isJsonObject()) {
                    key = JsonHelper.getString(jsonElement.getAsJsonObject(), "", "key", "name");
                    inverted = JsonHelper
                        .getBoolean(jsonElement.getAsJsonObject(), false, "inverted", "ascending", "asc");
                } else {
                    key = jsonElement.getAsString();
                }
                SortRule<ItemStack> rule = BogoSortAPI.INSTANCE.getItemSortRule(key);
                if (rule.isEmpty()) {
                    BogoSorterCore.LOGGER.error("Could not find item sort rule with key '{}'.", key);
                } else {
                    rule.setInverted(inverted);
                    SortRulesConfig.sortRules.add(rule);
                }
            }
        }
        nbtSortRules.clear();
        if (json.has("NbtSortRules")) {
            JsonArray sortRules = json.getAsJsonArray("NbtSortRules");
            for (JsonElement jsonElement : sortRules) {
                String key;
                boolean inverted = false;
                if (jsonElement.isJsonObject()) {
                    key = JsonHelper.getString(jsonElement.getAsJsonObject(), "", "key", "name");
                    inverted = JsonHelper
                        .getBoolean(jsonElement.getAsJsonObject(), false, "inverted", "ascending", "asc");
                } else {
                    key = jsonElement.getAsString();
                }
                NbtSortRule rule = BogoSortAPI.INSTANCE.getNbtSortRule(key);
                if (rule.isEmpty()) {
                    BogoSorterCore.LOGGER.error("Could not find nbt sort rule with key '{}'.", key);
                } else {
                    rule.setInverted(inverted);
                    SortRulesConfig.nbtSortRules.add(rule);
                }
            }
        }
    }

    public static void saveOrePrefixes(JsonObject json) {
        json.addProperty("_comment", "Setting this to true will recreate this entire file on next start");
        json.addProperty("reload", false);
        JsonArray orePrefixes = new JsonArray();
        json.add("orePrefixes", orePrefixes);
        for (String orePrefix : ORE_PREFIXES_LIST) {
            // Convert the String to a JsonPrimitive
            JsonElement jsonElement = new JsonPrimitive(orePrefix);
            orePrefixes.add(jsonElement);
        }
    }

    public static void loadOrePrefixes(JsonObject json) {
        if (json.has("reload") && json.get("reload")
            .getAsBoolean()) {
            Serializer.saveOrePrefixes();
            return;
        }
        if (json.has("orePrefixes")) {
            ORE_PREFIXES.clear();
            ORE_PREFIXES_LIST.clear();
            int i = 0;
            for (JsonElement jsonElement : json.getAsJsonArray("orePrefixes")) {
                if (jsonElement.isJsonPrimitive()) {
                    String orePrefix = jsonElement.getAsString();
                    ORE_PREFIXES.put(orePrefix, i++);
                    ORE_PREFIXES_LIST.add(orePrefix);
                }
            }
        }
    }

    public static void loadDefaultRules() {
        String[] itemRules = { "mod", "material", "ore_prefix", "id", "meta", "nbt_has", "nbt_rules" };
        String[] nbtRules = { "enchantment", "enchantment_book", "potion", "gt_circ_config", "gt_item_damage" };

        sortRules.addAll(
            Arrays.stream(itemRules)
                .map(BogoSortAPI.INSTANCE::getItemSortRule)
                .filter(rule -> rule != BogoSortAPI.EMPTY_ITEM_SORT_RULE)
                .collect(Collectors.toList()));
        nbtSortRules.addAll(
            Arrays.stream(nbtRules)
                .map(BogoSortAPI.INSTANCE::getNbtSortRule)
                .filter(rule -> rule != BogoSortAPI.EMPTY_NBT_SORT_RULE)
                .collect(Collectors.toList()));
    }
}
