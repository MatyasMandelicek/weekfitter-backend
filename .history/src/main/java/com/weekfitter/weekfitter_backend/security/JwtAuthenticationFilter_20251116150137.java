package com.weekfitter.weekfitter_backend.security;

import com.weekfitter.weekfitter_backend.service.CustomUserDetailsService;
import com.weekfitter.weekfitter_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT autentizační filtr – zpracovává každý HTTP request právě jednou.
 *
 * Úkolem filtru je:
 * - přeskočit veřejné endpointy (registrace, login, reset hesla),
 * - zachytit Authorization hlavičku,
 * - validovat JWT token,
 * - pokud je token platný → načíst uživatele a vložit autentizaci do SecurityContextu.
 *
 * Pokud request neobsahuje token (nebo míří na veřejný endpoint),
 * filtr pouze pustí požadavek dál bez zásahů.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Služba pro práci s JWT tokeny (validace, extrakce emailu). */
    private final JwtService jwtService;

    /** Načítá uživatelské detaily (Spring Security UserDetails). */
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Získáme URL cestu požadavku
        String path = request.getServletPath();

        // 1) VEŘEJNÉ ENDPOINTY – filtr MUSÍ být přeskočen
        // Pokud bychom filtr aplikovali na /register nebo /login,
        // Spring by nedokázal přečíst JSON tělo requestu → heslo == null.
        //
        // Tohle byl přesně důvod tvé chyby "rawPassword cannot be null".
        //
        if (path.startsWith("/api/users/register") ||
            path.startsWith("/api/users/login") ||
            path.startsWith("/api/users/forgot-password") ||
            path.startsWith("/api/users/reset-password") ||
            path.startsWith("/api/health")) {

            // Přeskočíme JWT logiku – request pokračuje rovnou do controlleru
            filterChain.doFilter(request, response);
            return;
        }

        // 2) JWT VALIDACE
        // Zkusíme získat hodnotu HTTP hlavičky Authorization
        final String authHeader = request.getHeader("Authorization");

        // Chybí hlavička nebo nezačíná na "Bearer " → žádný token → nic neautentizujeme
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Získáme samotný token bez prefixu "Bearer "
        final String jwt = authHeader.substring(7);

        // Pokusíme se z tokenu extrahovat email (subject)
        final String userEmail = jwtService.extractEmail(jwt);

        // Pokud máme email a zatím není žádný uživatel v SecurityContextu
        if (userEmail != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            // Načteme detaily uživatele z DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Ověříme, zda je JWT token validní (podpis, expirace)
            if (jwtService.isTokenValid(jwt)) {

                // Vytvoříme autentizační objekt pro Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Přidáme kontext requestu (IP adresa, User-Agent…)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Uložíme autentizaci → uživatel je nyní považován za přihlášeného
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Pokračujeme dál v řetězci filtrů (request běží do controlleru)
        filterChain.doFilter(request, response);
    }
}
