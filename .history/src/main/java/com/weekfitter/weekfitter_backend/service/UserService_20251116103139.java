package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;


    /** BCrypt encoder pro hashování hesel. */
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

        // BCrypt hashování hesla
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Automatické nastavení výchozí profilové fotky
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
     *
     * Porovnává se hash uložený v databázi s heslem zadaným uživatelem.
     * BCryptPasswordEncoder automaticky používá náhodný salt.
     *
     * @return true = přihlášení úspěšné, false = chybné heslo nebo uživatel neexistuje
     */
    public boolean loginUser(String email, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return passwordEncoder.matches(rawPassword, optionalUser.get().getPassword());
        }
        return false;
    }

    /**
     * Generuje token pro reset hesla a odešle e-mail s odkazem na stránku
     * pro nastavení nového hesla.
     *
     * Postup:
     * - najde uživatele podle e-mailu,
     * - vygeneruje náhodné UUID jako token,
     * - nastaví expiraci tokenu (1 hodina),
     * - uloží do databáze,
     * - pošle e-mail pomocí EmailService.
     *
     * Poznámka:
     * Pokud e-mail neexistuje, metoda jen tiše skončí (bezpečné chování).
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
     * Ověří platnost reset tokenu a změní uživateli heslo na nové.
     *
     * Podmínky úspěchu:
     * - token existuje v databázi,
     * - aktuální čas je před expirační dobou tokenu.
     *
     * Pokud je token platný:
     * - nastaví se nové heslo,
     * - token i jeho expirace se smažou (jednorázové použití),
     * - uživatel se uloží do databáze.
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

    public User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Uživatel s e-mailem " + email + " neexistuje"));
    }

}
