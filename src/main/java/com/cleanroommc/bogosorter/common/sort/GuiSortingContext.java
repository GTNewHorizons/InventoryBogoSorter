package com.cleanroommc.bogosorter.common.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.ISlotGroup;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

import it.unimi.dsi.fastutil.ints.IntArraySet;

public class GuiSortingContext {

    private static Container currentContainer;
    private static GuiSortingContext currentSortingContext;

    public static GuiSortingContext getOrCreate(Container container) {
        if (currentContainer != container) {
            currentSortingContext = create(container);
            currentContainer = container;
        }
        return currentSortingContext;
    }

    public static GuiSortingContext create(Container container) {
        GuiSortingContext.Builder builder = new GuiSortingContext.Builder(container);
        addPlayerInventory(builder, container);

        if (container instanceof ISortableContainer) {
            ((ISortableContainer) container).buildSortingContext(builder);
        } else if (BogoSortAPI.isValidSortable(container)) {
            BogoSortAPI.INSTANCE.getBuilder(container)
                .accept(container, builder);
        }
        return builder.build();
    }

    private final Container container;
    private final List<SlotGroup> slotGroups;
    private final boolean hasPlayer;

    public GuiSortingContext(Container container, List<SlotGroup> slotGroups, boolean hasPlayer) {
        slotGroups.sort(Comparator.comparingInt(SlotGroup::getPriority));
        this.container = container;
        this.slotGroups = slotGroups;
        this.hasPlayer = hasPlayer;
    }

    @Nullable
    public SlotGroup getSlotGroup(int id) {
        for (SlotGroup slotGroup : this.slotGroups) {
            if (slotGroup.hasSlot(id)) return slotGroup;
        }
        return null;
    }

    @Nullable
    public SlotGroup getNonPlayerSlotGroup() {
        for (SlotGroup slotGroup : this.slotGroups) {
            if (!isEmpty() && !slotGroup.isPlayerInventory()) return slotGroup;
        }
        return null;
    }

    @Nullable
    public SlotGroup getPlayerSlotGroup() {
        for (SlotGroup slotGroup : this.slotGroups) {
            if (slotGroup.isPlayerInventory() && !slotGroup.isHotbar()) return slotGroup;
        }
        return null;
    }

    public int getNonPlayerSlotGroupAmount() {
        if (this.hasPlayer) {
            return this.slotGroups.size() - 1;
        }
        return this.slotGroups.size();
    }

    public Container getContainer() {
        return container;
    }

    public List<SlotGroup> getSlotGroups() {
        return slotGroups;
    }

    public boolean hasPlayer() {
        return hasPlayer;
    }

    public boolean isEmpty() {
        return this.slotGroups.isEmpty();
    }

    public static class Builder implements ISortingContextBuilder {

        private final Container container;
        private final List<SlotGroup> slots = new ArrayList<>();
        private boolean player = false;

        public Builder(Container container) {
            this.container = container;
        }

        @Override
        public ISlotGroup addSlotGroupOf(List<Slot> slots, int rowSize) {
            return addSlotGroup(
                slots.stream()
                    .map(BogoSortAPI.INSTANCE::getSlot)
                    .collect(Collectors.toList()),
                rowSize);
        }

        @Override
        public ISlotGroup addSlotGroup(List<SlotAccessor> slots, int rowSize) {
            if (slots.size() < rowSize) {
                throw new IllegalArgumentException(
                    "Slots must at least fill 1 row! Expected at least " + rowSize
                        + " slot, but only found "
                        + slots.size());
            }
            if (slots.size() < 2) {
                throw new IllegalArgumentException("Slot group must have at least 2 slots!");
            }
            SlotGroup slotGroup = new SlotGroup(slots, rowSize);
            this.slots.add(slotGroup);
            return slotGroup;
        }

        @Override
        public ISlotGroup addSlotGroup(int startIndex, int endIndex, int rowSize) {
            return addSlotGroupOf(this.container.inventorySlots.subList(startIndex, endIndex), rowSize);
        }

        @Override
        public ISlotGroup addGenericSlotGroup() {
            List<SlotAccessor> slots = new ArrayList<>();
            IntArraySet xValues = new IntArraySet();
            for (Slot slot : this.container.inventorySlots) {
                SlotAccessor slotAccessor = BogoSortAPI.INSTANCE.getSlot(slot);
                if (!BogoSortAPI.isPlayerSlot(slotAccessor)) {
                    slots.add(slotAccessor);
                    xValues.add(slotAccessor.bogo$getX());
                }
            }
            if (slots.size() < 2) {
                return SlotGroup.EMPTY;
            }
            return addSlotGroup(slots, xValues.size());
        }

        @Override
        public Container getContainer() {
            return this.container;
        }

        public GuiSortingContext build() {
            return new GuiSortingContext(container, slots, player);
        }
    }

    private static void addPlayerInventory(GuiSortingContext.Builder builder, Container container) {
        List<SlotAccessor> slots = new ArrayList<>();
        List<SlotAccessor> hotbar = new ArrayList<>();
        for (Slot slot : container.inventorySlots) {
            if (BogoSortAPI.isPlayerSlot(slot)) {
                if (slot.getSlotIndex() < 9) hotbar.add(BogoSortAPI.INSTANCE.getSlot(slot));
                else slots.add(BogoSortAPI.INSTANCE.getSlot(slot));
            }
        }
        if (!slots.isEmpty()) {
            SlotGroup slotGroup = new SlotGroup(true, false, slots, Math.min(9, slots.size()));
            slotGroup.priority(-10000)
                .buttonPosSetter(BogoSortAPI.INSTANCE.getPlayerButtonPos(container));
            builder.slots.add(slotGroup);
            builder.player = true;
        }
        if (!hotbar.isEmpty()) {
            SlotGroup slotGroup = new SlotGroup(true, true, hotbar, Math.min(9, hotbar.size()));
            slotGroup.priority(-10000)
                .buttonPosSetter(null);
            builder.slots.add(slotGroup);
            builder.player = true;
        }
    }
}
