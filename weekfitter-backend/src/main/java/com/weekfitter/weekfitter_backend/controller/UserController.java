package com.weekfitter.controller;

import com.weekfitter.model.User;
import com.weekfitter.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // povolí komunikaci s Reactem (localhost:3000)
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // GET - získání všech uživatelů
    @GetMapping
    public List<User> getAll() {
        return service.findAll();
    }

    // POST - registrace nového uživatele
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return service.create(user);
    }
}
