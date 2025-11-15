package com.weekfitter.weekfitter_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Základní bezpečnostní konfigurace aplikace.
 * 
 * Vzhledem k tomu, že backend slouží jako REST API, je CSRF ochrana vypnuta
 * a vybrané endpointy jsou volně přístupné bez autentizace.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        // Zapneme CORS
        .cors(cors -> {})
        // REST API nevyužívá CSRF
        .csrf(csrf -> csrf.disable())

                // Povolené a chráněné endpointy
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
                        "/api/files/**",
                        "/error"
                    ).permitAll()
                    .anyRequest().authenticated()                    
                )

                // Deaktivace výchozího login formuláře                
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /** Bean pro šifrování hesel – používá se v UserService. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
