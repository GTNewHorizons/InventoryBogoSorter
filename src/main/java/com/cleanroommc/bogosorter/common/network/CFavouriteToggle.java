package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.common.favourite.FavouriteHelper;

/**
 * Client tells the server to toggle the favourite tag on a specific slot of the
 * sender's main inventory. The server reads the stack directly from
 * {@code player.inventory.mainInventory}, never from the open container, so a
 * spoofed packet can only mark items the sender already owns.
 */
public class CFavouriteToggle implements IPacket {

    private int slotIndex;

    public CFavouriteToggle() {}

    public CFavouriteToggle(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(slotIndex);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        slotIndex = buf.readVarIntFromBuffer();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        EntityPlayerMP player = handler.playerEntity;
        if (player == null) return null;

        ItemStack[] inv = player.inventory.mainInventory;
        if (slotIndex < 0 || slotIndex >= inv.length) return null;

        ItemStack stack = inv[slotIndex];
        if (stack == null) return null;

        FavouriteHelper.toggle(player, stack);
        // Sync the new NBT down to the client so the star appears immediately.
        player.inventoryContainer.detectAndSendChanges();
        return null;
    }
}
