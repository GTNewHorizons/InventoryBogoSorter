package com.cleanroommc.bogosorter.mixins.late.galacticraft.planets;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import micdoodle8.mods.galacticraft.planets.mars.inventory.ContainerSlimeling;

@Mixin(ContainerSlimeling.class)
public abstract class MixinContainerSlimeling implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        int inventorySize = builder.getContainer().inventorySlots.size();
        if (inventorySize > 37) {
            builder.addSlotGroup(37, inventorySize, 9)
                .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
        }

    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_VERTICAL;
    }
}
