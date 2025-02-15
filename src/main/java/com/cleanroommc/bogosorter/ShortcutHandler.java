package com.cleanroommc.bogosorter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.common.network.CShortcut;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.compat.loader.Mods;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import codechicken.lib.inventory.SlotDummy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ShortcutHandler {

    public static boolean SetCanTakeStack;

    @SideOnly(Side.CLIENT)
    public static boolean moveSingleItem(GuiContainer guiContainer, boolean emptySlot) {
        Slot slot = guiContainer.theSlot;
        if (slot == null || slot.getStack() == null) return false;
        NetworkHandler.sendToServer(
            new CShortcut(emptySlot ? CShortcut.Type.MOVE_SINGLE_EMPTY : CShortcut.Type.MOVE_SINGLE, slot.slotNumber));
        return true;
    }

    public static void moveSingleItem(EntityPlayer player, Container container, ISlot slot, boolean emptySlot) {
        moveItemStack(player, container, slot, emptySlot, 1);
    }

    public static void moveItemStack(EntityPlayer player, Container container, ISlot slot, boolean emptySlot,
        int amount) {
        if (slot == null || slot.bogo$getStack() == null) return;
        ItemStack stack = slot.bogo$getStack();
        amount = Math.min(amount, stack.getMaxStackSize());
        ItemStack toInsert = stack.copy();
        toInsert.stackSize = (amount);

        if (BogoSortAPI.isValidSortable(container)) {
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

            SlotGroup slots = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
            SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup()
                : sortingContext.getPlayerSlotGroup();
            if (otherSlots == null || slots == otherSlots) return;

            toInsert = emptySlot ? BogoSortAPI.insert(container, otherSlots.getSlots(), toInsert, true)
                : BogoSortAPI.insert(container, otherSlots.getSlots(), toInsert);
        } else {
            List<ISlot> otherSlots = new ArrayList<>();
            boolean isPlayer = BogoSortAPI.isPlayerSlot(slot);
            for (Slot slot1 : container.inventorySlots) {
                if (isPlayer != BogoSortAPI.isPlayerSlot(slot1) && isPlayer != SlotDummy(slot1)) {
                    otherSlots.add(BogoSortAPI.INSTANCE.getSlot(slot1));
                }
            }
            toInsert = emptySlot ? BogoSortAPI.insert(container, otherSlots, toInsert, true)
                : BogoSortAPI.insert(container, otherSlots, toInsert);
        }
        if (toInsert == null) {
            toInsert = stack.copy();
            toInsert.stackSize -= (amount);
            if (toInsert.stackSize == 0) {
                toInsert = null;
            }
            slot.bogo$putStack(toInsert);
            // needed for crafting tables
            slot.bogo$onSlotChanged(stack, toInsert);
            // I hope im doing this right
            toInsert = stack.copy();
            toInsert.stackSize = (amount);
            slot.bogo$onTake(player, toInsert);
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean moveAllItems(GuiContainer guiContainer, boolean sameItemOnly) {
        Container container = guiContainer.inventorySlots;
        Slot slot = guiContainer.theSlot;
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return false;
        ISlot iSlot = BogoSortAPI.INSTANCE.getSlot(slot);
        if (sameItemOnly && iSlot.bogo$getStack() == null) return false;
        SetCanTakeStack = false;
        NetworkHandler.sendToServer(
            new CShortcut(
                sameItemOnly ? CShortcut.Type.MOVE_ALL_SAME : CShortcut.Type.MOVE_ALL,
                iSlot.bogo$getSlotNumber()));
        return true;
    }

    public static void moveAllItems(EntityPlayer player, Container container, ISlot slot, boolean sameItemOnly) {
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return;
        if (slot.bogo$getStack() != null) {
            ItemStack stack = slot.bogo$getStack()
                .copy();
            if (sameItemOnly && stack == null) return;
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

            SlotGroup slots = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
            SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup()
                : sortingContext.getPlayerSlotGroup();
            if (slots == null || otherSlots == null || slots == otherSlots) return;
            for (ISlot slot1 : slots.getSlots()) {
                ItemStack stackInSlot = slot1.bogo$getStack();
                if (stackInSlot == null || (sameItemOnly && !stackInSlot.isItemEqual(stack))) continue;
                ItemStack copy = stackInSlot.copy();
                ItemStack remainder = BogoSortAPI.insert(container, otherSlots.getSlots(), copy);
                if (remainder == null) {
                    slot1.bogo$putStack(null);
                } else {
                    int inserted = stackInSlot.stackSize - remainder.stackSize;
                    if (inserted > 0) {
                        slot1.bogo$putStack(remainder.copy());
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean dropItems(GuiContainer guiContainer, boolean onlySameType) {
        Slot slot = guiContainer.theSlot;
        if (slot == null || slot.getStack() == null) return false;
        if (!BogoSortAPI.isPlayerSlot(slot) && !BogoSortAPI.isValidSortable(guiContainer.inventorySlots)) return false;
        NetworkHandler.sendToServer(
            new CShortcut(onlySameType ? CShortcut.Type.DROP_ALL_SAME : CShortcut.Type.DROP_ALL, slot.slotNumber));
        return true;
    }

    public static void dropItems(EntityPlayer player, Container container, ISlot slot, boolean onlySameType) {
        ItemStack stack = slot.bogo$getStack();
        if (onlySameType && stack == null) return;
        SlotGroup slots = GuiSortingContext.getOrCreate(container)
            .getSlotGroup(slot.bogo$getSlotNumber());
        if (slots == null) return;
        for (ISlot slot1 : slots.getSlots()) {
            ItemStack stackInSlot = slot1.bogo$getStack();
            if (stackInSlot != null && (!onlySameType || stackInSlot.isItemEqual(stack))) {
                slot1.bogo$putStack(null);
                player.dropPlayerItemWithRandomChoice(stackInSlot, true);
            }
        }
        return;
    }

    public static ItemStack insertToSlots(List<ISlot> slots, ItemStack stack, boolean emptyOnly) {
        for (ISlot slot : slots) {
            stack = insert(slot, stack, emptyOnly);
            if (stack == null) return stack;
        }
        return stack;
    }

    public static ItemStack insert(ISlot slot, ItemStack stack, boolean emptyOnly) {
        ItemStack stackInSlot = slot.bogo$getStack();
        if (emptyOnly) {
            if (stackInSlot != null || !slot.bogo$isItemValid(stack)) return stack;
            int amount = Math.min(stack.stackSize, slot.bogo$getItemStackLimit(stack));
            if (amount <= 0) return stack;
            ItemStack newStack = stack.copy();
            newStack.stackSize = (amount);
            stack.stackSize -= (amount);
            slot.bogo$putStack(newStack);
            return stack.stackSize == 0 ? null : stack;
        }
        if (stackInSlot != null && ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
            int amount = Math.min(
                slot.bogo$getItemStackLimit(stackInSlot),
                Math.min(stack.stackSize, slot.bogo$getMaxStackSize(stack) - stackInSlot.stackSize));
            if (amount <= 0) return stack;
            stack.stackSize -= (amount);
            stackInSlot.stackSize += (amount);
            slot.bogo$putStack(stackInSlot);
            return stack.stackSize == 0 ? null : stack;
        }
        return stack;
    }

    public static boolean SlotDummy(Slot slot) {
        if (Mods.CodeChickenCore.isLoaded() && slot instanceof SlotDummy) {
            return true;
        }
        // Prevent items from being moved into the output slot, which leads to them vanishing
        if (slot instanceof SlotCrafting) {
            return true;
        }
        return false;
    }
}
