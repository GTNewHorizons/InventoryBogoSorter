package com.cleanroommc.bogosorter.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.client.usageticker.UsageTicker;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.dropoff.DropOffButtonHandler;
import com.cleanroommc.bogosorter.common.dropoff.DropOffHandler;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.core.BogoSorterCore;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class BogoSorterConfig {

    public static final List<SortRule<ItemStack>> sortRules = new ArrayList<>();
    public static final List<NbtSortRule> nbtSortRules = new ArrayList<>();

    public static final Object2IntOpenHashMap<String> ORE_PREFIXES = new Object2IntOpenHashMap<>();
    public static final List<String> ORE_PREFIXES_LIST = new ArrayList<>();

    public static int buttonColor = 0xFFFFFFFF;

    @SideOnly(Side.CLIENT)
    public static void save(JsonObject json) {
        PlayerConfig playerConfig = PlayerConfig.getClient();
        JsonObject general = new JsonObject();
        general.addProperty("enableHotbarSort", playerConfig.enableHotbarSort);
        general.addProperty("enableAutoRefill", playerConfig.enableAutoRefill);
        general.addProperty("refillDmgThreshold", playerConfig.autoRefillDamageThreshold);
        general.addProperty("enableDropoff", DropOffHandler.enableDropOff);
        general.addProperty("dropoffRender", DropOffHandler.dropoffRender);
        general.addProperty("dropoffChatMessage", DropOffHandler.dropoffChatMessage);
        general.addProperty("dropoffQuotaInMS", DropOffHandler.dropoffQuotaInMS);
        general.addProperty("dropoffPacketThrottleInMS", DropOffHandler.dropoffPacketThrottleInMS);
        general.addProperty("dropoffTargetNames", DropOffHandler.dropoffTargetNames);
        general.addProperty("dropoffButtonShow", DropOffButtonHandler.showButton);
        general.addProperty("dropoffButtonX", DropOffButtonHandler.buttonX);
        general.addProperty("dropoffButtonY", DropOffButtonHandler.buttonY);
        general.addProperty("enableHotbarSwap", HotbarSwap.isEnabled());
        general.addProperty("sortSound", SortHandler.getSortSoundName());
        general.addProperty("buttonColor", "#" + Integer.toHexString(buttonColor));
        // general.addProperty("_comment", "By setting the chance below to 0 you agree to have no humor and that you are
        // boring.");

        json.add("General", general);

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

        JsonObject usageTicker = new JsonObject();
        usageTicker.addProperty("enableModule", UsageTicker.enableModule);
        usageTicker.addProperty("enableMainHand", UsageTicker.enableMainHand);
        usageTicker.addProperty("enableOffHand", UsageTicker.enableOffHand);
        usageTicker.addProperty("enableArmor", UsageTicker.enableArmor);
        json.add("UsageTicker", usageTicker);
    }

    @SideOnly(Side.CLIENT)
    public static void load(JsonObject json) {
        PlayerConfig playerConfig = PlayerConfig.getClient();
        if (json.has("General")) {
            JsonObject general = json.getAsJsonObject("General");
            playerConfig.enableHotbarSort = JsonHelper.getBoolean(general, true, "enableHotbarSort");
            playerConfig.enableAutoRefill = JsonHelper.getBoolean(general, true, "enableAutoRefill");
            playerConfig.autoRefillDamageThreshold = (short) JsonHelper.getInt(general, 1, "refillDmgThreshold");
            DropOffHandler.enableDropOff = JsonHelper.getBoolean(general, true, "enableDropoff");
            DropOffHandler.dropoffRender = JsonHelper.getBoolean(general, true, "dropoffRender");
            DropOffHandler.dropoffChatMessage = JsonHelper.getBoolean(general, true, "dropoffChatMessage");
            DropOffHandler.dropoffQuotaInMS = JsonHelper.getInt(general, 1, "dropoffQuotaInMS");
            DropOffHandler.dropoffPacketThrottleInMS = JsonHelper.getInt(general, 500, "dropoffPacketThrottleInMS");
            DropOffHandler.dropoffTargetNames = JsonHelper
                .getString(general, "*Chest*, *Barrel*, *Drawer*, *Crate*", "dropoffTargetNames");
            DropOffButtonHandler.showButton = JsonHelper.getBoolean(general, true, "dropoffButtonShow");
            DropOffButtonHandler.buttonX = JsonHelper.getInt(general, 160, "dropoffButtonX");
            DropOffButtonHandler.buttonY = JsonHelper.getInt(general, 5, "dropoffButtonY");
            HotbarSwap.setEnabled(JsonHelper.getBoolean(general, true, "enableHotbarSwap"));
            SortHandler.sortSound = JsonHelper
                .getElement(general, new ResourceLocation("gui.button.press"), element -> {
                    if (element.isJsonNull()) return null;
                    ResourceLocation soundEvent = new ResourceLocation(element.getAsString());
                    return soundEvent != null ? soundEvent : new ResourceLocation("gui.button.press");
                }, "sortSound");
            buttonColor = JsonHelper.getColor(general, 0xFFFFFFFF, "buttonColor");
        }
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
                    BogoSorterConfig.sortRules.add(rule);
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
                    BogoSorterConfig.nbtSortRules.add(rule);
                }
            }
        }
        if (json.has("UsageTicker")) {
            JsonObject ticker = json.getAsJsonObject("UsageTicker");
            UsageTicker.enableModule = JsonHelper.getBoolean(ticker, true, "enableModule");
            UsageTicker.enableMainHand = JsonHelper.getBoolean(ticker, true, "enableMainHand");
            UsageTicker.enableOffHand = JsonHelper.getBoolean(ticker, true, "enableOffHand");
            UsageTicker.enableArmor = JsonHelper.getBoolean(ticker, true, "enableArmor");
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
