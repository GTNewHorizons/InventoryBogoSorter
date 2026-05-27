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

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class CSort implements IPacket {

    private Collection<ClientSortData> clientSortDataList;
    private List<SortRule<ItemStack>> sortRules;
    private List<NbtSortRule> nbtSortRules;
    // Inversion flags parallel to the rule lists. Applied in executeServer rather than decode so the
    // shared SortRule singletons are only mutated on the main thread (see executeServer).
    private BooleanList sortRuleInverted;
    private BooleanList nbtRuleInverted;
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
        sortRuleInverted = new BooleanArrayList();
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            int syncId = buf.readVarIntFromBuffer();
            boolean inverted = buf.readBoolean();
            SortRule<ItemStack> sortRule = BogoSortAPI.INSTANCE.getItemSortRule(syncId);
            if (sortRule == null) continue;
            sortRules.add(sortRule);
            sortRuleInverted.add(inverted);
        }
        nbtSortRules = new ArrayList<>();
        nbtRuleInverted = new BooleanArrayList();
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            int syncId = buf.readVarIntFromBuffer();
            boolean inverted = buf.readBoolean();
            NbtSortRule sortRule = BogoSortAPI.INSTANCE.getNbtSortRule(syncId);
            if (sortRule == null) continue;
            nbtSortRules.add(sortRule);
            nbtRuleInverted.add(inverted);
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        // Apply inversion on the main thread (executeServer is scheduled there) right before sorting.
        // The SortRule objects are server-wide singletons, so setting this in decode on the netty thread
        // could be overwritten by another player's concurrent sort before this one runs.
        for (int i = 0; i < sortRules.size(); i++) {
            sortRules.get(i)
                .setInverted(sortRuleInverted.getBoolean(i));
        }
        for (int i = 0; i < nbtSortRules.size(); i++) {
            nbtSortRules.get(i)
                .setInverted(nbtRuleInverted.getBoolean(i));
        }
        Int2ObjectOpenHashMap<ClientSortData> map = new Int2ObjectOpenHashMap<>();
        for (ClientSortData sortData : clientSortDataList) {
            for (int i : sortData.getSlotNumbers()) {
                map.put(i, sortData);
            }
        }
        new SortHandler(handler.playerEntity, handler.playerEntity.openContainer, sortRules, nbtSortRules, map)
            .sort(hover);
        return null;
    }
}
