package com.ktpm.salesmanagement.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ktpm.salesmanagement.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET_A = "test-secret-A-must-be-at-least-32-bytes-long";
    private static final String SECRET_B = "test-secret-B-must-be-at-least-32-bytes-long";
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void validJwtContainsIdentityUsernameRoleIssuedAtAndExpiration() {
        JwtService service = service(SECRET_A, 60_000, NOW);
        var claims = service.parseAndValidate(service.generateToken(principal()));

        assertThat(claims.getSubject()).isEqualTo("10");
        assertThat(claims.get("userId", Long.class)).isEqualTo(10L);
        assertThat(claims.get("username", String.class)).isEqualTo("admin01");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void jwtWithWrongSignatureIsRejected() {
        String token = service(SECRET_A, 60_000, NOW).generateToken(principal());
        assertThatThrownBy(() -> service(SECRET_B, 60_000, NOW).parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredJwtIsRejected() {
        String token = service(SECRET_A, 1_000, NOW).generateToken(principal());
        JwtService later = service(SECRET_A, 1_000, NOW.plusSeconds(10));
        assertThatThrownBy(() -> later.parseAndValidate(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    private static JwtService service(String secret, long expiration, Instant now) {
        return new JwtService(new JwtProperties(secret, expiration), Clock.fixed(now, ZoneOffset.UTC));
    }

    private static CustomUserPrincipal principal() {
        User user = new User();
        user.setId(10L);
        user.setUsername("admin01");
        user.setPasswordHash("unused-hash");
        user.setFullName("Admin Demo");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");
        return CustomUserPrincipal.from(user);
    }
}
