package com.nodewave.portal.core.security;

import com.nodewave.portal.core.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:nodewave-super-secret-key-2026-very-secure-must-be-long-enough-32bytes}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.key = deriveSecretKey(secret);
        this.expirationMs = expirationMs;
    }

    private SecretKey deriveSecretKey(String secret) {
        try {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                bytes = sha256.digest(bytes);
            }
            return Keys.hmacShaKeyFor(bytes);
        } catch (NoSuchAlgorithmException e) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId())
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .claim("department", user.getDepartment().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
