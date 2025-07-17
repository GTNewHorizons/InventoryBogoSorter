package com.cleanroommc.bogosorter.common.sort;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISlotGroup;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

public class SlotGroup implements ISlotGroup {

    public static final SlotGroup EMPTY = new SlotGroup(Collections.emptyList(), 0);

    private final boolean player;
    private final boolean hotbar;
    private final List<SlotAccessor> slots;
    private final int rowSize;
    private int priority;
    private IPosSetter posSetter;

    public SlotGroup(List<SlotAccessor> slots, int rowSize) {
        this(false, false, slots, rowSize);
    }

    public SlotGroup(boolean player, boolean hotbar, List<SlotAccessor> slots, int rowSize) {
        this.player = player;
        this.hotbar = player && hotbar;
        this.slots = Collections.unmodifiableList(slots);
        this.rowSize = rowSize;
        this.priority = 0;
        this.posSetter = IPosSetter.TOP_RIGHT_HORIZONTAL;
    }

    @Override
    public @UnmodifiableView List<SlotAccessor> getSlots() {
        return this.slots;
    }

    @Override
    public int getRowSize() {
        return this.rowSize;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean isPlayerInventory() {
        return this.player;
    }

    public boolean isHotbar() {
        return this.hotbar;
    }

    @Nullable
    public IPosSetter getPosSetter() {
        return posSetter;
    }

    @Override
    public ISlotGroup priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ISlotGroup buttonPosSetter(@Nullable IPosSetter posSetter) {
        this.posSetter = posSetter;
        return this;
    }

    public boolean hasSlot(int slotNumber) {
        for (SlotAccessor slot : getSlots()) {
            if (slot.getSlotNumber() == slotNumber) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return getSlots().isEmpty();
    }
}
