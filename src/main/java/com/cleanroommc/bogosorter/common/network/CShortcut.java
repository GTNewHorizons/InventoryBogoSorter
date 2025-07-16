package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.ShortcutHandler;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

public class CShortcut implements IPacket {

    private Type type;
    private int slotNumber;

    public CShortcut(Type type, int slotNumber) {
        this.type = type;
        this.slotNumber = slotNumber;
    }

    public CShortcut() {}

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        NetworkUtils.writeEnumValue(buf, type);
        buf.writeVarIntToBuffer(slotNumber);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        type = NetworkUtils.readEnumValue(buf, Type.class);
        slotNumber = buf.readVarIntFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.playerEntity.openContainer;
        if (container == null) throw new IllegalStateException("Expected open container on server");
        SlotAccessor slot = BogoSortAPI.getSlot(container, slotNumber);// container.getSlot(slotNumber);

        switch (type) {
            case MOVE_ALL:
                ShortcutHandler.moveAllItems(handler.playerEntity, container, slot, false);
                break;
            case MOVE_ALL_SAME:
                ShortcutHandler.moveAllItems(handler.playerEntity, container, slot, true);
                break;
            case MOVE_SINGLE:
                ShortcutHandler.moveSingleItem(handler.playerEntity, container, slot, false);
                break;
            case MOVE_SINGLE_EMPTY:
                ShortcutHandler.moveSingleItem(handler.playerEntity, container, slot, true);
                break;
            case DROP_ALL:
                ShortcutHandler.dropItems(handler.playerEntity, container, slot, false);
                break;
            case DROP_ALL_SAME:
                ShortcutHandler.dropItems(handler.playerEntity, container, slot, true);
                break;
        }
        container.detectAndSendChanges();
        return null;
    }

    public enum Type {
        MOVE_ALL,
        MOVE_ALL_SAME,
        MOVE_SINGLE,
        MOVE_SINGLE_EMPTY,
        DROP_ALL,
        DROP_ALL_SAME
    }
}
