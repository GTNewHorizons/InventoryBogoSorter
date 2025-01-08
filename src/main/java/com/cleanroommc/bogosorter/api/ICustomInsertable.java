package com.cleanroommc.bogosorter.api;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public interface ICustomInsertable {

    ItemStack insert(Container container, List<ISlot> slots, ItemStack itemStack, boolean emptyOnly);
}
