package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import com.weekfitter.weekfitter_backend.model.Gender;
import com.weekfitter.weekfitter_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Controller zajišťující správu uživatelských účtů, autentizaci a profilové informace.
 *
 * Poskytuje REST rozhraní pro:
 * - registraci a přihlášení uživatele,
 * - obnovu zapomenutého hesla,
 * - načítání a úpravu profilu,
 * - nahrávání a zobrazování profilových fotografií.
 */
@RestController
@RequestMapping("/api/users")
 @CrossOrigin(origins = {"http://localhost:3000", "https://weekfitter.vercel.app"}) // React frontend
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // AUTENTIZACE

    /**
     * Registrace nového uživatele.
     */
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    /**
     * Přihlášení uživatele podle e-mailu a hesla.
     */
    @PostMapping("/login")
    public boolean loginUser(@RequestBody User user) {
        return userService.loginUser(user.getEmail(), user.getPassword());
    }

    /**
     * Zahájení procesu obnovy hesla – vygeneruje reset token a odešle e-mail s odkazem.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.generatePasswordResetToken(email);
        return ResponseEntity.ok("Pokud e-mail existuje, byl odeslán odkaz pro obnovení hesla.");
    }

    /**
     * Změna hesla po kliknutí na odkaz z e-mailu.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok("Heslo bylo úspěšně změněno.");
        } else {
            return ResponseEntity.badRequest().body("Neplatný nebo expirovaný token.");
        }
    }

    // PROFIL

    /**
     * Načte detail uživatelského profilu podle e-mailu.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Aktualizuje profil uživatele (kromě hesla).
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestParam String email, @RequestBody Map<String, String> data) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        if (data.containsKey("firstName")) user.setFirstName(data.get("firstName"));
        if (data.containsKey("lastName")) user.setLastName(data.get("lastName"));
        if (data.containsKey("birthDate") && data.get("birthDate") != null && !data.get("birthDate").isEmpty()) {
            user.setBirthDate(LocalDate.parse(data.get("birthDate")));
        }

        if (data.containsKey("gender") && data.get("gender") != null && !data.get("gender").isEmpty()) {
            try {
                user.setGender(Gender.valueOf(data.get("gender")));
            } catch (IllegalArgumentException e) {
                System.out.println("Neznámá hodnota pohlaví: " + data.get("gender"));
            }
        }

        if (data.containsKey("photo") && data.get("photo") != null && !data.get("photo").isEmpty()) {
            user.setPhoto(data.get("photo"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
