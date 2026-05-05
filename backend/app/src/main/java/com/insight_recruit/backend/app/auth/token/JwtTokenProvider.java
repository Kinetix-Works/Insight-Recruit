package com.insight_recruit.backend.app.auth.token;

import com.insight_recruit.backend.app.config.SecurityProperties;
import com.insight_recruit.backend.app.domain.entity.TenantSubscription;
import com.insight_recruit.backend.app.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecurityProperties securityProperties;
    private final SecretKey key;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.key = Keys.hmacShaKeyFor(securityProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user, TenantSubscription subscription) {
        Instant now = Instant.now();
        Instant expiry = now.plus(securityProperties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
            .subject(user.getId().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim("tenantId", user.getTenant().getId().toString())
            .claim("role", user.getRole().name())
            .claim("subscriptionTier", subscription.getPlanType().name())
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(securityProperties.refreshTokenTtlDays(), ChronoUnit.DAYS);
        return Jwts.builder()
            .subject(user.getId().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim("tokenType", "refresh")
            .claim("tenantId", user.getTenant().getId().toString())
            .signWith(key)
            .compact();
    }

    public long accessTokenExpiresInSeconds() {
        return securityProperties.accessTokenTtlMinutes() * 60;
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }
}
