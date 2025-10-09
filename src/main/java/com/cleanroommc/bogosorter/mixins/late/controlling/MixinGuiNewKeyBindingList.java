package com.cleanroommc.bogosorter.mixins.late.controlling;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.settings.KeyBinding;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.blamejared.controlling.client.gui.GuiNewKeyBindingList;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.client.keybinds.gui.BSButtonEntry;

@Mixin(GuiNewKeyBindingList.class)
public abstract class MixinGuiNewKeyBindingList {

    @Shadow(remap = false)
    @Final
    private Minecraft mc;
    @Shadow(remap = false)
    @Final
    private final List<GuiListExtended.IGuiListEntry> allEntries = new ArrayList<>();
    @Shadow(remap = false)
    private int maxListLabelWidth;

    @Shadow(remap = false)
    @Final
    private List<GuiListExtended.IGuiListEntry> displayedEntries;

    public MixinGuiNewKeyBindingList() {
        super();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // Find our dummy entry and replace it with our custom button entry.
        for (int i = 0; i < this.allEntries.size(); i++) {
            GuiListExtended.IGuiListEntry entry = this.allEntries.get(i);
            if (entry instanceof GuiNewKeyBindingList.KeyEntry) {
                // We need an accessor to get the private 'keybinding' field from the KeyEntry
                KeyBinding keybinding = ((GuiNewKeyBindingListKeyEntryAccessor) entry).getKeybinding();
                if (keybinding == BSKeybinds.BOGO_SORTER_CONTROLS_BUTTON) {
                    // Replace the entry with our custom button, passing the necessary info.
                    this.allEntries.set(i, new BSButtonEntry(this.mc, entry, this.maxListLabelWidth));
                    break;
                }
            }
        }

        // After modifying the master list, we must update the displayed list to reflect the changes.
        this.displayedEntries.clear();
        this.displayedEntries.addAll(this.allEntries);
    }
}
