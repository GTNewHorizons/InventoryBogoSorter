package com.cleanroommc.bogosorter.common.dropoff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.cleanroommc.bogosorter.BogoSorter;

import cpw.mods.fml.common.registry.GameRegistry;

public class LocalizationHelper {

    // Cache to hold translations for each mod, keyed by modId
    private static final Map<String, Map<String, String>> translationCache = new HashMap<>();

    /**
     * Gets the English translation (name) for an ItemStack, checking mod translations first.
     */
    public static String getDisplayNameEnglish(ItemStack itemStack) {
        if (itemStack == null || itemStack.field_151002_e == null) {
            return "Unknown";
        }
        String unlocalizedName = itemStack.getUnlocalizedName() + ".name";
        String modId = getModIdFromItemStack(itemStack);
        // Try to get the cached translations for the mod
        Map<String, String> modTranslations = fetchModTranslation(modId);
        if (modTranslations.containsKey(unlocalizedName)) {
            return modTranslations.get(unlocalizedName); // Return the cached translation
        }

        return StatCollector.translateToFallback(unlocalizedName); // Fallback to default translation
    }

    /**
     * Get the mod ID that registered the item
     */
    private static String getModIdFromItemStack(ItemStack stack) {
        if (stack == null) return "minecraft";
        Item item = stack.getItem();
        if (item == null) return "minecraft";

        // Use GameRegistry to get the mod domain
        if (GameRegistry.findUniqueIdentifierFor(item) != null) {
            return GameRegistry.findUniqueIdentifierFor(item).modId;
        }

        return "minecraft";
    }

    /**
     * Fetches the translations for a specific mod, checking the cache first.
     */
    private static Map<String, String> fetchModTranslation(String modId) {
        // Check if translations are already cached for this mod
        if (translationCache.containsKey(modId)) {
            return translationCache.get(modId); // Return the cached translations
        }

        // If translations are not cached, load and cache them
        Map<String, String> modTranslations = loadModLanguageFile(modId);
        translationCache.put(modId, modTranslations);
        return modTranslations;
    }

    /**
     * Loads the language file (e.g., en_US.lang) for a specific mod.
     */
    private static Map<String, String> loadModLanguageFile(String modId) {
        Map<String, String> translations = new HashMap<>();
        String resourcePath = "/assets/" + modId.toLowerCase() + "/lang/en_US.lang";
        InputStream stream = LocalizationHelper.class.getResourceAsStream(resourcePath);
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (!line.startsWith("#") && line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            translations.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            } catch (IOException | NullPointerException e) {
                BogoSorter.LOGGER.error("Failed to load en_US.lang for mod: {}", modId);
                e.printStackTrace();
            }
        }
        return translations;
    }
}
