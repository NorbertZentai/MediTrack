package hu.project.MediWeb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ConfigStartupValidator {
    private static final Logger log = LoggerFactory.getLogger(ConfigStartupValidator.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    private final Environment environment;

    public ConfigStartupValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        // Skip strict validation on test profile to allow in-memory DB auto configs
    if (environment != null && environment.acceptsProfiles(Profiles.of("test"))) {
            if (jwtSecret == null || jwtSecret.length() < 32) {
                log.warn("config.validation.test-profile skipping strict checks (jwtSecretLength={})", jwtSecret == null ? 0 : jwtSecret.length());
            }
            return;
        }

        if (datasourceUrl.isBlank()) {
            throw new IllegalStateException("spring.datasource.url missing. Provide database URL.");
        }
        if (jwtSecret == null || jwtSecret.length() < 32) {
            log.error("config.validation jwt.secret length={} (must be >=32)", jwtSecret == null ? 0 : jwtSecret.length());
            throw new IllegalStateException("jwt.secret too short (<32 chars)");
        }
        log.info("config.validation.ok datasource present jwtSecretLength={}", jwtSecret.length());
    }
}
