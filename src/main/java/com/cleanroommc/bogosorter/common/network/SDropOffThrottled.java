package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.cleanroommc.bogosorter.BogoSorter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SDropOffThrottled implements IPacket {

    @Override
    public void encode(PacketBuffer buf) throws IOException {

    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        String message = "[" + EnumChatFormatting.BLUE
            + BogoSorter.NAME
            + EnumChatFormatting.RESET
            + "]: "
            + EnumChatFormatting.RED
            + "Dropoff throttled";

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        return null;
    }
}
