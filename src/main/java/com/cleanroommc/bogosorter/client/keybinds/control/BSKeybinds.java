package com.cleanroommc.bogosorter.client.keybinds.control;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.client.keybinds.KeyBind;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines and manages all custom multi-key combinations.
 * This system is self-contained and uses a tick-based KeyBind implementation.
 */
@SideOnly(Side.CLIENT)
public class BSKeybinds {

    // --- Mouse Button Constants (as used by the KeyBind class) ---
    public static final int MOUSE_LEFT = -100;
    public static final int MOUSE_RIGHT = -99;
    public static final int MOUSE_MIDDLE = -98;

    public static final KeyBinding sortKeyInGUI = new KeyBinding(
        "key.sort_gui",
        MOUSE_MIDDLE,
        "key.categories.bogosorter");
    public static final KeyBinding sortKeyOutsideGUI = new KeyBinding(
        "key.sort_nogui",
        Keyboard.KEY_NONE,
        "key.categories.bogosorter");
    public static final KeyBinding configGuiKey = new KeyBinding(
        "key.sort_config",
        Keyboard.KEY_NONE,
        "key.categories.bogosorter");

    public static final KeyBinding dropoffKey = new KeyBinding(
        "key.dropoff",
        Keyboard.KEY_NONE,
        "key.categories.bogosorter");
    /**
     * A "dummy" keybinding that will be found and replaced with a button.
     */
    public static final KeyBinding BOGO_SORTER_CONTROLS_BUTTON = new KeyBinding(
        "key.bogosorter.controls_button",
        Keyboard.KEY_NONE,
        "key.categories.bogosorter");

    public static final KeybindDefinition MOVE_ALL_SAME = new KeybindDefinition(
        "move_all_same",
        "key.bogosorter.move_all_same",
        MOUSE_LEFT,
        Keyboard.KEY_LMENU);
    public static final KeybindDefinition MOVE_ALL = new KeybindDefinition(
        "move_all",
        "key.bogosorter.move_all",
        MOUSE_LEFT,
        Keyboard.KEY_SPACE);
    public static final KeybindDefinition MOVE_SINGLE = new KeybindDefinition(
        "move_single",
        "key.bogosorter.move_single",
        MOUSE_LEFT,
        Keyboard.KEY_LCONTROL);
    public static final KeybindDefinition MOVE_SINGLE_EMPTY = new KeybindDefinition(
        "move_single_empty",
        "key.bogosorter.move_single_empty",
        MOUSE_RIGHT,
        Keyboard.KEY_LCONTROL);
    public static final KeybindDefinition THROW_ALL_SAME = new KeybindDefinition(
        "throw_all_same",
        "key.bogosorter.throw_all_same",
        () -> isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop),
        Keyboard.KEY_LMENU);
    public static final KeybindDefinition THROW_ALL = new KeybindDefinition(
        "throw_all",
        "key.bogosorter.throw_all",
        () -> isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop),
        Keyboard.KEY_SPACE);

    // --- Internal Storage ---
    private static final Map<KeybindDefinition, List<Integer>> keyCombos = new HashMap<>();
    private static final Map<KeybindDefinition, KeyBind> activeKeyBinds = new HashMap<>();

    private static final KeybindDefinition[] ALL_KEYBINDS = new KeybindDefinition[] { MOVE_ALL_SAME, MOVE_ALL,
        MOVE_SINGLE, MOVE_SINGLE_EMPTY, THROW_ALL_SAME, THROW_ALL };
    private static File configFile;

    /**
     * Initializes the config, key combo maps, and builds the active KeyBind objects.
     */
    public static void init(File suggestedConfigFile) {
        configFile = new File(suggestedConfigFile.getParentFile(), "bogosorter/keybinds.json");
        loadKeyCombos();
    }

    /**
     * Rebuilds the active KeyBind objects from the current key combo configurations.
     */
    public static void rebuildKeyBinds() {
        activeKeyBinds.clear();
        for (Map.Entry<KeybindDefinition, List<Integer>> entry : keyCombos.entrySet()) {
            KeybindDefinition keySetting = entry.getKey();
            List<Integer> keys = entry.getValue();
            int[] keyCodes = keys.stream()
                .mapToInt(i -> i)
                .toArray();

            if (keyCodes.length == 0) {
                continue;
            }

            KeyBind.Builder builder = KeyBind.builder(keySetting.getName())
                .mustPress(keyCodes);
            if (keySetting.getValidator() != null) {
                builder.validator(keySetting.getValidator());
            }
            KeyBind keyBind = builder.build();

            activeKeyBinds.put(keySetting, keyBind);
        }
    }

    public static KeybindDefinition[] getAllKeybinds() {
        return ALL_KEYBINDS;
    }

    // --- Public Accessor Methods (used by the GUI and for checking) ---

    public static List<Integer> getKeyCombo(KeybindDefinition keybindDef) {
        return keyCombos.computeIfAbsent(keybindDef, k -> new ArrayList<>(k.getDefaultKeyCodes()));
    }

    public static String getComboDisplayString(KeybindDefinition keybindDef) {
        List<Integer> combo = getKeyCombo(keybindDef);
        if (combo.isEmpty()) {
            return "NONE";
        }
        return combo.stream()
            .map(keyCode -> {
                if (keyCode < 0) { // Mouse buttons
                    switch (keyCode) {
                        case MOUSE_LEFT:
                            return "LMB";
                        case MOUSE_RIGHT:
                            return "RMB";
                        case MOUSE_MIDDLE:
                            return "MMB";
                        default:
                            return "Mouse " + (keyCode + 100);
                    }
                }
                return Keyboard.getKeyName(keyCode); // Keyboard keys
            })
            .collect(Collectors.joining(" + "));
    }

    public static boolean isDefault(KeybindDefinition keybindDef) {
        return getKeyCombo(keybindDef).equals(keybindDef.getDefaultKeyCodes());
    }

    public static void resetToDefault(KeybindDefinition keybindDef) {
        keyCombos.put(keybindDef, new ArrayList<>(keybindDef.getDefaultKeyCodes()));
        rebuildKeyBinds();
    }

    /**
     * Retrieves the active, tick-based KeyBind object for a given definition.
     * This allows for more advanced state checking, such as isFirstPress().
     *
     * @param keybindDef The keybind definition to look up.
     * @return The corresponding KeyBind object, or null if it doesn't exist.
     */
    @Nullable
    public static KeyBind getActiveKeyBind(KeybindDefinition keybindDef) {
        return activeKeyBinds.get(keybindDef);
    }

    private static boolean isButtonPressed(int button) {
        return Mouse.getEventButtonState() && Mouse.getEventButton() == button;
    }

    private static boolean isKeyDown(KeyBinding key) {
        if (key.getKeyCode() == 0) return false;

        if (key.getKeyCode() < 0) {
            return isButtonPressed(key.getKeyCode() + 100);
        }
        return Keyboard.getEventKeyState() && Keyboard.getEventKey() == key.getKeyCode();
    }

    // --- Config Methods ---

    public static void loadKeyCombos() {
        if (configFile == null) return;

        Map<String, List<Integer>> loadedCombos = new HashMap<>();
        JsonElement jsonElement = Serializer.loadJson(configFile);

        if (jsonElement != null && jsonElement.isJsonObject()) {
            Type type = new TypeToken<Map<String, List<Integer>>>() {}.getType();
            loadedCombos = Serializer.gson.fromJson(jsonElement, type);
        }

        boolean needsSave = !configFile.exists();
        for (KeybindDefinition def : ALL_KEYBINDS) {
            List<Integer> combo = loadedCombos.get(def.getName());
            if (combo != null) {
                keyCombos.put(def, combo);
            } else {
                keyCombos.put(def, new ArrayList<>(def.getDefaultKeyCodes()));
                needsSave = true;
            }
        }

        if (needsSave) {
            saveKeyCombos();
        }

        rebuildKeyBinds();
    }

    public static void saveKeyCombos() {
        if (configFile == null) return;

        Map<String, List<Integer>> dataToSave = new HashMap<>();
        for (Map.Entry<KeybindDefinition, List<Integer>> entry : keyCombos.entrySet()) {
            dataToSave.put(
                entry.getKey()
                    .getName(),
                entry.getValue());
        }

        JsonElement jsonElement = Serializer.gson.toJsonTree(dataToSave);
        Serializer.saveJson(configFile, jsonElement);

        rebuildKeyBinds();
    }

    /**
     * A simple, immutable class to define a keybind action.
     * This replaces the need for net.minecraft.client.settings.KeyBinding.
     */
    public static class KeybindDefinition {

        private final String name;
        private final String unlocalizedName;
        private final List<Integer> defaultKeyCodes;
        private final BooleanSupplier validator;

        public KeybindDefinition(String name, String unlocalizedName, Integer... defaultKeys) {
            this(name, unlocalizedName, null, defaultKeys);
        }

        public KeybindDefinition(String name, String unlocalizedName, BooleanSupplier validator,
            Integer... defaultKeys) {
            this.name = name;
            this.unlocalizedName = unlocalizedName;
            this.defaultKeyCodes = new ArrayList<>(Arrays.asList(defaultKeys));
            this.validator = validator;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return I18n.format(unlocalizedName);
        }

        public List<Integer> getDefaultKeyCodes() {
            return defaultKeyCodes;
        }

        public BooleanSupplier getValidator() {
            return validator;
        }
    }
}
