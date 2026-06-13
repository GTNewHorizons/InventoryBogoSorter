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
import com.cleanroommc.bogosorter.common.dropoff.CoinDepositDestination;
import com.cleanroommc.bogosorter.common.dropoff.DropOffScheduler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CDropOff implements IPacket {

    static final HashMap<UUID, Long> playerThrottles = new HashMap<>();
    private boolean preferTeamWallet;

    public CDropOff() {}

    public CDropOff(boolean preferTeamWallet) {
        this.preferTeamWallet = preferTeamWallet;
    }

    @SideOnly(Side.CLIENT)
    public static CDropOff fromClientPreference() {
        return new CDropOff(BogoSorterConfig.dropOff.coinDepositDestination == CoinDepositDestination.TEAM);
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeBoolean(preferTeamWallet);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        preferTeamWallet = buf.readBoolean();
    }

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

        scheduler.startTask(player, preferTeamWallet);

        return null;
    }
}
