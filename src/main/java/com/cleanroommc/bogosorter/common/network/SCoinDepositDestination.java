package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.dropoff.CoinDepositDestination;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SCoinDepositDestination implements IPacket {

    private boolean useTeamWallet;

    public SCoinDepositDestination() {}

    public SCoinDepositDestination(boolean useTeamWallet) {
        this.useTeamWallet = useTeamWallet;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeBoolean(useTeamWallet);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        useTeamWallet = buf.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        BogoSorterConfig.dropOff.coinDepositDestination = useTeamWallet ? CoinDepositDestination.TEAM
            : CoinDepositDestination.PERSONAL;
        ConfigurationManager.save(BogoSorterConfig.class);
    }
}
