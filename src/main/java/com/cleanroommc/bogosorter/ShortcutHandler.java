package com.cleanroommc.bogosorter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.bogosorter.common.network.CShortcut;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import appeng.container.slot.AppEngCraftingSlot;
import buildcraft.factory.gui.SlotWorkbench;
import codechicken.lib.inventory.SlotDummy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.core.gui.slots.SlotCraftMatrix;
import tconstruct.tools.inventory.CraftingStationContainer;

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

    public static void moveSingleItem(EntityPlayer player, Container container, SlotAccessor slot, boolean emptySlot) {
        moveItemStack(player, container, slot, emptySlot, 1);
    }

    public static void moveItemStack(EntityPlayer player, Container container, SlotAccessor slot, boolean emptySlot,
        int amount) {
        if (slot == null || slot.callGetStack() == null) return;

        ItemStack stack = slot.callGetStack();
        if (stack.stackSize <= 0) return;

        Slot currentSlot = container.getSlot(slot.getSlotNumber());
        if (currentSlot == null) return;
        if (SlotDummyOrCrafting(currentSlot)) {
            return;
        }
        amount = Math.min(amount, stack.getMaxStackSize());
        ItemStack toInsert = stack.copy();
        toInsert.stackSize = (amount);

        if (BogoSortAPI.isValidSortable(container)) {
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);

            List<SlotAccessor> shortcutSlots = getContainerShortcutSlots(container);
            List<SlotAccessor> targetSlots = getShortcutTargetSlots(slot, sortingContext, shortcutSlots);
            if (targetSlots == null || containsSlot(targetSlots, slot)) return;

            toInsert = emptySlot ? BogoSortAPI.insert(container, targetSlots, toInsert, true)
                : BogoSortAPI.insert(container, targetSlots, toInsert);
        } else {
            List<SlotAccessor> otherSlots = new ArrayList<>();
            boolean isPlayer = BogoSortAPI.isPlayerSlot(slot);
            for (Slot slot1 : container.inventorySlots) {
                if (isPlayer != BogoSortAPI.isPlayerSlot(slot1) && isPlayer != SlotDummyOrCrafting(slot1)) {
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
            slot.callPutStack(toInsert);
            // needed for crafting tables
            slot.callOnSlotChange(stack, toInsert);
            // I hope im doing this right
            toInsert = stack.copy();
            toInsert.stackSize = (amount);
            slot.callOnPickupFromSlot(player, toInsert);
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean moveAllItems(GuiContainer guiContainer, boolean sameItemOnly) {
        Slot slot = guiContainer.theSlot;
        if (slot == null) return false;
        SlotAccessor slotAccessor = BogoSortAPI.INSTANCE.getSlot(slot);
        if (sameItemOnly && slotAccessor.callGetStack() == null) return false;
        SetCanTakeStack = false;
        NetworkHandler.sendToServer(
            new CShortcut(
                sameItemOnly ? CShortcut.Type.MOVE_ALL_SAME : CShortcut.Type.MOVE_ALL,
                slotAccessor.getSlotNumber()));
        return true;
    }

    public static void moveAllItems(Container container, SlotAccessor slot, boolean sameItemOnly) {
        if (slot == null || !BogoSortAPI.isValidSortable(container)) return;
        Slot currentSlot = container.getSlot(slot.getSlotNumber());
        if (currentSlot == null || SlotDummyOrCrafting(currentSlot)) return;
        if (slot.callGetStack() == null) return;

        ItemStack stack = slot.callGetStack()
            .copy();
        List<SlotAccessor> sourceSlots;
        List<SlotAccessor> targetSlots;

        if (BogoSortAPI.isValidSortable(container)) {
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);
            List<SlotAccessor> shortcutSlots = getContainerShortcutSlots(container);
            SlotGroup slotGroup = sortingContext.getSlotGroup(slot.getSlotNumber());
            targetSlots = getShortcutTargetSlots(slot, sortingContext, shortcutSlots);
            if (shortcutSlots != null && containsSlot(shortcutSlots, slot)) {
                sourceSlots = shortcutSlots;
            } else if (slotGroup != null) {
                sourceSlots = slotGroup.getSlots();
            } else {
                return;
            }
            if (targetSlots == null || containsSlot(targetSlots, slot)) return;
        } else {
            boolean isPlayer = BogoSortAPI.isPlayerSlot(slot);
            sourceSlots = new ArrayList<>();
            targetSlots = new ArrayList<>();
            for (Slot slot1 : container.inventorySlots) {
                if (SlotDummyOrCrafting(slot1)) continue;
                if (isPlayer == BogoSortAPI.isPlayerSlot(slot1)) {
                    sourceSlots.add(BogoSortAPI.INSTANCE.getSlot(slot1));
                } else {
                    targetSlots.add(BogoSortAPI.INSTANCE.getSlot(slot1));
                }
            }
        }

        for (SlotAccessor slot1 : sourceSlots) {
            ItemStack stackInSlot = slot1.callGetStack();
            if (stackInSlot == null || (sameItemOnly && !stackInSlot.isItemEqual(stack))) continue;
            ItemStack copy = stackInSlot.copy();
            ItemStack remainder = BogoSortAPI.insert(container, targetSlots, copy);
            if (remainder == null) {
                slot1.callPutStack(null);
            } else {
                int inserted = stackInSlot.stackSize - remainder.stackSize;
                if (inserted > 0) {
                    slot1.callPutStack(remainder.copy());
                }
            }
        }
    }

    @Nullable
    private static List<SlotAccessor> getShortcutTargetSlots(SlotAccessor source, GuiSortingContext sortingContext,
        @Nullable List<SlotAccessor> shortcutSlots) {
        if (shortcutSlots != null) {
            if (BogoSortAPI.isPlayerSlot(source)) {
                return shortcutSlots;
            }
            if (containsSlot(shortcutSlots, source)) {
                SlotGroup playerSlots = sortingContext.getPlayerSlotGroup();
                return playerSlots == null ? null : playerSlots.getSlots();
            }
        }
        SlotGroup targetGroup = BogoSortAPI.isPlayerSlot(source) ? sortingContext.getNonPlayerSlotGroup()
            : sortingContext.getPlayerSlotGroup();
        return targetGroup == null ? null : targetGroup.getSlots();
    }

    @Nullable
    private static List<SlotAccessor> getContainerShortcutSlots(Container container) {
        if (Mods.Tconstruct.isLoaded() && container instanceof CraftingStationContainer craftingStation) {
            List<SlotAccessor> slots = new ArrayList<>(craftingStation.craftMatrix.getSizeInventory());
            for (Slot slot : container.inventorySlots) {
                if (slot.inventory == craftingStation.craftMatrix) {
                    slots.add(BogoSortAPI.INSTANCE.getSlot(slot));
                }
            }
            return slots;
        }
        return null;
    }

    private static boolean containsSlot(List<SlotAccessor> slots, SlotAccessor target) {
        for (SlotAccessor slot : slots) {
            if (slot.getSlotNumber() == target.getSlotNumber()) return true;
        }
        return false;
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

    public static void dropItems(EntityPlayer player, Container container, SlotAccessor slot, boolean onlySameType) {
        ItemStack stack = slot.callGetStack();
        if (onlySameType && stack == null) return;
        Slot currentSlot = container.getSlot(slot.getSlotNumber());
        if (currentSlot == null) return;
        if (SlotDummyOrCrafting(currentSlot)) {
            return;
        }
        List<SlotAccessor> sourceSlots = getContainerShortcutSlots(container);
        if (sourceSlots == null || !containsSlot(sourceSlots, slot)) {
            SlotGroup slotGroup = GuiSortingContext.getOrCreate(container)
                .getSlotGroup(slot.getSlotNumber());
            if (slotGroup == null) return;
            sourceSlots = slotGroup.getSlots();
        }
        for (SlotAccessor slot1 : sourceSlots) {
            ItemStack stackInSlot = slot1.callGetStack();
            if (stackInSlot != null && (!onlySameType || stackInSlot.isItemEqual(stack))) {
                slot1.callPutStack(null);
                player.dropPlayerItemWithRandomChoice(stackInSlot, true);
            }
        }
    }

    public static ItemStack insertToSlots(List<SlotAccessor> slots, ItemStack stack, boolean emptyOnly) {
        for (SlotAccessor slot : slots) {
            stack = insert(slot, stack, emptyOnly);
            if (stack == null) return stack;
        }
        return stack;
    }

    public static ItemStack insert(SlotAccessor slot, ItemStack stack, boolean emptyOnly) {
        ItemStack stackInSlot = slot.callGetStack();
        if (emptyOnly) {
            if (stackInSlot != null || !slot.callIsItemValid(stack)) return stack;
            int amount = Math.min(stack.stackSize, slot.callGetSlotStackLimit());
            if (amount <= 0) return stack;
            ItemStack newStack = stack.copy();
            newStack.stackSize = (amount);
            stack.stackSize -= (amount);
            slot.callPutStack(newStack);
            return stack.stackSize == 0 ? null : stack;
        }
        if (stackInSlot != null && ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)
            && slot.callIsItemValid(stack)) {
            int amount = Math.min(
                slot.callGetSlotStackLimit(),
                Math.min(stack.stackSize, stack.getMaxStackSize() - stackInSlot.stackSize));
            if (amount <= 0) return stack;
            stack.stackSize -= (amount);
            stackInSlot.stackSize += (amount);
            slot.callPutStack(stackInSlot);
            return stack.stackSize == 0 ? null : stack;
        }
        return stack;
    }

    public static boolean SlotDummyOrCrafting(Slot slot) {
        if (Mods.CodeChickenCore.isLoaded() && slot instanceof SlotDummy) {
            return true;
        }
        if (Mods.Forestry.isLoaded() && slot instanceof SlotCraftMatrix) {
            return true;
        }
        if (Mods.Buildcraft.isLoaded() && slot instanceof SlotWorkbench) {
            return true;
        }
        if (Mods.Ae2.isLoaded() && slot instanceof AppEngCraftingSlot) {
            return true;
        }

        // Prevent items from being moved into the output slot, which leads to them vanishing
        if (slot instanceof SlotCrafting) {
            return true;
        }
        return false;
    }
}
