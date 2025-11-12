package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import com.weekfitter.weekfitter_backend.model.Gender;
import com.weekfitter.weekfitter_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    private final Path uploadDir = Paths.get("uploads/user_photos");

    public UserController() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Nelze vytvořit složku pro fotky uživatelů.", e);
        }
    }

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

        // 🟢 DOPLNĚNÁ ČÁST – uloží fotku, pokud přijde z frontendu
        if (data.containsKey("photo") && data.get("photo") != null && !data.get("photo").isEmpty()) {
            user.setPhoto(data.get("photo"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }


    /**
     * Umožňuje nahrání nebo změnu profilové fotografie uživatele.
     */
    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserPhoto(@RequestParam String email, @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Soubor je prázdný.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        try {
            String originalName = file.getOriginalFilename();
            String original = StringUtils.cleanPath(originalName == null ? "" : originalName);

            String extension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }

            String filename = UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            user.setPhoto("/api/users/photo/" + filename);
            userRepository.save(user);

            return ResponseEntity.ok(user);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Chyba při ukládání souboru.");
        }
    }

    /**
     * Vrací uloženou profilovou fotografii uživatele podle názvu souboru.
     */
    @GetMapping("/photo/{filename:.+}")
    public ResponseEntity<byte[]> getUserPhoto(@PathVariable String filename) {
        try {
            Path path = uploadDir.resolve(filename);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = Files.readAllBytes(path);
            String mime = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(mime != null ? mime : "image/jpeg"))
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
