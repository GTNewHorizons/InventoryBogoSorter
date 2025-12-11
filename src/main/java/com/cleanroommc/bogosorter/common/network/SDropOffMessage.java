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
    private int quotaReachedCount;

    public SDropOffMessage() {}

    public SDropOffMessage(int itemsCounter, int affectedContainers, int totalContainers,
        List<RendererCubeTarget> rendererCubeTargets, int quotaReachedCount) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
        this.quotaReachedCount = quotaReachedCount;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeInt(itemsCounter);
        buf.writeInt(affectedContainers);
        buf.writeInt(totalContainers);
        buf.writeInt(quotaReachedCount);

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
        quotaReachedCount = buf.readInt();

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

        if (BogoSorterConfig.dropOff.dropoffChatMessage) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("[")
                .append(EnumChatFormatting.BLUE)
                .append(BogoSorter.NAME)
                .append(EnumChatFormatting.RESET)
                .append("]: ")
                .append(EnumChatFormatting.RED)
                .append(itemsCounter)
                .append(EnumChatFormatting.RESET)
                .append(" items moved to ")
                .append(EnumChatFormatting.RED)
                .append(affectedContainers)
                .append(EnumChatFormatting.RESET)
                .append(" containers of ")
                .append(EnumChatFormatting.RED)
                .append(totalContainers)
                .append(EnumChatFormatting.RESET)
                .append(" checked in total.");

            // Append the quota warning if it happened at least once
            if (quotaReachedCount > 0) {
                messageBuilder.append(EnumChatFormatting.RED)
                    .append(" (Quota reached ")
                    .append(quotaReachedCount)
                    .append(" times)");
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(messageBuilder.toString()));
        }

        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.func_147673_a(new ResourceLocation("gui.button.press")));

        return null;
    }
}
