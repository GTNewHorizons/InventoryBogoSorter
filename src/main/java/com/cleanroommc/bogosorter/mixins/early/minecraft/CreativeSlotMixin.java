package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.inventory.GuiContainerCreative$CreativeSlot")
public abstract class CreativeSlotMixin extends Slot { // Check to see if it works

    private CreativeSlotMixin(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void constructor(GuiContainerCreative this$0, Slot p_i1087_2_, int p_i1087_3_, CallbackInfo ci) {
        slotNumber = p_i1087_2_.slotNumber;
    }
}
