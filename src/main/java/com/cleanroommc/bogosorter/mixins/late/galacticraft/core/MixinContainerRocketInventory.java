package com.cleanroommc.bogosorter.mixins.late.galacticraft.core;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import micdoodle8.mods.galacticraft.api.entity.IRocketType;
import micdoodle8.mods.galacticraft.core.inventory.ContainerRocketInventory;

@Mixin(value = ContainerRocketInventory.class, remap = false)
public abstract class MixinContainerRocketInventory implements ISortableContainer {

    @Shadow
    @Final
    private IRocketType.EnumRocketType rocketType;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        int inventorySpace = rocketType.getInventorySpace() - 2;
        if (inventorySpace > 2) {
            builder.addSlotGroup(0, inventorySpace, 9);
        }
    }
}
