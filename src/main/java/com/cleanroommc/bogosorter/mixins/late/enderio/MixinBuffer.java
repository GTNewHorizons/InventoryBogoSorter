package com.cleanroommc.bogosorter.mixins.late.enderio;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import crazypants.enderio.machine.buffer.ContainerBuffer;

@Mixin(ContainerBuffer.class)
public abstract class MixinBuffer implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, 9, 3)
            .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);

    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_VERTICAL;
    }
}
