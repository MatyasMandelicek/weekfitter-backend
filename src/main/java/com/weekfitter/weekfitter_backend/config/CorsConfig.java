package com.weekfitter.weekfitter_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Konfigurace CORS (Cross-Origin Resource Sharing).
 * Umožňuje bezpečnou komunikaci mezi frontendem (React na Vercelu)
 * a backendem (Spring Boot na Renderu).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Povolený původ (frontend aplikace)
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://weekfitter-frontend.vercel.app/"));

        // Povolené HTTP metody
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Povolené hlavičky
        config.setAllowedHeaders(List.of("*"));

        // Povolení přenosu cookies / autentizace
        config.setAllowCredentials(true);

        // Aplikace konfigurace na všechny endpointy
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
