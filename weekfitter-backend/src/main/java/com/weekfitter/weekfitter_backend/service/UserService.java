package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserService userRepository;

    public UserService(UserService userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private List<User> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    public User createUser(User user) {
        user.setCreatedAt(OffsetDateTime.now());
        user.setCreatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    private User save(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
}
