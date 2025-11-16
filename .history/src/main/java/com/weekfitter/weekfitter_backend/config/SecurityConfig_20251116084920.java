package com.weekfitter.weekfitter_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Konfigurace zabezpečení aplikace.
 *
 * Tato třída definuje způsob zabezpečení REST API pomocí Spring Security.
 * U REST rozhraní se běžně vypíná CSRF ochrana (používaná hlavně pro formuláře),
 * endpointy jsou nastavovány jako veřejné nebo chráněné a také se vypínají
 * výchozí login formuláře či BasicAuth, protože autentizace probíhá vlastní cestou.
 */
@Configuration
public class SecurityConfig {

    /**
     * Definuje hlavní bezpečnostní pravidla pomocí SecurityFilterChain.
     *
     * - vypíná CSRF, protože API nemá stavovou session s formuláři,
     * - nastavuje, které endpointy jsou veřejně přístupné,
     * - vyžaduje autentizaci pro ostatní části API,
     * - deaktivuje výchozí přihlašovací mechanismy Spring Security,
     *   protože aplikace používá vlastní autentizační tok (login endpoint).
     *
     * @param http hlavní HTTP konfigurátor
     * @return nakonfigurovaný bezpečnostní filtr
     * @throws Exception pokud by nastala chyba konfigurace
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF není potřeba, protože aplikace nepracuje s formuláři ani session
                .csrf(csrf -> csrf.disable())

                // Nastavení přístupových pravidel
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/api/users/register",
                        "/api/users/login",
                        "/api/users/forgot-password",
                        "/api/users/reset-password",
                        "/api/users/profile",          
                        "/api/users/upload-photo",     
                        "/api/users/photo/**",         
                        "/api/events/**",
                        "/api/health",
                        "/error"
                    ).permitAll()
                    .anyRequest().authenticated()                    
                )

                // Deaktivace výchozího HTML login formuláře Spring Security
                .formLogin(form -> form.disable())

                // Deaktivace BasicAuth (používáte vlastní autentizaci)
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * Bean pro hashování hesel pomocí BCrypt algoritmu.
     * Používá se při registraci a změně hesla v UserService.
     *
     * @return password encoder spravovaný Springem
     */    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
