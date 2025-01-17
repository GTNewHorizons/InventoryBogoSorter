package com.cleanroommc.bogosorter.common.config;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.common.network.CConfigSync;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class PlayerConfig {

    private static final Map<EntityPlayerMP, PlayerConfig> playerConfig = new Object2ObjectOpenHashMap<>();
    private static final PlayerConfig CLIENT = new PlayerConfig();
    public boolean enableHotbarSort = true;
    public boolean enableAutoRefill = true;
    public int autoRefillDamageThreshold = 1;

    public static PlayerConfig get(@NotNull EntityPlayer player) {
        if (NetworkUtils.isDedicatedClient()) {
            return CLIENT;
        }
        if (player instanceof EntityPlayerMP) {
            return playerConfig.computeIfAbsent((EntityPlayerMP) player, key -> new PlayerConfig());
        }
        throw new IllegalStateException("Could net get player config for " + player.getDisplayName());
    }

    public static void checkPlayers() {
        playerConfig.keySet()
            .removeIf(player -> player.worldObj == null);
    }

    @SideOnly(Side.CLIENT)
    public static PlayerConfig getClient() {
        return CLIENT;
    }

    public void writePacket(PacketBuffer buffer) {
        buffer.writeBoolean(enableHotbarSort);
        buffer.writeBoolean(enableAutoRefill);
        buffer.writeVarIntToBuffer(autoRefillDamageThreshold);
    }

    public void readPacket(PacketBuffer buffer) {
        enableHotbarSort = buffer.readBoolean();
        enableAutoRefill = buffer.readBoolean();
        autoRefillDamageThreshold = buffer.readVarIntFromBuffer();
    }

    @SideOnly(Side.CLIENT)
    public static void syncToServer() {
        NetworkHandler.sendToServer(new CConfigSync());
    }
}
