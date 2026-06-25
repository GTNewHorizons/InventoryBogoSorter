package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.compat.Mods;
import com.cleanroommc.bogosorter.compat.VendingMachineCompat;

public class CCoinDepositDestination implements IPacket {

    private boolean preferTeamWallet;

    public CCoinDepositDestination() {}

    public CCoinDepositDestination(boolean preferTeamWallet) {
        this.preferTeamWallet = preferTeamWallet;
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
        boolean useTeamWallet = preferTeamWallet && Mods.VendingMachine.isLoaded()
            && VendingMachineCompat.canUseTeamWallet(handler.playerEntity);
        return new SCoinDepositDestination(useTeamWallet);
    }
}
