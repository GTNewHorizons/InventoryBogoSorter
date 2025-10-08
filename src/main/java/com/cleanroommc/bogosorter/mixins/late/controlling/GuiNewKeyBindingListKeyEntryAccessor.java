package com.cleanroommc.bogosorter.mixins.late.controlling;

import net.minecraft.client.settings.KeyBinding;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.blamejared.controlling.client.gui.GuiNewKeyBindingList;

@Mixin(GuiNewKeyBindingList.KeyEntry.class)
public interface GuiNewKeyBindingListKeyEntryAccessor {

    /**
     * Provides access to the private 'keybinding' inside the KeyEntry class.
     */
    @Accessor(remap = false, value = "keybinding")
    KeyBinding getKeybinding();
}
