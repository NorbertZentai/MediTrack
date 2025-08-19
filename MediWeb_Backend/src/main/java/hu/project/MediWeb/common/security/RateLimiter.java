package hu.project.MediWeb.common.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple in-memory fixed window rate limiter for low volume endpoints (e.g., /auth/login). */
@Component
public class RateLimiter {
    private static class Counter { int count; long windowStart; }

    private final Map<String, Counter> store = new ConcurrentHashMap<>();

    private final int limit = 10; // attempts
    private final long windowMillis = 60_000; // 1 minute

    public boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Counter c = store.computeIfAbsent(key, k -> { Counter nc = new Counter(); nc.windowStart = now; return nc; });
        synchronized (c) {
            if (now - c.windowStart >= windowMillis) { c.windowStart = now; c.count = 0; }
            if (c.count >= limit) return false;
            c.count++; return true;
        }
    }
}
