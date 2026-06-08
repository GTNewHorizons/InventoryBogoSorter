package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.BogoSorter;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

/**
 * Joinked from Multiblocked
 */
public interface IPacket extends IMessage {

    Set<IPacket> REJECTED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    void encode(PacketBuffer buf) throws IOException;

    void decode(PacketBuffer buf) throws IOException;

    default boolean isRejected() {
        return REJECTED.contains(this);
    }

    default void acknowledge() {
        REJECTED.remove(this);
    }

    @Override
    default void fromBytes(ByteBuf buf) {
        try {
            decode(new PacketBuffer(buf));
        } catch (IOException e) {
            // bad packets get dropped instead of kicking the player
            BogoSorter.LOGGER.warn("Rejected malformed {} packet", getClass().getName(), e);
            REJECTED.add(this);
        }
    }

    @Override
    default void toBytes(ByteBuf buf) {
        try {
            encode(new PacketBuffer(buf));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode packet " + getClass().getName(), e);
        }
    }

    @SideOnly(Side.CLIENT)
    default void executeClient(NetHandlerPlayClient handler) {}

    default IPacket executeServer(NetHandlerPlayServer handler) {
        return null;
    }
}
