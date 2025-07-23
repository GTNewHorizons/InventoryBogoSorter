package com.cleanroommc.bogosorter.api;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

public interface ICustomInsertable {

    ItemStack insert(Container container, List<SlotAccessor> slots, ItemStack itemStack, boolean emptyOnly);
}
