package com.cleanroommc.bogosorter.common.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.Objects;

// The rows are indexed from 0 to 3, where 0 is the hotbar, 3 is the row above hotbar, 2 is the row above that, and 1 is the row above that.
//
// Vertically, that would be something like this:
// 1
// 2
// 3
// 0
//
// This behavior matches item slot ids in minecraft, where the hotbar is 0-8, and the rows continue counting from 9 at the top row of the inventory.
public class CHotbarRowSwap implements IPacket {
    private int rowIndex;

    public CHotbarRowSwap() {}

    public CHotbarRowSwap(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(rowIndex);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.rowIndex = buf.readVarIntFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (this.rowIndex <= 0 || this.rowIndex > 3) {
            return null;
        }

        ItemStack[] mainInventory = handler.playerEntity.inventory.mainInventory;

        for (int i = 0, itemIndex = i + rowIndex * 9;
             i < 9 && itemIndex < mainInventory.length;
             i++, itemIndex = i + rowIndex * 9) {
            ItemStack hotbarItem = mainInventory[i];
            ItemStack toSwapItem = mainInventory[itemIndex];
            // No null checks as we want to preserve spaces in the row swap.
            if (Objects.equals(hotbarItem, toSwapItem)) {
                continue;
            }
            mainInventory[i] = toSwapItem;
            mainInventory[itemIndex] = hotbarItem;
        }
        return null;
    }
}
