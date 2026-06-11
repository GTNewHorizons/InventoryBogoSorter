package com.cleanroommc.bogosorter.common.network.ae2;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.config.ae2.TooltipFeatureConfig;

public final class IntegrationDiagnostics {

    private static final Set<String> LOGGED_FAILURES = new HashSet<>();
    private static final long REPORT_INTERVAL_MS = 60000L;
    private static final AtomicLong CONTEXT_RESOLUTIONS = new AtomicLong();
    private static final AtomicLong CONTEXT_CACHE_HITS = new AtomicLong();
    private static final AtomicLong LOOKUP_CACHE_HITS = new AtomicLong();
    private static final AtomicLong THROTTLES = new AtomicLong();
    private static final AtomicLong ADAPTER_FAILURES = new AtomicLong();
    private static long nextReportTime;

    private IntegrationDiagnostics() {}

    public static void logCapabilityFailureOnce(String capability, Throwable throwable) {
        ADAPTER_FAILURES.incrementAndGet();
        if (TooltipFeatureConfig.isDebugLoggingEnabled() && LOGGED_FAILURES.add(capability)) {
            BogoSorter.LOGGER.warn("Disabling or degrading optional integration capability {}", capability, throwable);
        }
    }

    public static void recordContextResolution(boolean cacheHit) {
        CONTEXT_RESOLUTIONS.incrementAndGet();
        if (cacheHit) CONTEXT_CACHE_HITS.incrementAndGet();
        reportIfNeeded();
    }

    public static void recordLookupCacheHit() {
        LOOKUP_CACHE_HITS.incrementAndGet();
        reportIfNeeded();
    }

    public static void recordThrottle() {
        THROTTLES.incrementAndGet();
        reportIfNeeded();
    }

    private static void reportIfNeeded() {
        if (!TooltipFeatureConfig.isDebugLoggingEnabled()) return;
        long now = System.currentTimeMillis();
        if (now < nextReportTime) return;
        nextReportTime = now + REPORT_INTERVAL_MS;
        BogoSorter.LOGGER.info(
            "AE2 integration counters: contexts={}, contextCacheHits={}, lookupCacheHits={}, throttles={}, adapterFailures={}",
            CONTEXT_RESOLUTIONS.get(),
            CONTEXT_CACHE_HITS.get(),
            LOOKUP_CACHE_HITS.get(),
            THROTTLES.get(),
            ADAPTER_FAILURES.get());
    }
}
