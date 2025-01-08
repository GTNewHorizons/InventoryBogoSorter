package com.cleanroommc.bogosorter.common.dropoff;

import java.util.Comparator;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

class ItemStackComparator implements Comparator<ItemStack> {

    @Override
    public int compare(@Nonnull ItemStack left, @Nonnull ItemStack right) {
        return left.getDisplayName()
            .compareToIgnoreCase(right.getDisplayName());
    }

}
