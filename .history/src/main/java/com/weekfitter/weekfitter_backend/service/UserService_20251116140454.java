package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.dto.LoginResponse;
import com.weekfitter.weekfitter_backend.model.*;
import com.weekfitter.weekfitter_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** Registruje nového uživatele. */
    public User registerUser(User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getPhoto() == null || user.getPhoto().isEmpty()) {
            switch (user.getGender()) {
                case FEMALE -> user.setPhoto("/assets/default-avatar-female.png");
                case MALE -> user.setPhoto("/assets/default-avatar.png");
                default -> user.setPhoto("/assets/default-avatar-other.png");
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
     */
    public LoginResponse loginAndCreateToken(String email, String rawPassword) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword)
            );

            User user = getUserOrThrow(email);

            String token = jwtService.generateToken(email);

            return new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getFirstName()
            );

        } catch (BadCredentialsException ex) {
            throw new RuntimeException("Neplatný e-mail nebo heslo");
        }
    }
}
