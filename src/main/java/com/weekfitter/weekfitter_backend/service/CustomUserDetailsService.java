package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsService je součást Spring Security.
 *
 * Slouží k načtení uživatele podle e-mailu (username).
 * Spring Security ji používá při:
 *  - klasickém loginu (e-mail + heslo),
 *  - ověřování JWT tokenů (z tokenu získáme e-mail a uživatele načteme zde).
 *
 * V naší aplikaci nejsou žádné role (ROLE_USER, ROLE_ADMIN),
 * proto vracíme prázdný seznam authorities.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Načte uživatele podle e-mailu a převede ho na UserDetails,
     * což je objekt, kterému Spring Security rozumí.
     *
     * @param email e-mail uživatele
     * @return UserDetails – objekt obsahující heslo a další info
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Vyhledáme uživatele v databázi
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Uživatel s e-mailem " + email + " nebyl nalezen.")
                );

        /**
         * Vracíme implementaci UserDetails:
         *
         * - první parametr: username (u nás e-mail),
         * - druhý parametr: heslo (už hashed pomocí BCrypt),
         * - třetí parametr: seznam oprávnění (u nás prázdný).
         *
         * Pokud budeš někdy používat role, přidají se sem.
         */
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
