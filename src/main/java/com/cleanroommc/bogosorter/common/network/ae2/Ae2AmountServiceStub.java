package com.cleanroommc.bogosorter.common.network.ae2;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

final class Ae2AmountServiceStub implements IAe2Service {

    @Override
    public ContextResult resolveContext(EntityPlayerMP player, long now) {
        return ContextResult.noSystem();
    }

    @Override
    public AmountLookupResult lookupAmount(PlayerAeContext context, ItemStack stack, FluidStack fluidStack,
        String essentiaAspectTag, long now) {
        return AmountLookupResult.noSystem();
    }

    @Override
    public List<AmountLookupResult> lookupAmountBatch(PlayerAeContext context, List<BatchLookupEntry> entries,
        long now) {
        return Collections.emptyList();
    }

    @Override
    public int countDistinctLookupKeys(List<BatchLookupEntry> entries) {
        return 0;
    }

    @Override
    public void clearCaches() {}
}
