package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiKeyBindingList;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.settings.KeyBinding;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.client.keybinds.gui.BSButtonEntry;

@Mixin(GuiKeyBindingList.class)
public abstract class MixinGuiKeyBindingList extends GuiListExtended {

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private IGuiListEntry[] field_148190_m; // listEntries
    @Shadow
    private int field_148188_n; // maxListLabelWidth

    public MixinGuiKeyBindingList(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        for (int i = 0; i < this.field_148190_m.length; i++) {
            IGuiListEntry entry = this.field_148190_m[i];
            // Check if the entry is a KeyEntry before attempting to access its keybind
            if (entry instanceof GuiKeyBindingList.KeyEntry) {
                KeyBinding keybinding = ((GuiKeyBindingListKeyEntryAccessor) entry).getKeybinding();
                if (keybinding == BSKeybinds.BOGO_SORTER_CONTROLS_BUTTON) {
                    // Replace the entry in the list with our custom button.
                    this.field_148190_m[i] = new BSButtonEntry(this.mc, entry, this.field_148188_n);
                    break;
                }
            }
        }
    }
}
