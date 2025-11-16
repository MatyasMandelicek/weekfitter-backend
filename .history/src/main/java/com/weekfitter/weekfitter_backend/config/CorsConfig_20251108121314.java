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
 * Umožňuje frontendové části (např. React aplikaci) komunikovat s API backendu.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Povolené původy (produkce + preview + lokální vývoj)
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
