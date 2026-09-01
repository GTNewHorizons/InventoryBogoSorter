package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.client.PinnedSlotClient;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SPlayerPins implements IPacket {

    private static final int MAX_PIN_WORDS = 64;

    private int windowId;
    private int mask;
    private int[] backpackMask = new int[0];

    public SPlayerPins() {}

    public SPlayerPins(int windowId, int mask, int[] backpackMask) {
        this.windowId = windowId;
        this.mask = mask;
        this.backpackMask = backpackMask;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(windowId);
        buf.writeInt(mask);
        buf.writeVarIntToBuffer(backpackMask.length);
        for (int word : backpackMask) buf.writeInt(word);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        windowId = buf.readInt();
        mask = buf.readInt();
        int length = buf.readVarIntFromBuffer();
        if (length < 0 || length > MAX_PIN_WORDS) throw new IOException("Invalid backpack pin mask length: " + length);
        backpackMask = new int[length];
        for (int i = 0; i < length; i++) backpackMask[i] = buf.readInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(NetHandlerPlayClient handler) {
        PinnedSlotClient.sync(windowId, mask, backpackMask);
    }
}
