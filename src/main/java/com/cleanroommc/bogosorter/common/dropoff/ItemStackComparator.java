package com.cleanroommc.bogosorter.common.dropoff;

import cpw.mods.fml.common.Loader;
import invtweaks.api.InvTweaksAPI;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

class ItemStackComparator implements Comparator<ItemStack> {

    @Override
    public int compare(@Nonnull ItemStack left, @Nonnull ItemStack right) {
        return left.getDisplayName().compareToIgnoreCase(right.getDisplayName());
    }

}
