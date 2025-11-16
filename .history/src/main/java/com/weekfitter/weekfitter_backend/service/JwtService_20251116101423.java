package com.weekfitter.weekfitter_backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Služba pro práci s JWT tokeny – generování, parsování a ověřování.
 */
@Service
public class JwtService {

    @Value("${JWT_SECRET:default-secret-key-change-me}")
    private String secretKey;

    // Platnost tokenu – např. 24 hodin
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000;

    /** Vygeneruje JWT token pro zadaný e-mail uživatele. */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Z tokenu vytáhne e-mail (subject). */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Ověří, zda je token platný (neexpiroval a má validní podpis). */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // pokud padne výjimka, token je neplatný
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
