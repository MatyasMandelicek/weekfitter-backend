package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000") // React běží na portu 3000
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public boolean loginUser(@RequestBody User user) {
        return userService.loginUser(user.getEmail(), user.getPassword());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    userService.generatePasswordResetToken(email);
    return ResponseEntity.ok("Pokud e-mail existuje, byl odeslán odkaz pro obnovení hesla.");
    }

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

}
