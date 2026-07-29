package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.common.PinnedSlots;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;

public class CPlayerPins implements IPacket {

    private static final long TOGGLE_THROTTLE_NS = TimeUnit.MILLISECONDS.toNanos(50);
    private static final Map<EntityPlayerMP, Long> LAST_BACKPACK_TOGGLE = new WeakHashMap<>();

    private Operation operation;
    private int windowId;
    private int slotNumber;

    public CPlayerPins() {}

    private CPlayerPins(Operation operation, int windowId, int slotNumber) {
        this.operation = operation;
        this.windowId = windowId;
        this.slotNumber = slotNumber;
    }

    public static CPlayerPins get(int windowId) {
        return new CPlayerPins(Operation.GET, windowId, 0);
    }

    public static CPlayerPins toggle(int windowId, int slotNumber) {
        return new CPlayerPins(Operation.TOGGLE, windowId, slotNumber);
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        NetworkUtils.writeEnumValue(buf, operation);
        buf.writeInt(windowId);
        buf.writeVarIntToBuffer(slotNumber);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        operation = NetworkUtils.readEnumValue(buf, Operation.class);
        windowId = buf.readInt();
        slotNumber = buf.readVarIntFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (operation == null) return null;
        EntityPlayerMP player = handler.playerEntity;
        Container container = player.openContainer;
        if (container == null || container.windowId != windowId) return null;

        int[] backpackMask = null;
        if (operation == Operation.TOGGLE) {
            if (slotNumber >= 0 && slotNumber < container.inventorySlots.size()) {
                SlotAccessor slot = BogoSortAPI.getSlot(container, slotNumber);
                if (PinnedSlots.isPinnable(slot)) {
                    PinnedSlots.toggle(player, slot.callGetSlotIndex());
                    // Personal Minecraft Backpacks persist synchronously, so rate-limit all backpack pin toggles.
                } else if (!isBackpackToggleThrottled(player)) {
                    backpackMask = PinnedSlots.toggleBackpack(player, container, slot);
                    if (backpackMask.length != 0) container.detectAndSendChanges();
                }
            }
        }
        if (backpackMask == null || backpackMask.length == 0) {
            backpackMask = PinnedSlots.getBackpackMask(player, container);
        }
        return new SPlayerPins(windowId, PinnedSlots.getMask(player), backpackMask);
    }

    private static boolean isBackpackToggleThrottled(EntityPlayerMP player) {
        long now = System.nanoTime();
        Long previous = LAST_BACKPACK_TOGGLE.get(player);
        if (previous != null && now - previous < TOGGLE_THROTTLE_NS) return true;
        LAST_BACKPACK_TOGGLE.put(player, now);
        return false;
    }

    private enum Operation {
        GET,
        TOGGLE
    }
}
