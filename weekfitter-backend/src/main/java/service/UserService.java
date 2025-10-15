package com.weekfitter.service;

import com.weekfitter.model.User;
import com.weekfitter.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() { return repo.findAll(); }

    public User create(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setCreatedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
        return repo.save(u);
    }

    public User get(UUID id) {
        return repo.findById(id).orElseThrow();
    }
}
