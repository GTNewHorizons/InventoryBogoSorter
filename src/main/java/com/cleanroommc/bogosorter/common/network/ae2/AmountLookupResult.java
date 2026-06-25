package com.cleanroommc.bogosorter.common.network.ae2;

public final class AmountLookupResult {

    static final int THROTTLE_RETRY_MS = 1000;

    private final int status;
    private final long amount;
    private final int retryAfterMs;

    AmountLookupResult(int status, long amount, int retryAfterMs) {
        this.status = status;
        this.amount = amount;
        this.retryAfterMs = retryAfterMs;
    }

    public static AmountLookupResult ok(long amount) {
        return new AmountLookupResult(Ae2Status.OK, amount, 0);
    }

    public static AmountLookupResult noSystem() {
        return new AmountLookupResult(Ae2Status.NO_SYSTEM, 0L, ContextResult.CONTEXT_RETRY_MS);
    }

    public static AmountLookupResult throttled() {
        return new AmountLookupResult(Ae2Status.THROTTLED, 0L, THROTTLE_RETRY_MS);
    }

    public static AmountLookupResult unsupported() {
        return new AmountLookupResult(Ae2Status.UNSUPPORTED, 0L, 5000);
    }

    public static AmountLookupResult error() {
        return new AmountLookupResult(Ae2Status.ERROR, 0L, 5000);
    }

    public int getStatus() {
        return this.status;
    }

    public long getAmount() {
        return this.amount;
    }

    public int getRetryAfterMs() {
        return this.retryAfterMs;
    }
}
