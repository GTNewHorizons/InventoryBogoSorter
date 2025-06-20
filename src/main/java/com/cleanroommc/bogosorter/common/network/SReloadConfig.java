package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;

import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.config.Serializer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SReloadConfig implements IPacket {

    @Override
    public void encode(PacketBuffer buf) throws IOException {}

    @Override
    public void decode(PacketBuffer buf) throws IOException {}

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Serializer.loadConfig();
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
        return null;
    }
}
