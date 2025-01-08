package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

/**
 * Joinked from Multiblocked
 */
public interface IPacket extends IMessage {

    void encode(PacketBuffer buf) throws IOException;

    void decode(PacketBuffer buf) throws IOException;

    @Override
    default void fromBytes(ByteBuf buf) {
        try {
            decode(new PacketBuffer(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    default void toBytes(ByteBuf buf) {
        try {
            encode(new PacketBuffer(buf));
        } catch (IOException e) {
            System.out.println(e.getCause());
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    default IPacket executeClient(NetHandlerPlayClient handler) {
        return null;
    }

    default IPacket executeServer(NetHandlerPlayServer handler) {
        return null;
    }
}
