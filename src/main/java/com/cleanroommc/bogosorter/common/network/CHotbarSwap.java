package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CHotbarSwap implements IPacket {

    private int hotbarIndex;
    private int swapIndex;

    public CHotbarSwap() {}

    public CHotbarSwap(int hotbarIndex, int swapIndex) {
        this.hotbarIndex = hotbarIndex;
        this.swapIndex = swapIndex;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(hotbarIndex);
        buf.writeVarIntToBuffer(swapIndex);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.hotbarIndex = buf.readVarIntFromBuffer();
        this.swapIndex = buf.readVarIntFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        ItemStack[] mainInventory = handler.playerEntity.inventory.mainInventory;
        if (this.hotbarIndex < 0 || this.hotbarIndex >= mainInventory.length
            || this.swapIndex < 0
            || this.swapIndex >= mainInventory.length) {
            return null;
        }
        ItemStack hotbarItem = mainInventory[this.hotbarIndex];
        ItemStack toSwapItem = mainInventory[this.swapIndex];
        if (hotbarItem == null || toSwapItem == null || hotbarItem.equals(toSwapItem)) return null;
        mainInventory[this.hotbarIndex] = toSwapItem;
        mainInventory[this.swapIndex] = hotbarItem;
        return null;
    }
}
