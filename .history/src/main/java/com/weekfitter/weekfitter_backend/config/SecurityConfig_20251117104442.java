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
 * Hlavní bezpečnostní konfigurace aplikace.
 *
 * - zapíná podporu CORS (nutné pro komunikaci s front-endem),
 * - vypíná CSRF (REST API ho nepotřebuje),
 * - nastavuje stateless režim kvůli JWT,
 * - definuje veřejné a chráněné endpointy,
 * - přidává náš JWT filtr,
 * - vypíná klasické formLogin / httpBasic.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filtr, který zpracovává JWT token z Authorization headeru. */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Načítání detailů uživatele pro ověřování. */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Hlavní konfigurace Spring Security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /**
                 * Zapnutí CORS ve Spring Security.
                 * Díky tomu se správně použije CorsFilter z třídy CorsConfig.
                 * Bez toho by preflight (OPTIONS) requesty padaly.
                 */
                .cors(cors -> {})

                /**
                 * CSRF vypínáme — REST API nepoužívá cookie-based session,
                 * takže CSRF tokeny nedávají smysl.
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * Aplikace funguje kompletně bez HTTP session (stateless).
                 * Všechno řízení přihlášení obstarává JWT.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /**
                 * Definování, které endpointy jsou veřejné
                 * a které vyžadují JWT token.
                 */
                .authorizeHttpRequests(auth -> auth

                        // Veřejné endpointy (bez JWT)
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/forgot-password",
                                "/api/users/reset-password",
                                "/api/health",
                                "/error"
                        ).permitAll()

                        // Všechno ostatní je chráněné (vyžaduje platný JWT)
                        .anyRequest().authenticated()
                )

                /**
                 * Zde určujeme, jak se má ověřovat uživatel:
                 * - načíst z DB (CustomUserDetailsService),
                 * - porovnat heslo pomocí BCrypt.
                 */
                .authenticationProvider(authenticationProvider())

                /**
                 * Vložíme náš JWT filtr do řetězce ještě PŘED
                 * UsernamePasswordAuthenticationFilter, aby
                 * se nejdřív pokusil ověřit token.
                 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                /**
                 * Nepoužíváme žádné formuláře ani základní HTTP login.
                 * Autentizace je řešena pouze přes JWT.
                 */
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * AuthenticationManager — zajišťuje přihlášení pomocí CustomUserDetailsService
     * a ověření hesla.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * AuthenticationProvider říká Springu:
     * - kde má hledat uživatele (UserDetailsService),
     * - jak má ověřit heslo (BCrypt).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * BCrypt encoder — stejný používáme na registraci i login,
     * aby porovnání hashů sedělo.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
