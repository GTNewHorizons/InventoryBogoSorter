package com.cleanroommc.bogosorter.common.network;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCubeTarget;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SDropOffMessage implements IPacket {

    private int itemsCounter;
    private int affectedContainers;
    private int totalContainers;
    private List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
    private boolean timeQuotaReached;

    public SDropOffMessage() {}

    public SDropOffMessage(int itemsCounter, int affectedContainers, int totalContainers,
        List<RendererCubeTarget> rendererCubeTargets, boolean timeQuotaReached) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
        this.timeQuotaReached = timeQuotaReached;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(itemsCounter);
        buf.writeInt(affectedContainers);
        buf.writeInt(totalContainers);
        buf.writeBoolean(timeQuotaReached);

        buf.writeInt(rendererCubeTargets.size());
        for (RendererCubeTarget target : rendererCubeTargets) {
            buf.writeInt(
                target.getBlockPos()
                    .getX());
            buf.writeInt(
                target.getBlockPos()
                    .getY());
            buf.writeInt(
                target.getBlockPos()
                    .getZ());

            buf.writeInt(
                target.getColor()
                    .getRed());
            buf.writeInt(
                target.getColor()
                    .getGreen());
            buf.writeInt(
                target.getColor()
                    .getBlue());
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        itemsCounter = buf.readInt();
        affectedContainers = buf.readInt();
        totalContainers = buf.readInt();
        timeQuotaReached = buf.readBoolean();

        int targetsLen = buf.readInt();
        for (int i = 0; i < targetsLen; i++) {
            rendererCubeTargets.add(
                new RendererCubeTarget(
                    new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()),
                    new Color(buf.readInt(), buf.readInt(), buf.readInt())));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
        if (BogoSorterConfig.dropOff.dropoffRender) {
            RendererCube.INSTANCE.draw(rendererCubeTargets);
        }

        if (timeQuotaReached) {
            String message = "[" + EnumChatFormatting.BLUE
                + BogoSorter.NAME
                + EnumChatFormatting.RESET
                + "]: "
                + EnumChatFormatting.RED
                + "Quota Time Limit Reached; Stopped Early";

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        }

        if (BogoSorterConfig.dropOff.dropoffChatMessage) {
            String message = "[" + EnumChatFormatting.BLUE
                + BogoSorter.NAME
                + EnumChatFormatting.RESET
                + "]: "
                + EnumChatFormatting.RED
                + itemsCounter
                + EnumChatFormatting.RESET
                + " items moved to "
                + EnumChatFormatting.RED
                + affectedContainers
                + EnumChatFormatting.RESET
                + " containers of "
                + EnumChatFormatting.RED
                + totalContainers
                + EnumChatFormatting.RESET
                + " checked in total.";

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        }

        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.func_147673_a(new ResourceLocation("gui.button.press")));

        return null;
    }
}
