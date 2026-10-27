package com.gqibindlala.booking.security;

import com.gqibindlala.booking.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String issueToken(AppUser user) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
            .subject(user.getLoginIdentifier())
            .claim("userId", user.getId())
            .claim("fullName", user.getFullName())
            .claim("personalEmail", user.getPersonalEmail())
            .claim("phone", user.getPhone())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expirationMs)));
        if (user.getDepartment() != null) {
            builder.claim("departmentId", user.getDepartment().getId());
            builder.claim("departmentName", user.getDepartment().getName());
        }
        return builder.signWith(signingKey).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}