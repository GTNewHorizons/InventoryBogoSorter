package com.cleanroommc.bogosorter.common.sort;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import org.apache.commons.lang3.StringUtils;

import com.cleanroommc.bogosorter.common.sort.color.ItemColorHelper;
import com.gtnewhorizon.gtnhlib.util.font.FontRendering;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class ClientSortData {

    @SideOnly(Side.CLIENT)
    public static ClientSortData of(ItemStack itemStack, boolean getColor, boolean getName) {
        int color = getColor ? ItemColorHelper.getItemColorHue(itemStack) : 0;
        String name = getName ? (itemStack != null ? stripFormatting(itemStack.getDisplayName()) : StringUtils.EMPTY)
            : StringUtils.EMPTY;
        return new ClientSortData(color, name);
    }

    /** Display name without format codes, so &-styled or colored names sort by their visible text. */
    @SideOnly(Side.CLIENT)
    private static String stripFormatting(String name) {
        name = FontRendering.preprocessText(name);
        if (name == null || name.indexOf('\u00a7') == -1) return name;

        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '\u00a7' && i + 1 < name.length()) {
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static ClientSortData readFromPacket(PacketBuffer buf) throws IOException {
        int color = buf.readVarIntFromBuffer();
        String name = buf.readStringFromBuffer(32767);
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
