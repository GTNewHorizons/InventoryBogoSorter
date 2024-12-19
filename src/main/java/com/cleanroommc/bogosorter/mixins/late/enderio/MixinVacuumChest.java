package com.cleanroommc.bogosorter.mixins.late.enderio;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import crazypants.enderio.machine.vacuum.ContainerVacuumChest;

@Mixin(ContainerVacuumChest.class)
public abstract class MixinVacuumChest implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(1, 28, 9);
    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_VERTICAL;
    }
}
