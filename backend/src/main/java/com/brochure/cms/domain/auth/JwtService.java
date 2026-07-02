package com.brochure.cms.domain.auth;

import com.brochure.cms.config.JwtConfig;
import com.brochure.cms.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtConfig.expiryDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("tid", user.getTenantId().toString())
                .claim("role", user.getRole().toDb())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Optional<JwtClaims> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            UUID tenantId = UUID.fromString(claims.get("tid", String.class));
            String role = claims.get("role", String.class);
            return Optional.of(new JwtClaims(userId, tenantId, role));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String generatePageUnlockToken(UUID pageId, UUID tenantId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(24, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(pageId.toString())
                .claim("tid", tenantId.toString())
                .claim("type", "page_unlock")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Optional<UUID> parsePageUnlockToken(String token, UUID tenantId) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!"page_unlock".equals(claims.get("type", String.class))) {
                return Optional.empty();
            }
            UUID pageId = UUID.fromString(claims.getSubject());
            UUID tid = UUID.fromString(claims.get("tid", String.class));
            if (!tenantId.equals(tid)) {
                return Optional.empty();
            }
            return Optional.of(pageId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record JwtClaims(UUID userId, UUID tenantId, String role) {}
}
