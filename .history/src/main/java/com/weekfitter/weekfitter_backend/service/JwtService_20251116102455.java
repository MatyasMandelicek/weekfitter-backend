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
 * Servisní třída pro práci s JWT tokeny.
 *
 * Odpovídá za:
 * - generování JWT tokenu pro přihlášeného uživatele,
 * - parsování tokenu a získání e-mailu (subject),
 * - ověření platnosti tokenu (podpis + expirace).
 *
 * JWT token obsahuje jako "subject" e-mail uživatele.
 * Podpis je řešen symetrickým klíčem (HMAC-SHA256).
 */
@Service
public class JwtService {

    /**
     * Tajný klíč pro podepisování tokenů.
     *
     * Hodnota se načítá z application.properties / ENV proměnné
     * pod názvem JWT_SECRET.
     *
     * Poznámka:
     * - hodnota by měla být BASE64 řetězec,
     * - pro produkci je vhodné použít dlouhý, náhodný klíč.
     */
    @Value("${JWT_SECRET}")
    private String secretKey;

    /**
     * Doba platnosti tokenu v milisekundách.
     *
     * Zde je nastaveno 24 hodin:
     */
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000;

    /**
     * Vygeneruje nový JWT token pro daný e-mail uživatele.
     *
     * - subject = e-mail uživatele,
     * - issuedAt = čas vytvoření,
     * - expiration = issuedAt + EXPIRATION_MS,
     * - podpis = HMAC-SHA256 s tajným klíčem.
     */
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

    /**
     * Z tokenu vytáhne e-mail (uložený v "subject").
     *
     * @param token JWT token
     * @return e-mail uživatele
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Ověří, zda je token platný.
     *
     * Metoda:
     * - pokusí se token parsovat a ověřit podpis,
     * - pokud dojde k výjimce (expirace, neplatný podpis, poškozený token),
     *   vrací false.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException – problémy s podpisem, expirovaný token apod.
            // IllegalArgumentException – např. prázdný token
            return false;
        }
    }

    /**
     * Obecná pomocná metoda – z tokenu získá libovolný "claim".
     *
     * @param token    JWT token
     * @param resolver funkce, která z Claims objektu vytáhne požadovanou hodnotu
     */
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Rozparsuje celý JWT token a vrátí Claims (payload).
     *
     * Pokud je token neplatný (podpis nesedí, je expirovaný, poškozený),
     * vyhodí výjimku JwtException.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Vrátí kryptografický klíč pro podepisování a ověřování JWT.
     *
     * Očekává, že secretKey je BASE64 kódovaný řetězec.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
