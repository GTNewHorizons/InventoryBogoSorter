package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.SortHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Debug packets; requires config + op permisison
 */
public class CSlotSync implements IPacket {

    private Operation operation;
    private int slotNumber;

    public CSlotSync() {}

    public CSlotSync(Operation operation, int slotNumber) {
        this.operation = operation;
        this.slotNumber = slotNumber;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        NetworkUtils.writeEnumValue(buf, operation);
        buf.writeVarIntToBuffer(slotNumber);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        operation = NetworkUtils.readEnumValue(buf, Operation.class);
        slotNumber = buf.readVarIntFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (operation == null) return null;
        if (!BogoSorterConfig.enableDebugTools) return null;
        EntityPlayerMP player = handler.playerEntity;
        if (!MinecraftServer.getServer()
            .getConfigurationManager()
            .func_152596_g(player.getGameProfile())) {
            return null;
        }
        Container container = player.openContainer;
        if (container == null) return null;
        if (slotNumber < 0 || slotNumber >= container.inventorySlots.size()) return null;

        SortHandler sortHandler = new SortHandler(
            player,
            container,
            new ArrayList<>(),
            new ArrayList<>(),
            new Int2ObjectOpenHashMap<ClientSortData>());

        switch (operation) {
            case CLEAR:
                sortHandler.clearGroup(slotNumber);
                break;
            case RANDOMIZE:
                sortHandler.randomizeGroup(slotNumber);
                break;
        }
        return null;
    }

    public enum Operation {
        CLEAR,
        RANDOMIZE
    }
}
