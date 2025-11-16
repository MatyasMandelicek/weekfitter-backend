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
 * Provádí:
 * - čtení hlavičky Authorization,
 * - validaci JWT tokenu,
 * - načtení uživatele a vložení autentizace do SecurityContextu.
 *
 * Spouští se jednou pro každý request (OncePerRequestFilter).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Služba pro práci s JWT tokeny (validace, extrakce emailu). */
    private final JwtService jwtService;

    /** Načítání uživatelských údajů pro Spring Security. */
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        /**
         * Veřejné endpointy – zde se JWT nesmí aplikovat.
         * Pokud by filtr běžel nad login/registrací, Spring by ztratil request body,
         * což způsobovalo chybu: rawPassword == null.
         */
        if (path.startsWith("/api/users/register") ||
            path.startsWith("/api/users/login") ||
            path.startsWith("/api/users/forgot-password") ||
            path.startsWith("/api/users/reset-password") ||
            path.startsWith("/api/health")) {

            filterChain.doFilter(request, response);
            return;
        }

        /** Čtení HTTP hlavičky Authorization. */
        final String authHeader = request.getHeader("Authorization");

        /** Pokud není token přítomen nebo neobsahuje Bearer prefix → pokračujeme dál. */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /** Získání tokenu (bez prefixu Bearer). */
        final String jwt = authHeader.substring(7);

        /** Extrakce emailu (subject) z JWT. */
        final String userEmail = jwtService.extractEmail(jwt);

        /**
         * Pokud máme email a zatím není autentizace ve SecurityContextu,
         * validujeme token a nastavíme přihlášeného uživatele.
         */
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            /** Ověření platnosti tokenu (podpis + expirace). */
            if (jwtService.isTokenValid(jwt)) {

                /** Vytvoření autentizačního objektu. */
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                /** Doplnění detailů o requestu. */
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                /** Uložení autentizace – uživatel je považován za přihlášeného. */
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        /** Pokračování v řetězci filtrů. */
        filterChain.doFilter(request, response);
    }
}
