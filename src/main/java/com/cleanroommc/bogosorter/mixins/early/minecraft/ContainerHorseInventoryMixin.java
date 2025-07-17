package com.cleanroommc.bogosorter.mixins.early.minecraft;

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.inventory.ContainerHorseInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

@Mixin(ContainerHorseInventory.class)
public abstract class ContainerHorseInventoryMixin implements ISortableContainer {

    @Shadow
    private EntityHorse theHorse;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        if (theHorse.isChested()) {
            builder.addSlotGroup(2, 17, 5);
        }
    }
}
