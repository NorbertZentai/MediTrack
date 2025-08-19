package hu.project.MediWeb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationInMediWebApplicationThatIsLongEnoughForHMACAlgorithm}")
    private String jwtSecret;

    @Value("${jwt.expiration:43200000}") // 12 hours default
    private int jwtExpirationMs;

    private Key getSigningKey() {
        // Ensure the key is at least 256 bits (32 bytes) for HMAC-SHA256
        byte[] keyBytes = jwtSecret.getBytes();
    if (keyBytes.length < 32) throw new IllegalStateException("JWT secret too short (<32 bytes). Provide stronger secret via jwt.secret");
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("jwt.validate.invalid msg={}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("jwt.validate.expired msg={}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("jwt.validate.unsupported msg={}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("jwt.validate.empty-claims msg={}", e.getMessage());
        }
        return false;
    }
}
