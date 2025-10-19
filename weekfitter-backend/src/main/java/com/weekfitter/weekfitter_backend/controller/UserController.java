package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
