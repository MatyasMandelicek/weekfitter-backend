package com.weekfitter.weekfitter_backend.config;

import com.weekfitter.weekfitter_backend.security.JwtAuthenticationFilter;
import com.weekfitter.weekfitter_backend.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Bezpečnostní konfigurace aplikace WeekFitter.
 *
 * Zajišťuje:
 * - JWT autentizaci (stateless přístup),
 * - povolení veřejných endpointů (login, registrace, reset hesla),
 * - ochranu všech ostatních API endpointů,
 * - integraci JwtAuthenticationFilter do řetězce filtrů.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /** Vlastní JWT autentizační filtr, který zpracovává requesty. */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Služba, která Spring Security poskytuje informace o uživateli. */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Hlavní konfigurace Spring Security.
     *
     * - vypíná CSRF (pro REST API je zbytečné),
     * - nastavuje bezstavový režim (stateless),
     * - definuje veřejné endpointy,
     * - přidává náš vlastní JWT filtr před UsernamePasswordAuthenticationFilter,
     * - vypíná Basic Auth a login formulář.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API nepoužívá CSRF tokeny
                .csrf(csrf -> csrf.disable())

                // API je stateless – nepoužívá HTTP Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Nastavení přístupů
                .authorizeHttpRequests(auth -> auth
                        // Veřejné endpointy, které NEVYŽADUJÍ JWT token
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/forgot-password",
                                "/api/users/reset-password",
                                "/api/health",
                                "/error"
                        ).permitAll()

                        // Všechno ostatní vyžaduje autentizaci
                        .anyRequest().authenticated()
                )

                // Authentication Provider → práce s detaily uživatelů a heslem
                .authenticationProvider(authenticationProvider())

                // Přidání JWT filtru před standardní Spring Security filtr
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Nepotřebujeme formulář pro login
                .formLogin(form -> form.disable())

                // Nepoužíváme základní HTTP autentizaci
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * Správce autentizace – používá se při loginu.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * AuthenticationProvider říká Spring Security:
     * - jak získat uživatele (CustomUserDetailsService),
     * - jak ověřit heslo (BCrypt).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * BCrypt encoder – stejný pro registraci i login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
