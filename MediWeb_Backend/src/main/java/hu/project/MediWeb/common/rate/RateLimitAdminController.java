package hu.project.MediWeb.common.rate;

import hu.project.MediWeb.common.rate.RateLimitSettingsService.RateLimitSettingsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/rate-limit")
public class RateLimitAdminController {

    private final RateLimitSettingsService settings;

    public RateLimitAdminController(RateLimitSettingsService settings) {
        this.settings = settings;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RateLimitSettingsDto get() {
        return settings.snapshot();
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateLimitSettingsDto> update(@RequestBody Map<String, Object> body) {
        Boolean enabled = body.containsKey("enabled") ? (Boolean) body.get("enabled") : null;
        Integer window = body.containsKey("windowSeconds") ? ((Number) body.get("windowSeconds")).intValue() : null;
        Integer limit = body.containsKey("limit") ? ((Number) body.get("limit")).intValue() : null;
        String whitelist = (String) body.getOrDefault("whitelistCsv", null);
        settings.update(enabled, window, limit, whitelist);
        return ResponseEntity.ok(settings.snapshot());
    }
}
