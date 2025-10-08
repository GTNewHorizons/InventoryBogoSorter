package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.settings.KeyBinding;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiKeyBindingList.KeyEntry.class)
public interface GuiKeyBindingListKeyEntryAccessor {

    /**
     * Provides access to the private 'keybinding' field (field_148282_b) inside the KeyEntry class.
     */
    @Accessor("field_148282_b")
    KeyBinding getKeybinding();

}
