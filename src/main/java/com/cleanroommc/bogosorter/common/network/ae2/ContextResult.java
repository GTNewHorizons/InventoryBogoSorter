package com.cleanroommc.bogosorter.common.network.ae2;

public final class ContextResult {

    static final int CONTEXT_RETRY_MS = 1500;

    private final PlayerAeContext context;
    private final int status;
    private final int retryAfterMs;

    ContextResult(PlayerAeContext context, int status, int retryAfterMs) {
        this.context = context;
        this.status = status;
        this.retryAfterMs = retryAfterMs;
    }

    public static ContextResult ok(PlayerAeContext context) {
        return new ContextResult(context, Ae2Status.OK, 0);
    }

    public static ContextResult noSystem() {
        return new ContextResult(null, Ae2Status.NO_SYSTEM, CONTEXT_RETRY_MS);
    }

    public static ContextResult outOfRange() {
        return new ContextResult(null, Ae2Status.OUT_OF_RANGE, CONTEXT_RETRY_MS);
    }

    public boolean isAvailable() {
        return this.context != null && this.status == Ae2Status.OK;
    }

    public PlayerAeContext getContext() {
        return this.context;
    }

    public int getStatus() {
        return this.status;
    }

    public int getRetryAfterMs() {
        return this.retryAfterMs;
    }
}
