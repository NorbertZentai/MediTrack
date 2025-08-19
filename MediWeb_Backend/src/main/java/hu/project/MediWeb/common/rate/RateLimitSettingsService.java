package hu.project.MediWeb.common.rate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds mutable runtime rate limiting settings. Initialized from properties, updatable via admin endpoint.
 */
@Component
public class RateLimitSettingsService {
    private volatile boolean enabled;
    private volatile int windowSeconds;
    private volatile int limit;
    private volatile Set<String> whitelistPaths;

    public RateLimitSettingsService(
            @Value("${rate-limiting.enabled:true}") boolean enabled,
            @Value("${rate-limiting.window-seconds:60}") int windowSeconds,
            @Value("${rate-limiting.limit:10}") int limit,
            @Value("${rate-limiting.whitelist-paths:/actuator/health,/actuator/info,/auth/login,/auth/register}") String whitelistCsv
    ) {
        this.enabled = enabled;
        this.windowSeconds = windowSeconds;
        this.limit = limit;
        this.whitelistPaths = parseWhitelist(whitelistCsv);
    }

    private Set<String> parseWhitelist(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }

    public boolean isEnabled() { return enabled; }
    public int getWindowSeconds() { return windowSeconds; }
    public int getLimit() { return limit; }
    public Set<String> getWhitelistPaths() { return whitelistPaths; }

    public synchronized void update(Boolean enabled, Integer windowSeconds, Integer limit, String whitelistCsv) {
        if (enabled != null) this.enabled = enabled;
        if (windowSeconds != null && windowSeconds > 0) this.windowSeconds = windowSeconds;
        if (limit != null && limit > 0) this.limit = limit;
        if (whitelistCsv != null) this.whitelistPaths = parseWhitelist(whitelistCsv);
    }

    public RateLimitSettingsDto snapshot() {
        return new RateLimitSettingsDto(enabled, windowSeconds, limit, whitelistPaths);
    }

    public record RateLimitSettingsDto(boolean enabled, int windowSeconds, int limit, Set<String> whitelistPaths) {}
}
