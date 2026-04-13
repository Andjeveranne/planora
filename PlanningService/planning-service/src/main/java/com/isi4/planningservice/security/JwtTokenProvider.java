package com.isi4.planningservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-change-this-in-production-environment-variable}")
    private String jwtSecret;

    public String extractUserId(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting userId from token", e);
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            log.error("Error extracting role from token", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
