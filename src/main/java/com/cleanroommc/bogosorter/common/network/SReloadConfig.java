package com.cleanroommc.bogosorter.common.network;

import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.config.Serializer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;

public class SReloadConfig implements IPacket {

    @Override
    public void encode(PacketBuffer buf) throws IOException {
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Serializer.loadConfig();
        PlayerConfig.syncToServer();
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
        return null;
    }
}
