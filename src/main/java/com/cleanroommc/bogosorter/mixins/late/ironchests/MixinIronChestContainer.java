package com.cleanroommc.bogosorter.mixins.late.ironchests;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import cpw.mods.ironchest.ContainerIronChest;
import cpw.mods.ironchest.IronChestType;

@Mixin(value = ContainerIronChest.class, remap = false)
public abstract class MixinIronChestContainer implements ISortableContainer {

    @Shadow
    private IronChestType type;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        if (type != IronChestType.DIRTCHEST9000) {
            builder.addSlotGroup(0, (type.getRowCount() * type.getRowLength()), type.getRowLength())
                .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
        }
    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_VERTICAL;
    }
}
