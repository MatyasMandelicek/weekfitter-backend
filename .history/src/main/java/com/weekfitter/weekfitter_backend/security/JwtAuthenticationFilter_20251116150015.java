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
 * JWT autentizační filtr.
 *
 * OncePerRequestFilter zajišťuje, že filtr se spustí přesně jednou pro každý request.
 *
 * Úkolem filtru je:
 * - přečíst hlavičku Authorization,
 * - ověřit, zda obsahuje platný JWT token,
 * - pokud ano, vyhledá uživatele podle emailu z tokenu,
 * - vloží autentizaci do SecurityContextu,
 * - poté propustí request dál.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Získáme hodnotu hlavičky Authorization
        String authHeader = request.getHeader("Authorization");

        // Pokud hlavička neexistuje, nebo nezačíná na "Bearer ",
        // znamená to, že request nemá JWT token → pouštíme dál bez autentizace
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ořízneme "Bearer " a získáme samotný token
        final String jwt = authHeader.substring(7);

        // Z tokenu se pokusíme vytáhnout email (subject)
        final String userEmail = jwtService.extractEmail(jwt);

        // Pokud máme email z tokenu a zároveň není v SecurityContextu žádný přihlášený uživatel
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Načteme UserDetails z DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Ověříme platnost tokenu (podpis + expirace)
            if (jwtService.isTokenValid(jwt)) {

                // Vytvoříme autentizační objekt pro Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Doplníme detaily o requestu (např. IP adresu)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Uložíme tuto autentizaci do SecurityContextu
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Pokračujeme dál v řetězci filtrů (request běží dál do controlleru)
        filterChain.doFilter(request, response);
    }
}
