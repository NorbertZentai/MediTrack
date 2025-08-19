package hu.project.MediWeb.config;

import hu.project.MediWeb.security.JwtAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import hu.project.MediWeb.security.JsonAuthenticationEntryPoint;
import hu.project.MediWeb.security.JsonAccessDeniedHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JsonAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .anonymous(AbstractHttpConfigurer::disable) // disable anonymous to get 401 instead of 403
        .headers(h -> h
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            .frameOptions(f -> f.sameOrigin())
            .httpStrictTransportSecurity(ht -> ht.includeSubDomains(true).maxAgeInSeconds(31536000))
            .contentTypeOptions(withDefaults())
        )
        .authorizeHttpRequests(auth -> auth
            // Public auth & info endpoints
            .requestMatchers("/auth/login", "/auth/register").permitAll()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
            .requestMatchers("/static/**", "/assets/**").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
            // Public API surface (explicit namespace only)
            .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
            // Secure user features
            .requestMatchers("/api/favorites/**", "/api/profile/**", "/api/notification/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/review/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
            // Segregated namespaces
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/admin/rate-limit/**").hasRole("ADMIN")
            .requestMatchers("/api/secure/**").authenticated()
            // Authenticated self info
            .requestMatchers("/auth/me").authenticated()
            // Test utility
            .requestMatchers("/test/boom").permitAll()
            // Everything else -> auth
            .anyRequest().authenticated()
        )
        .exceptionHandling(e -> e
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        )
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}