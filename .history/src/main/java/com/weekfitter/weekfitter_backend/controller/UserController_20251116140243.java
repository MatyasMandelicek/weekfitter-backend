package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import com.weekfitter.weekfitter_backend.dto.LoginResponse;
import com.weekfitter.weekfitter_backend.model.Gender;
import com.weekfitter.weekfitter_backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller pro správu uživatelských účtů, autentizaci
 * a práci s profilem. Ověřování se provádí pomocí JWT.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "https://weekfitter.vercel.app"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ============ AUTENTIZACE ============

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            LoginResponse response =
                    userService.loginAndCreateToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword());

            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body("Neplatný e-mail nebo heslo.");
        }
    }

    // ============ OBNOVA HESLA ============

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        userService.generatePasswordResetToken(email);
        return ResponseEntity.ok("Pokud e-mail existuje, byl odeslán odkaz pro obnovu hesla.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> req) {
        String token = req.get("token");
        String newPassword = req.get("newPassword");

        boolean ok = userService.resetPassword(token, newPassword);
        if (ok) return ResponseEntity.ok("Heslo bylo změněno.");
        return ResponseEntity.badRequest().body("Neplatný nebo expirovaný token.");
    }

    // ============ PROFIL ============

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(java.security.Principal principal) {
        String email = principal.getName();
        User user = userService.getUserOrThrow(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            java.security.Principal principal,
            @RequestBody Map<String, String> data
    ) {
        String email = principal.getName();
        User user = userService.getUserOrThrow(email);

        if (data.containsKey("firstName")) user.setFirstName(data.get("firstName"));
        if (data.containsKey("lastName")) user.setLastName(data.get("lastName"));

        if (data.containsKey("birthDate") && !data.get("birthDate").isBlank()) {
            user.setBirthDate(LocalDate.parse(data.get("birthDate")));
        }

        if (data.containsKey("gender") && !data.get("gender").isBlank()) {
            try {
                user.setGender(Gender.valueOf(data.get("gender")));
            } catch (IllegalArgumentException ignored) {}
        }

        if (data.containsKey("photo") && !data.get("photo").isBlank()) {
            user.setPhoto(data.get("photo"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
