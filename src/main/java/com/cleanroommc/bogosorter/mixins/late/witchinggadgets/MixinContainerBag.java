package com.cleanroommc.bogosorter.mixins.late.witchinggadgets;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import witchinggadgets.common.gui.ContainerBag;

@Mixin(value = ContainerBag.class, remap = false)
public abstract class MixinContainerBag implements ISortableContainer {

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, 18, 6)
            .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_VERTICAL;
    }
}
