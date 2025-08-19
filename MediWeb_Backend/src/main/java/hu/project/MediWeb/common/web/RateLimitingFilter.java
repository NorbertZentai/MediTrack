package hu.project.MediWeb.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Iterator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Egyszerű in-memory IP alapú rate limiter külső dependency nélkül.
 * Limit: max 10 kérés / 60 másodperc / IP.
 * Nem production-grade (nincs elosztott szinkronizáció, memória tisztítás korlátozott),
 * de alap védelem brute-force vagy flood ellen.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${rate-limiting.enabled:true}")
    private boolean enabled;
    @Value("${rate-limiting.window-seconds:60}")
    private int windowSeconds;
    @Value("${rate-limiting.limit:10}")
    private int limit;
    @Value("${rate-limiting.whitelist-paths:/actuator/health,/actuator/info,/auth/login,/auth/register}")
    private String whitelistCsv;

    private final Map<String, Deque<Long>> requestsPerIp = new ConcurrentHashMap<>();
    private volatile long lastPurge = 0L;
    private static final long PURGE_INTERVAL_MS = 300_000L; // 5 minutes

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        for (String wl : whitelistCsv.split(",")) {
            String trimmed = wl.trim();
            if (!trimmed.isEmpty() && path.startsWith(trimmed)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

    int effLimit = limit > 0 ? limit : 10;
        long windowMillis = (windowSeconds > 0 ? windowSeconds : 60) * 1000L;
    String ip = request.getRemoteAddr();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String principal = (auth != null && auth.isAuthenticated() && auth.getName() != null) ? auth.getName() : null;
    String key = principal != null ? principal : ip;
        long now = Instant.now().toEpochMilli();

    Deque<Long> timestamps = requestsPerIp.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            // Régi időbélyegek törlése
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= effLimit) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too Many Requests\",\"limit\":" + effLimit + ",\"windowSeconds\":" + (windowMillis/1000) + "}");
        response.setHeader("Retry-After", String.valueOf(windowMillis / 1000));
        response.setHeader("X-RateLimit-Limit", String.valueOf(effLimit));
        response.setHeader("X-RateLimit-Remaining", "0");
                return;
            }
            timestamps.addLast(now);
        response.setHeader("X-RateLimit-Limit", String.valueOf(effLimit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, effLimit - timestamps.size())));
        }
        // Opportunistic purge of empty / stale entries
        if (now - lastPurge > PURGE_INTERVAL_MS) {
            synchronized (this) {
                if (now - lastPurge > PURGE_INTERVAL_MS) {
                    Iterator<Map.Entry<String, Deque<Long>>> it = requestsPerIp.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Deque<Long>> e = it.next();
                        Deque<Long> dq = e.getValue();
                        synchronized (dq) {
                            if (dq.isEmpty() || (now - dq.peekLast() > windowMillis)) {
                                it.remove();
                            }
                        }
                    }
                    lastPurge = now;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
