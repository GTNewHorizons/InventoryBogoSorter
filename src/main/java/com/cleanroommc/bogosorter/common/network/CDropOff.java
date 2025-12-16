package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.dropoff.DropOffScheduler;

public class CDropOff implements IPacket {

    static final HashMap<UUID, Long> playerThrottles = new HashMap<>();

    public CDropOff() {}

    @Override
    public void encode(PacketBuffer buf) throws IOException {}

    @Override
    public void decode(PacketBuffer buf) throws IOException {}

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.playerEntity;
        UUID playerID = player.getPersistentID();

        // Packet Throttling
        if (MinecraftServer.getServer()
            .isDedicatedServer()) {
            long lastPlayerTime = playerThrottles.computeIfAbsent(playerID, p -> 0L);
            final long throttleTime = System.nanoTime();
            long requiredDelayNanos = TimeUnit.MILLISECONDS.toNanos(BogoSorterConfig.dropOff.dropoffPacketThrottleInMS);

            if ((throttleTime - lastPlayerTime) < requiredDelayNanos) {
                return new SDropOffThrottled();
            }
            playerThrottles.replace(playerID, throttleTime);
        }
        DropOffScheduler scheduler = DropOffScheduler.INSTANCE;

        if (scheduler.isPlayerTaskActive(player)) {
            return new SDropOffThrottled();
        }

        scheduler.startTask(player);

        return null;
    }
}
