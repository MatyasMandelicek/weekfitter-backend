package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import com.weekfitter.weekfitter_backend.model.Gender;
import com.weekfitter.weekfitter_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Controller zajišťující správu uživatelských účtů, autentizaci a profilové informace.
 *
 * Poskytuje REST API pro:
 * - registraci a přihlášení,
 * - obnovu zapomenutého hesla,
 * - načítání a úpravu profilu,
 * - (v jiné třídě) nahrávání a načítání profilových fotek.
 *
 * Controller pracuje s UserService, který obsahuje logiku pro práci s hesly,
 * validaci a generování tokenů pro obnovu hesla.
 */
@RestController
@RequestMapping("/api/users")
 @CrossOrigin(origins = {"http://localhost:3000", "https://weekfitter.vercel.app"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    // ==========================================================
    // =============== AUTENTIZACE A OBNOVA HESLA ===============
    // ==========================================================

    /**
     * Registrace nového uživatele.
     *
     * @param user objekt obsahující registrační údaje
     * @return nově vytvořený uživatel
     */
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    /**
     * Přihlášení uživatele podle e-mailu a hesla.
     *
     * POZNÁMKA:
     * - vrací pouze boolean, což je funkční, ale ne bezpečné pro produkční aplikace.
     * - doporučuje se používat JWT nebo session token.
     */
    @PostMapping("/login")
    public boolean loginUser(@RequestBody User user) {
        return userService.loginUser(user.getEmail(), user.getPassword());
    }

    /**
     * Zahájení procesu obnovy hesla.
     *
     * Backend:
     * - vygeneruje unikátní token,
     * - uloží ho do DB,
     * - odešle e-mail s odkazem na frontend.
     *
     * @param request obsahuje e-mail uživatele
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.generatePasswordResetToken(email);
        return ResponseEntity.ok("Pokud e-mail existuje, byl odeslán odkaz pro obnovení hesla.");
    }

    /**
     * Obnova hesla po kliknutí na odkaz v e-mailu.
     *
     * @param request obsahuje reset token + nové heslo
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

    // ==========================================================
    // ========================== PROFIL ========================
    // ==========================================================

    /**
     * Načte detail uživatelského profilu podle e-mailu.
     *
     * POZOR: Bez autentizace může kdokoliv načíst profil libovolného uživatele.
     * Vhodné řešení je kontrolovat identitu např. přes JWT.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam String email) {
        User user = userService.getUserOrThrow(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Aktualizuje uživatelský profil (kromě hesla).
     *
     * @param email e-mail uživatele, který se aktualizuje
     * @param data mapovaný JSON obsahující pole, která se mají změnit
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestParam String email, @RequestBody Map<String, String> data) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Aktualizace jednotlivých hodnot profilu
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
