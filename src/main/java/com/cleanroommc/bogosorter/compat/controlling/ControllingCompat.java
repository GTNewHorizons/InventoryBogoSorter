package com.cleanroommc.bogosorter.compat.controlling;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.blamejared.controlling.keybinding.ComboKeyBinding;
import com.cleanroommc.bogosorter.BogoSorter;

public final class ControllingCompat {

    private ControllingCompat() {}

    public static boolean isModifierActive(KeyBinding key) {
        return key instanceof ComboKeyBinding combo && combo.controlling$isModifierActive();
    }

    public static void setDefaultPinChord(KeyBinding key) {
        if (!(key instanceof ComboKeyBinding combo)) return;
        boolean wasDefault = combo.controlling$isSetToDefaultValue();
        try {
            List<Integer> chord = Collections.singletonList(Keyboard.KEY_LCONTROL);
            key.getClass()
                .getMethod("controlling$setDefaultComboKeys", List.class)
                .invoke(key, chord);
            if (wasDefault) {
                key.getClass()
                    .getMethod("controlling$setComboKeys", List.class)
                    .invoke(key, chord);
            }
        } catch (NoSuchMethodException ignored) {
            setLegacyControlModifier(key, wasDefault);
        } catch (ReflectiveOperationException e) {
            BogoSorter.LOGGER.warn("Could not configure the Controlling pin-slot chord", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setLegacyControlModifier(KeyBinding key, boolean wasDefault) {
        try {
            Class<? extends Enum> type = (Class<? extends Enum>) Class
                .forName("com.blamejared.controlling.keybinding.KeyModifier");
            Object control = Enum.valueOf(type, "CONTROL");
            key.getClass()
                .getMethod("controlling$setDefaultKeyModifier", type)
                .invoke(key, control);
            if (wasDefault) {
                key.getClass()
                    .getMethod("controlling$setKeyModifier", type)
                    .invoke(key, control);
            }
        } catch (ReflectiveOperationException e) {
            BogoSorter.LOGGER.warn("Could not configure the legacy Controlling pin-slot modifier", e);
        }
    }
}
