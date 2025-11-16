package com.weekfitter.weekfitter_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Konfigurace CORS (Cross-Origin Resource Sharing).
 *
 * CORS pravidla určují, z jakých domén je povoleno volat API backendu.
 * Je to nezbytné zejména u SPA frontend aplikací (např. React), které běží
 * na jiné doméně než backend. Bez správného nastavení by prohlížeč
 * komunikaci zablokoval z bezpečnostních důvodů.
 */
@Configuration
public class CorsConfig {

    /**
     * Vytváří a registruje globální CORS filtr.
     *
     * Tento filtr zajišťuje:
     * - definici povolených domén (originů),
     * - specifikaci povolených HTTP metod,
     * - nastavení povolených hlaviček,
     * - možnost přidávat cookies / JWT (allowCredentials),
     * - registraci konfigurace pro všechny API endpointy.
     *
     * Filtr se spustí před obsloužením jakéhokoliv requestu a tím zabezpečí
     * správné chování komunikace mezi frontendem a backendem.
     *
     * @return CorsFilter spravovaný Springem
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Povolené původy (origin patterns podporují wildcard — např. pro všechny Vercel preview verze)
        config.setAllowedOriginPatterns(List.of(
                "https://*.vercel.app",
                "https://weekfitter.vercel.app",
                "http://localhost:3000"
        ));

        // Povolené HTTP metody
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Povolené hlavičky
        config.setAllowedHeaders(List.of("*"));

        // Povolení přenosu cookies / autentizace
        config.setAllowCredentials(true);

        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // Aplikace konfigurace na všechny endpointy
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
