package com.cleanroommc.bogosorter.common.network.ae2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.bogosorter.client.ae2.Ae2ClientBridge;
import com.cleanroommc.bogosorter.common.network.IPacket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SAe2AmountBatchResponse implements IPacket {

    private static final int MAX_BATCH_SIZE = 32;
    private static final int MAX_RETRY_AFTER_MS = 60000;

    private int contextStatus = Ae2Status.NO_SYSTEM;
    private final List<Entry> entries = new ArrayList<>();

    @SuppressWarnings("unused")
    public SAe2AmountBatchResponse() {}

    public SAe2AmountBatchResponse(int contextStatus, List<Entry> entries) {
        if (!Ae2Status.isValid(contextStatus)) {
            throw new IllegalArgumentException("Invalid AE2 context status " + contextStatus);
        }
        if (entries == null || entries.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Invalid AE2 response batch size");
        }
        this.contextStatus = contextStatus;
        for (Entry entry : entries) {
            entry.validate();
            this.entries.add(entry);
        }
    }

    @Override
    public void encode(PacketBuffer buf) throws IOException {
        buf.writeByte(this.contextStatus);
        buf.writeVarIntToBuffer(this.entries.size());
        for (Entry entry : this.entries) {
            entry.validate();
            buf.writeInt(entry.requestId);
            buf.writeByte(entry.status);
            buf.writeLong(entry.amount);
            buf.writeVarIntToBuffer(entry.retryAfterMs);
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        this.entries.clear();
        this.contextStatus = buf.readUnsignedByte();
        if (!Ae2Status.isValid(this.contextStatus)) {
            throw new IOException("Invalid AE2 context status " + this.contextStatus);
        }
        int count = buf.readVarIntFromBuffer();
        if (count < 0 || count > MAX_BATCH_SIZE) {
            throw new IOException("Invalid AE2 response batch size " + count);
        }
        for (int i = 0; i < count; i++) {
            int requestId = buf.readInt();
            int status = buf.readUnsignedByte();
            long amount = buf.readLong();
            int retryAfterMs = buf.readVarIntFromBuffer();
            if (!Ae2Status.isValid(status)) {
                throw new IOException("Invalid AE2 response status " + status);
            }
            if (retryAfterMs < 0 || retryAfterMs > MAX_RETRY_AFTER_MS) {
                throw new IOException("Invalid AE2 retry delay " + retryAfterMs);
            }
            this.entries.add(new Entry(requestId, status, amount, retryAfterMs));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        List<Ae2ClientBridge.Response> responses = new ArrayList<>(this.entries.size());
        for (Entry entry : entries) {
            responses
                .add(new Ae2ClientBridge.Response(entry.requestId, entry.status, entry.amount, entry.retryAfterMs));
        }
        Ae2ClientBridge.handleBatchResponse(this.contextStatus, responses);
    }

    public static final class Entry {

        private final int requestId;
        private final int status;
        private final long amount;
        private final int retryAfterMs;

        public Entry(int requestId, int status, long amount, int retryAfterMs) {
            this.requestId = requestId;
            this.status = status;
            this.amount = amount;
            this.retryAfterMs = retryAfterMs;
            validate();
        }

        private void validate() {
            if (!Ae2Status.isValid(this.status)) {
                throw new IllegalArgumentException("Invalid AE2 response status " + this.status);
            }
            if (this.retryAfterMs < 0 || this.retryAfterMs > MAX_RETRY_AFTER_MS) {
                throw new IllegalArgumentException("Invalid AE2 retry delay " + this.retryAfterMs);
            }
        }
    }
}
