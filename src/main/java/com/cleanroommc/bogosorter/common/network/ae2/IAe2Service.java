package com.cleanroommc.bogosorter.common.network.ae2;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IAe2Service {

    ContextResult resolveContext(EntityPlayerMP player, long now);

    AmountLookupResult lookupAmount(PlayerAeContext context, ItemStack stack, FluidStack fluidStack,
        String essentiaAspectTag, long now);

    List<AmountLookupResult> lookupAmountBatch(PlayerAeContext context, List<BatchLookupEntry> entries, long now);

    int countDistinctLookupKeys(List<BatchLookupEntry> entries);

    void clearCaches();
}
