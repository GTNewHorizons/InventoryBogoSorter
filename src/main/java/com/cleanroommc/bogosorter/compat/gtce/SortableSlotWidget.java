//package com.cleanroommc.bogosorter.compat.gtce;
//
//import gregtech.api.gui.widgets.SlotWidget;
//import net.minecraft.inventory.IInventory;
//import net.minecraftforge.items.IItemHandler;
//import net.minecraftforge.items.IItemHandlerModifiable;
//import org.jetbrains.annotations.Nullable;
//
//public class SortableSlotWidget extends SlotWidget {
//
//    @Nullable
//    private String sortArea;
//
//    public SortableSlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
//        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
//    }
//
//    public SortableSlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
//        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
//    }
//
//    public SortableSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
//        super(itemHandler, slotIndex, xPosition, yPosition);
//    }
//
//    public SortableSlotWidget(IInventory inventory, int slotIndex, int xPosition, int yPosition) {
//        super(inventory, slotIndex, xPosition, yPosition);
//    }
//
//    public SortableSlotWidget setSortArea(@Nullable String sortArea) {
//        this.sortArea = sortArea;
//        return this;
//    }
//
//    public @Nullable String getSortArea() {
//        return sortArea;
//    }
//}
