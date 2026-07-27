package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.client.PinnedSlotClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SPlayerPins implements IPacket {

    private int windowId;
    private int mask;

    public SPlayerPins() {}

    public SPlayerPins(int windowId, int mask) {
        this.windowId = windowId;
        this.mask = mask;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(windowId);
        buf.writeInt(mask);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        windowId = buf.readInt();
        mask = buf.readInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(NetHandlerPlayClient handler) {
        PinnedSlotClient.sync(windowId, mask);
    }
}
