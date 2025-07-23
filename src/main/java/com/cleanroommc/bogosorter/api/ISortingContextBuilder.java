package com.cleanroommc.bogosorter.api;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

/**
 * A helper interface to create {@link ISlotGroup} instances.
 * Meant to be used in {@link ISortableContainer#buildSortingContext(ISortingContextBuilder)}
 */
@ApiStatus.NonExtendable
public interface ISortingContextBuilder {

    /**
     * Creates and registers a slot group with a list of slots.
     *
     * @param slots   slot list
     * @param rowSize This is mostly used to determine the button position with {@link IPosSetter#TOP_RIGHT_VERTICAL}
     *                and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is not rectangular,
     *                try to use the row size of the first row.
     * @return the created slot group
     */
    ISlotGroup addSlotGroupOf(List<Slot> slots, int rowSize);

    /**
     * Creates and registers a slot group with a list of slots.
     *
     * @param slots   slot list
     * @param rowSize This is mostly used to determine the button position with {@link IPosSetter#TOP_RIGHT_VERTICAL}
     *                and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is not rectangular,
     *                try to use the row size of the first row.
     * @return the created slot group
     */
    ISlotGroup addSlotGroup(List<SlotAccessor> slots, int rowSize);

    /**
     * Creates and registers a slot group based on a start and end index.
     *
     * @param startIndex index of the first slot (including)
     * @param endIndex   index of the end slot (excluding)
     * @param rowSize    This is mostly used to determine the button position with {@link IPosSetter#TOP_RIGHT_VERTICAL}
     *                   and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is not rectangular,
     *                   try to use the row size of the first row.
     * @return the created slot group
     */
    ISlotGroup addSlotGroup(int startIndex, int endIndex, int rowSize);

    /**
     * Creates and registers a generic slot group. It assumes that all non player slots belong to the same group and
     * that the slot group has a rectangular shape.
     *
     * @return the created slot group or a dummy if not enough slots where found.
     */
    ISlotGroup addGenericSlotGroup();

    Container getContainer();
}
