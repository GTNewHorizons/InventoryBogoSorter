package com.cleanroommc.bogosorter.compat.controlling;

import net.minecraft.client.settings.KeyBinding;

import com.blamejared.controlling.api.ComboModifier;
import com.blamejared.controlling.api.ControllingApi;
import com.blamejared.controlling.keybinding.ComboKeyBinding;

public final class ControllingCompat {

    private ControllingCompat() {}

    public static boolean isModifierActive(KeyBinding key) {
        return key instanceof ComboKeyBinding combo && combo.controlling$isModifierActive();
    }

    public static void setDefaultPinChord(KeyBinding key) {
        ControllingApi.setDefaultComboKeyBinding(key, ComboModifier.CONTROL);
    }
}
