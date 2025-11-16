package com.weekfitter.weekfitter_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Aplikační konfigurační třída.
 *
 * Slouží k definování společných komponent (Beanů), které jsou sdílené napříč
 * celou aplikací. Spring tyto Beany spravuje a zajišťuje jejich životní cyklus.
 */
@Configuration
public class AppConfig {

    /** Bean pro šifrování hesel pomocí BCrypt algoritmu. */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
