package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
}

