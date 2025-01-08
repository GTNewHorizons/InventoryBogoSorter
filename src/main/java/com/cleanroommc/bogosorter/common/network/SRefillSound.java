package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SRefillSound implements IPacket {

    @Override
    public void encode(PacketBuffer buf) throws IOException {}

    @Override
    public void decode(PacketBuffer buf) throws IOException {}

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.func_147673_a(new ResourceLocation("mob.chicken.plop")));
        return null;
    }
}
