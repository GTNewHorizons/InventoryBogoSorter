package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;

public class CRefill implements IPacket {

    private ItemStack stack;
    private int index;
    private boolean swap;
    private boolean allowPinnedSlots;

    public CRefill(ItemStack _stack, int _index, boolean _swap, boolean _allowPinnedSlots) {
        this.stack = _stack;
        this.index = _index;
        this.swap = _swap;
        this.allowPinnedSlots = _allowPinnedSlots;
    }

    public CRefill() {}

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeItemStackToBuffer(stack);
        buf.writeInt(index);
        buf.writeBoolean(swap);
        buf.writeBoolean(allowPinnedSlots);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.stack = buf.readItemStackFromBuffer();
        this.index = buf.readInt();
        this.swap = buf.readBoolean();
        this.allowPinnedSlots = buf.readBoolean();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (BogoSorterConfig.enableAutoRefill_server && stack != null && this.index >= 0 && this.index < 9) {
            new RefillHandler(
                this.index,
                this.stack,
                handler.playerEntity,
                this.swap,
                this.allowPinnedSlots && BogoSorterConfig.autoRefillFromPinnedSlots_server).handleRefill();
        }
        return null;
    }
}
