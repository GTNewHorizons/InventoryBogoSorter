package com.cleanroommc.bogosorter.common.network.ae2;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record BatchLookupEntry(ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {

}
