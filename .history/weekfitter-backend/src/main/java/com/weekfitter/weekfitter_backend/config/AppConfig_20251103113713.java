package com.weekfitter.weekfitter_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Základní aplikační konfigurace.
 * 
 * Definuje společné komponenty (Beany), které se používají napříč aplikací.
 */
@Configuration
public class AppConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
