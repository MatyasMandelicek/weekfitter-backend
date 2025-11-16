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

    /**
     * Vytváří Bean pro šifrování hesel pomocí algoritmu BCrypt.
     *
     * BCrypt je moderní hashovací algoritmus navržený přímo pro bezpečné ukládání hesel.
     * Provádí tzv. „saltování“ (přidávání náhodných dat) a opakované procházení hash funkcí,
     * aby bylo výrazně obtížnější získat původní heslo metodami jako je brute-force.
     *
     * Tento Bean se automaticky injektuje do služeb, které potřebují pracovat
     * s hesly uživatelů (typicky při registraci nebo změně hesla).
     *
     * @return instance BCryptPasswordEncoder spravovaná Springem
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
