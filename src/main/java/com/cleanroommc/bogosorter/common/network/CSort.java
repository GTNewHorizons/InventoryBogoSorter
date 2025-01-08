package com.cleanroommc.bogosorter.common.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class CSort implements IPacket {

    private Collection<ClientSortData> clientSortDataList;
    private List<SortRule<ItemStack>> sortRules;
    private List<NbtSortRule> nbtSortRules;
    private int hover;
    private boolean player;

    public CSort(Collection<ClientSortData> clientSortDataList, List<SortRule<ItemStack>> sortRules,
        List<NbtSortRule> nbtSortRules, int hover, boolean player) {
        this.clientSortDataList = clientSortDataList;
        this.sortRules = sortRules;
        this.nbtSortRules = nbtSortRules;
        this.hover = hover;
        this.player = player;
    }

    public CSort() {}

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(hover);
        buf.writeBoolean(player);
        buf.writeVarIntToBuffer(clientSortDataList.size());
        for (ClientSortData sortData : clientSortDataList) {
            sortData.writeToPacket(buf);
        }
        buf.writeVarIntToBuffer(sortRules.size());
        for (SortRule<ItemStack> sortRule : sortRules) {
            buf.writeVarIntToBuffer(sortRule.getSyncId());
            buf.writeBoolean(sortRule.isInverted());
        }
        buf.writeVarIntToBuffer(nbtSortRules.size());
        for (NbtSortRule sortRule : nbtSortRules) {
            buf.writeVarIntToBuffer(sortRule.getSyncId());
            buf.writeBoolean(sortRule.isInverted());
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        hover = buf.readVarIntFromBuffer();
        player = buf.readBoolean();
        clientSortDataList = new ArrayList<>();
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            clientSortDataList.add(ClientSortData.readFromPacket(buf));
        }
        sortRules = new ArrayList<>();
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            SortRule<ItemStack> sortRule = BogoSortAPI.INSTANCE.getItemSortRule(buf.readVarIntFromBuffer());
            sortRule.setInverted(buf.readBoolean());
            sortRules.add(sortRule);
        }
        nbtSortRules = new ArrayList<>();
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            NbtSortRule sortRule = BogoSortAPI.INSTANCE.getNbtSortRule(buf.readVarIntFromBuffer());
            sortRule.setInverted(buf.readBoolean());
            nbtSortRules.add(sortRule);
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        SortHandler.cacheItemSortRules.put(handler.playerEntity, sortRules);
        SortHandler.cacheNbtSortRules.put(handler.playerEntity, nbtSortRules);
        Int2ObjectOpenHashMap<ClientSortData> map = new Int2ObjectOpenHashMap<>();
        for (ClientSortData sortData : clientSortDataList) {
            for (int i : sortData.getSlotNumbers()) {
                map.put(i, sortData);
            }
        }
        SortHandler sortHandler = new SortHandler(handler.playerEntity, handler.playerEntity.openContainer, map);
        sortHandler.sort(hover);
        return null;
    }
}
