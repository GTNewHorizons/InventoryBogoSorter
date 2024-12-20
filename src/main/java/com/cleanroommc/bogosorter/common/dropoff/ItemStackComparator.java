package com.cleanroommc.bogosorter.common.dropoff;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Comparator;

class ItemStackComparator implements Comparator<ItemStack> {

    @Override
    public int compare(@Nonnull ItemStack left, @Nonnull ItemStack right) {
        return left.getDisplayName().compareToIgnoreCase(right.getDisplayName());
    }

}
