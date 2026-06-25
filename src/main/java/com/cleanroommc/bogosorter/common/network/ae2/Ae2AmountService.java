package com.cleanroommc.bogosorter.common.network.ae2;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class Ae2AmountService {

    private static final int PLAYER_REQUESTS_PER_SECOND = 10;
    private static final int PLAYER_REQUEST_BURST = 40;
    private static final int MAX_PLAYER_LIMITS = 1024;
    private static final long CACHE_RETAIN_MS = 30000L;
    private static final long CACHE_CLEANUP_INTERVAL_MS = 30000L;

    private static final Map<String, RateLimit> PLAYER_LIMITS = new ConcurrentHashMap<>();
    private static volatile long nextCleanupTime;

    private static final IAe2Service IMPL;

    static {
        IAe2Service impl;
        try {
            Class<?> clazz = Class.forName(
                "com.cleanroommc.bogosorter.common.network.ae2.impl.Ae2AmountServiceImpl",
                true,
                Ae2AmountService.class.getClassLoader());
            java.lang.reflect.Method m = clazz.getMethod("instance");
            impl = (IAe2Service) m.invoke(null);
        } catch (Throwable t) {
            impl = new Ae2AmountServiceStub();
        }
        IMPL = impl;
    }

    private Ae2AmountService() {}

    static boolean arePlayerRequestsLimited(EntityPlayerMP player, long now, int count) {
        if (player == null) return true;
        cleanupCaches(now);
        String key = playerKey(player);
        RateLimit limit = PLAYER_LIMITS
            .computeIfAbsent(key, ignored -> new RateLimit(PLAYER_REQUESTS_PER_SECOND, PLAYER_REQUEST_BURST, now));
        if (limit.isThrottled(now, count)) {
            IntegrationDiagnostics.recordThrottle();
            return true;
        }
        trimPlayerLimits();
        return false;
    }

    public static ContextResult resolvePlayerContext(EntityPlayerMP player, long now) {
        return IMPL.resolveContext(player, now);
    }

    public static AmountLookupResult lookupAmount(PlayerAeContext context, ItemStack stack, FluidStack fluidStack,
        String essentiaAspectTag, long now) {
        return IMPL.lookupAmount(context, stack, fluidStack, essentiaAspectTag, now);
    }

    public static List<AmountLookupResult> lookupAmountBatch(PlayerAeContext context, List<BatchLookupEntry> entries,
        long now) {
        return IMPL.lookupAmountBatch(context, entries, now);
    }

    public static int countDistinctLookupKeys(List<BatchLookupEntry> entries) {
        return IMPL.countDistinctLookupKeys(entries);
    }

    public static void clearCaches() {
        PLAYER_LIMITS.clear();
        IMPL.clearCaches();
    }

    public static void clearPlayer(EntityPlayerMP player) {
        if (player == null) return;
        PLAYER_LIMITS.remove(playerKey(player));
    }

    private static String playerKey(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        return uuid == null ? player.getCommandSenderName() : uuid.toString();
    }

    private static void cleanupCaches(long now) {
        if (now < nextCleanupTime) return;
        nextCleanupTime = now + CACHE_CLEANUP_INTERVAL_MS;
        PLAYER_LIMITS.entrySet()
            .removeIf(entry -> now - entry.getValue().lastRefillTime > CACHE_RETAIN_MS);
    }

    private static void trimPlayerLimits() {
        int over = PLAYER_LIMITS.size() - MAX_PLAYER_LIMITS;
        if (over <= 0) return;
        PriorityQueue<Map.Entry<String, RateLimit>> heap = new PriorityQueue<>(
            Comparator.comparingLong(e -> e.getValue().lastRefillTime));
        heap.addAll(PLAYER_LIMITS.entrySet());
        for (int i = 0; i < over; i++) {
            Map.Entry<String, RateLimit> e = heap.poll();
            if (e != null) PLAYER_LIMITS.remove(e.getKey());
        }
    }

    private static final class RateLimit {

        private final int perSecond;
        private final int burst;
        private double tokens;
        private long lastRefillTime;

        private RateLimit(int perSecond, int burst, long now) {
            this.perSecond = perSecond;
            this.burst = burst;
            this.tokens = burst;
            this.lastRefillTime = now;
        }

        private boolean isThrottled(long now, int count) {
            if (count <= 0) {
                return false;
            }
            refill(now);
            if (this.tokens < count) {
                return true;
            }
            this.tokens -= count;
            return false;
        }

        private void refill(long now) {
            if (now <= this.lastRefillTime) return;
            long elapsed = Math.min(now - this.lastRefillTime, 60000L);
            if (elapsed > 0L) {
                this.tokens = Math.min(this.burst, this.tokens + elapsed * this.perSecond / 1000.0D);
                this.lastRefillTime = now;
            }
        }
    }
}
