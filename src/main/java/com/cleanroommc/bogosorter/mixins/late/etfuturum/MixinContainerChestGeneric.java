package com.cleanroommc.bogosorter.mixins.late.etfuturum;

import net.minecraft.inventory.IInventory;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import ganymedes01.etfuturum.inventory.ContainerChestGeneric;

@Mixin(value = ContainerChestGeneric.class, remap = false)
public abstract class MixinContainerChestGeneric implements ISortableContainer {

    @Shadow
    @Final
    private IInventory chestInventory;

    @Shadow
    @Final
    private int rowSize;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, chestInventory.getSizeInventory(), rowSize);
    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        // Normal ShulkerBox/Barrel Will be top right horizontal just like a chest
        if (chestInventory.getSizeInventory() == 27) {
            return IPosSetter.TOP_RIGHT_HORIZONTAL;
        }
        return IPosSetter.TOP_RIGHT_VERTICAL;
    }
}
