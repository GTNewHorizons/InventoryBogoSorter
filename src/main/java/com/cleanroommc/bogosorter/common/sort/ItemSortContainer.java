package com.cleanroommc.bogosorter.common.sort;

import java.util.Objects;

import net.minecraft.item.ItemStack;

public class ItemSortContainer {

    private final ItemStack itemStack;
    private final ClientSortData sortData;

    public ItemSortContainer(ItemStack itemStack, ClientSortData sortData) {
        this.itemStack = itemStack.copy();
        this.sortData = sortData;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ClientSortData getSortData() {
        return sortData;
    }

    public int getColorHue() {
        return sortData.getColor();
    }

    public String getName() {
        return sortData.getName();
    }

    public void shrink(int amount) {
        this.itemStack.stackSize -= (amount);
    }

    public void grow(int amount) {
        this.itemStack.stackSize += (amount);
    }

    public int getAmount() {
        return this.itemStack.stackSize;
    }

    public boolean canMakeStack() {
        return getAmount() > 0;
    }

    public ItemStack makeStack(int max) {
        // Ensure max is not larger than the available stack size
        int splitAmount = Math.min(max, this.itemStack.stackSize);
        return this.itemStack.splitStack(splitAmount);
    }

    @Override
    public int hashCode() {
        ItemStack o = itemStack;
        return Objects.hash(o.getItem(), o.getItemDamage(), o.getTagCompound());
    }

    @Override
    public boolean equals(Object b1) {
        ItemStack a = itemStack;
        if (a == b1) return true;
        if (a == null || b1 == null) return false;
        ItemStack b;
        if (b1 instanceof ItemStack) {
            b = (ItemStack) b1;
        } else if (b1 instanceof ItemSortContainer) {
            b = ((ItemSortContainer) b1).itemStack;
        } else {
            return false;
        }
        return (a == null && b == null) || (a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
            && Objects.equals(a.getTagCompound(), b.getTagCompound()));
    }
}
