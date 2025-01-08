package com.cleanroommc.bogosorter.common.sort;

import java.util.Comparator;

import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.api.SortRule;

public class ClientItemSortRule extends SortRule<ItemStack> {

    private final Comparator<ItemSortContainer> serverComparator;

    public ClientItemSortRule(String key, Comparator<ItemStack> comparator,
        Comparator<ItemSortContainer> serverComparator) {
        super(key, comparator);
        this.serverComparator = serverComparator;
    }

    public int compareServer(ItemSortContainer o1, ItemSortContainer o2) {
        return isInverted() ? serverComparator.compare(o2, o1) : serverComparator.compare(o1, o2);
    }
}
