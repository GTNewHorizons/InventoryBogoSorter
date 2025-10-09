package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.common.refill.RefillHandler;

public class CRefill implements IPacket {

    private ItemStack stack;
    private int index;
    private boolean swap;

    public CRefill(ItemStack _stack, int _index, boolean _swap) {
        this.stack = _stack;
        this.index = _index;
        this.swap = _swap;
    }

    public CRefill() {}

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeItemStackToBuffer(stack);
        buf.writeInt(index);
        buf.writeBoolean(swap);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.stack = buf.readItemStackFromBuffer();
        this.index = buf.readInt();
        this.swap = buf.readBoolean();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (stack != null && this.index < 9) {
            new RefillHandler(this.index, this.stack, handler.playerEntity, this.swap).handleRefill();
        }
        return null;
    }
}
