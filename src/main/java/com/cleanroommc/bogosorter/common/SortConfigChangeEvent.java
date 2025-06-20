package com.cleanroommc.bogosorter.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Unmodifiable;

import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.config.SortRulesConfig;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.bogosorter.common.sort.SortHandler;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SortConfigChangeEvent extends Event {

    @Unmodifiable
    public final List<SortRule<ItemStack>> configuredItemSortRules;
    @Unmodifiable
    public final List<NbtSortRule> configuredNbtSortRules;

    public SortConfigChangeEvent() {
        this.configuredItemSortRules = Collections.unmodifiableList(SortRulesConfig.sortRules);
        this.configuredNbtSortRules = Collections.unmodifiableList(SortRulesConfig.nbtSortRules);
    }

    @SideOnly(Side.CLIENT)
    public Comparator<ItemStack> getItemComparator() {
        return SortHandler.getClientItemComparator();
    }
}
