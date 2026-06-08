package com.cleanroommc.bogosorter.common.network.ae2;

import java.io.IOException;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.client.ae2.Ae2ClientBridge;
import com.cleanroommc.bogosorter.common.network.IPacket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class STooltipFeatureState implements IPacket {

    private boolean amountTooltipsAllowed;
    private boolean thaumicAllowed;

    @SuppressWarnings("unused")
    public STooltipFeatureState() {}

    public STooltipFeatureState(boolean amountTooltipsAllowed, boolean thaumicAllowed) {
        this.amountTooltipsAllowed = amountTooltipsAllowed;
        this.thaumicAllowed = thaumicAllowed;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeBoolean(this.amountTooltipsAllowed);
        buf.writeBoolean(this.thaumicAllowed);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.amountTooltipsAllowed = buf.readBoolean();
        this.thaumicAllowed = buf.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        Ae2ClientBridge.setServerFeatures(this.amountTooltipsAllowed, this.thaumicAllowed);
    }
}
