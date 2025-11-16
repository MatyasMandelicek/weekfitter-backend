package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.dto.LoginResponse;
import com.weekfitter.weekfitter_backend.model.*;
import com.weekfitter.weekfitter_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servisní vrstva pro práci s uživateli.
 *
 * Řeší:
 * - registraci
 * - login + generování JWT
 * - obnovu hesla
 * - načítání uživatelů
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

    /** Encoder pro hashování hesel. */
    private PasswordEncoder passwordEncoder;

    /** Registruje nového uživatele. */
    public User registerUser(User user) {

        // Zahashování hesla
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Výchozí avatar podle pohlaví
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

    /** Vrátí uživatele podle e-mailu, nebo vyhodí výjimku. */
    public User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Uživatel s e-mailem " + email + " neexistuje"));
    }

    /** Vygeneruje reset token a odešle e-mail. */
    public void generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, token);
    }

    /** Změní heslo podle tokenu. */
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (user.getTokenExpiration().isBefore(LocalDateTime.now())) return false;

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiration(null);

        userRepository.save(user);
        return true;
    }

    /**
     * Přihlášení uživatele + vytvoření JWT tokenu.
     *
     * Tady používáme stejné ověřování jako původně:
     * - najdeme uživatele podle e-mailu
     * - porovnáme zadané heslo s BCrypt hashem v DB
     * - při úspěchu vygenerujeme JWT token
     */
    public LoginResponse loginAndCreateToken(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Neplatný e-mail nebo heslo"));

        // Ověření hesla pomocí BCrypt
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Neplatný e-mail nebo heslo");
        }

        // Vygenerujeme JWT token (subject = email uživatele)
        String token = jwtService.generateToken(email);

        // Vrátíme token + základní info
        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFirstName()
        );
    }
}
