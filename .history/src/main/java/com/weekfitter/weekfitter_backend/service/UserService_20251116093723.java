package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.*;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servisní vrstva zodpovědná za práci s uživatelskými účty.
 *
 * Obsahuje hlavní aplikační logiku pro:
 * - registraci nových uživatelů,
 * - ověřování přihlášení (login),
 * - generování a ověřování tokenů pro obnovu hesla,
 * - zasílání e-mailů pomocí EmailService.
 *
 * Oddělení této logiky do samostatné vrstvy zvyšuje přehlednost,
 * znovupoužitelnost a usnadňuje testování.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registruje nového uživatele.
     *
     * Postup:
     * - zahashuje jeho heslo (BCrypt),
     * - nastaví výchozí avatar podle pohlaví,
     * - uloží uživatele do databáze.
     *
     * Poznámka:
     * E-mail musí být unikátní – validace je řešena databází (unique constraint).
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getPhoto() == null || user.getPhoto().isEmpty()) {
            if (user.getGender() == Gender.FEMALE) {
                user.setPhoto("/assets/default-avatar-female.png");
            } else if (user.getGender() == Gender.MALE) {
                user.setPhoto("/assets/default-avatar.png");
            } else {
                user.setPhoto("/assets/default-avatar-other.png");
            }
        }

        return userRepository.save(user);
    }

    /**
     * Ověřuje přihlášení uživatele podle e-mailu a hesla.
     */
    public boolean loginUser(String email, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return passwordEncoder.matches(rawPassword, optionalUser.get().getPassword());
        }
        return false;
    }

    /**
     * Generuje token pro reset hesla, ukládá jej do databáze
     * a odesílá uživateli e-mail s odkazem na obnovu.
     */
    public void generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }
    }

    /**
     * Ověřuje platnost reset tokenu a nastavuje nové heslo.
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getTokenExpiration().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null);
                user.setTokenExpiration(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
