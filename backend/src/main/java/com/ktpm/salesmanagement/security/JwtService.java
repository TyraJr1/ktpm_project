package com.ktpm.salesmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMillis;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtService(JwtProperties properties, Clock clock) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = properties.expiration();
        this.clock = clock;
    }

    public String generateToken(CustomUserPrincipal principal) {
        Instant issuedAt = clock.instant();
        return Jwts.builder()
                .subject(principal.getUserId().toString())
                .claim("userId", principal.getUserId())
                .claim("username", principal.getUsername())
                .claim("role", principal.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusMillis(expirationMillis)))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser().verifyWith(signingKey)
                .clock(() -> Date.from(clock.instant()))
                .build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) {
        return parseAndValidate(token).get("username", String.class);
    }
}
