package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import com.weekfitter.weekfitter_backend.model.Gender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Nastavení výchozího avataru podle pohlaví
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

    public boolean loginUser(String email, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return passwordEncoder.matches(rawPassword, optionalUser.get().getPassword());
        }
        return false;
    }

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
