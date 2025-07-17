package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.tuple.Pair;

import com.cleanroommc.bogosorter.BogoSortAPI;

public class CSlotSync implements IPacket {

    private final List<Pair<ItemStack, Integer>> content;

    public CSlotSync() {
        content = new ArrayList<>();
    }

    public CSlotSync(List<Pair<ItemStack, Integer>> content) {
        this.content = content;
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(content.size());
        for (Pair<ItemStack, Integer> pair : content) {
            buf.writeItemStackToBuffer(pair.getKey());
            buf.writeNBTTagCompoundToBuffer(
                pair.getKey() == null ? null
                    : pair.getKey()
                        .getTagCompound());
            buf.writeVarIntToBuffer(pair.getValue());
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            ItemStack stack = buf.readItemStackFromBuffer();
            NBTTagCompound nbt = buf.readNBTTagCompoundFromBuffer();
            if (stack != null && nbt != null) {
                stack.setTagCompound(nbt);
            }
            content.add(Pair.of(stack, buf.readVarIntFromBuffer()));
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        for (Pair<ItemStack, Integer> pair : content) {
            BogoSortAPI.getSlot(handler.playerEntity.openContainer, pair.getValue())
                .callPutStack(pair.getKey());
        }
        handler.playerEntity.openContainer.detectAndSendChanges();
        return null;
    }
}
