package com.cleanroommc.bogosorter.common.network.ae2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.bogosorter.common.config.ae2.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.common.network.IPacket;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;

public class CAe2AmountBatchRequest implements IPacket {

    private static final int MAX_BATCH_SIZE = 32;
    private static final int MAX_ASPECT_TAG_LENGTH = 64;
    private static final long EMPTY_AMOUNT = 0L;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FLUID = 1;
    private static final int TYPE_ESSENTIA = 2;

    private final List<Entry> entries = new ArrayList<>();

    @SuppressWarnings("unused")
    public CAe2AmountBatchRequest() {}

    public CAe2AmountBatchRequest(List<Entry> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("entries");
        }
        if (entries.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("AE2 request batch exceeds " + MAX_BATCH_SIZE + " entries");
        }
        for (Entry entry : entries) {
            entry.validate();
            this.entries.add(entry);
        }
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entries.size());
        for (Entry entry : this.entries) {
            entry.validate();
            buf.writeInt(entry.requestId);
            if (entry.fluidStack != null) {
                buf.writeByte(TYPE_FLUID);
                NetworkUtils.writeFluidStack(buf, entry.fluidStack);
            } else if (entry.essentiaAspectTag != null) {
                buf.writeByte(TYPE_ESSENTIA);
                buf.writeStringToBuffer(entry.essentiaAspectTag);
            } else {
                buf.writeByte(TYPE_ITEM);
                buf.writeItemStackToBuffer(entry.stack);
            }
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.entries.clear();
        int count = buf.readVarIntFromBuffer();
        if (count < 0 || count > MAX_BATCH_SIZE) {
            throw new IOException("Invalid AE2 request batch size " + count);
        }
        for (int i = 0; i < count; i++) {
            int requestId = buf.readInt();
            int type = buf.readUnsignedByte();
            if (type == TYPE_FLUID) {
                FluidStack fluidStack = NetworkUtils.readFluidStack(buf);
                if (fluidStack == null || fluidStack.getFluid() == null) {
                    throw new IOException("Missing fluid payload");
                }
                this.entries.add(new Entry(requestId, null, fluidStack, null));
            } else if (type == TYPE_ESSENTIA) {
                String aspectTag = buf.readStringFromBuffer(MAX_ASPECT_TAG_LENGTH);
                if (aspectTag.isEmpty()) {
                    throw new IOException("Missing essentia aspect");
                }
                this.entries.add(new Entry(requestId, null, null, aspectTag));
            } else if (type == TYPE_ITEM) {
                ItemStack stack = buf.readItemStackFromBuffer();
                if (stack == null || stack.getItem() == null) {
                    throw new IOException("Missing item payload");
                }
                this.entries.add(new Entry(requestId, stack, null, null));
            } else {
                throw new IOException("Unknown AE2 request type " + type);
            }
        }
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        if (handler == null || handler.playerEntity == null || this.entries.isEmpty()) {
            return null;
        }

        EntityPlayerMP player = handler.playerEntity;
        long now = System.currentTimeMillis();
        List<SAe2AmountBatchResponse.Entry> responses = new ArrayList<>(this.entries.size());
        if (!TooltipFeatureConfig.isTooltipEnabled()) {
            addResponses(responses, Ae2Status.NO_SYSTEM, 1500);
            return new SAe2AmountBatchResponse(Ae2Status.NO_SYSTEM, responses);
        }
        List<Ae2AmountService.BatchLookupEntry> batchEntries = new ArrayList<>(this.entries.size());
        for (Entry entry : this.entries) {
            batchEntries
                .add(new Ae2AmountService.BatchLookupEntry(entry.stack, entry.fluidStack, entry.essentiaAspectTag));
        }

        int distinctLookups = Ae2AmountService.countDistinctLookupKeys(batchEntries);
        if (distinctLookups == 0) {
            addResponses(responses, Ae2Status.ERROR, 5000);
            return new SAe2AmountBatchResponse(Ae2Status.ERROR, responses);
        }
        if (Ae2AmountService.arePlayerRequestsLimited(player, now, distinctLookups)) {
            addResponses(responses, Ae2Status.THROTTLED, 1000);
            return new SAe2AmountBatchResponse(Ae2Status.THROTTLED, responses);
        }

        Ae2AmountService.ContextResult contextResult = Ae2AmountService.resolvePlayerContext(player, now);
        if (!contextResult.isAvailable()) {
            addResponses(responses, contextResult.getStatus(), contextResult.getRetryAfterMs());
            return new SAe2AmountBatchResponse(contextResult.getStatus(), responses);
        }

        List<Ae2AmountService.AmountLookupResult> lookupResults = Ae2AmountService
            .lookupAmountBatch(contextResult.getContext(), batchEntries, now);
        for (int i = 0; i < this.entries.size(); i++) {
            Entry entry = this.entries.get(i);
            Ae2AmountService.AmountLookupResult result = lookupResults.get(i);
            responses.add(
                new SAe2AmountBatchResponse.Entry(
                    entry.requestId,
                    result.getStatus(),
                    result.getAmount(),
                    result.getRetryAfterMs()));
        }

        return new SAe2AmountBatchResponse(Ae2Status.OK, responses);
    }

    private void addResponses(List<SAe2AmountBatchResponse.Entry> responses, int status, int retryAfterMs) {
        for (Entry entry : this.entries) {
            responses.add(new SAe2AmountBatchResponse.Entry(entry.requestId, status, EMPTY_AMOUNT, retryAfterMs));
        }
    }

    public static final class Entry {

        private final int requestId;
        private final ItemStack stack;
        private final FluidStack fluidStack;
        private final String essentiaAspectTag;

        public Entry(int requestId, ItemStack stack, FluidStack fluidStack, String essentiaAspectTag) {
            this.requestId = requestId;
            this.stack = stack;
            this.fluidStack = fluidStack;
            this.essentiaAspectTag = essentiaAspectTag == null || essentiaAspectTag.isEmpty() ? null
                : essentiaAspectTag;
        }

        private void validate() {
            int payloads = (this.stack == null ? 0 : 1) + (this.fluidStack == null ? 0 : 1)
                + (this.essentiaAspectTag == null ? 0 : 1);
            if (payloads != 1) {
                throw new IllegalArgumentException("AE2 request entries require exactly one payload");
            }
            if (this.stack != null && this.stack.getItem() == null) {
                throw new IllegalArgumentException("Invalid AE2 item payload");
            }
            if (this.fluidStack != null && this.fluidStack.getFluid() == null) {
                throw new IllegalArgumentException("Invalid AE2 fluid payload");
            }
            if (this.essentiaAspectTag != null && this.essentiaAspectTag.length() > MAX_ASPECT_TAG_LENGTH) {
                throw new IllegalArgumentException("AE2 aspect tag is too long");
            }
        }
    }
}
