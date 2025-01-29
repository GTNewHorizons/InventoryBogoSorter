package com.cleanroommc.bogosorter.common.sort;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.StringUtils;

import com.cleanroommc.bogosorter.common.sort.color.ItemColorHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class ClientSortData {

    @SideOnly(Side.CLIENT)
    public static ClientSortData of(ItemStack itemStack, boolean getColor, boolean getName) {
        int color = getColor ? ItemColorHelper.getItemColorHue(itemStack) : 0;
        String name = getName ? (itemStack != null ? itemStack.getDisplayName() : StringUtils.EMPTY)
            : StringUtils.EMPTY;
        return new ClientSortData(color, name);
    }

    public static ClientSortData readFromPacket(PacketBuffer buf) throws IOException {
        int color = buf.readVarIntFromBuffer();
        String name = buf.readStringFromBuffer(64);
        ClientSortData sortData = new ClientSortData(color, name);
        for (int i = 0, n = buf.readVarIntFromBuffer(); i < n; i++) {
            sortData.getSlotNumbers()
                .add(buf.readVarIntFromBuffer());
        }
        return sortData;
    }

    private final int color;
    private final String name;
    private final IntList slotNumbers = new IntArrayList();

    public ClientSortData(int color, String name) {
        this.color = color;
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public IntList getSlotNumbers() {
        return slotNumbers;
    }

    public String getName() {
        return name;
    }

    public void writeToPacket(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(color);
        buf.writeStringToBuffer(name);
        buf.writeVarIntToBuffer(slotNumbers.size());
        for (int i : slotNumbers) buf.writeVarIntToBuffer(i);
    }
}
