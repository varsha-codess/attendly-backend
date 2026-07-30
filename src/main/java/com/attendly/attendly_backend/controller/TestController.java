package com.attendly.attendly_backend.controller;

import com.attendly.attendly_backend.entity.User;
import com.attendly.attendly_backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Attendly backend is alive!";
    }

    @PostMapping("/add-user")
    public User addTestUser() {
        User user = new User();
        user.setUsername("testuser1");
        user.setPassword("test123"); // plaintext just for this test, we'll hash later
        user.setRole("STUDENT");
        user.setFullName("Test Student");
        return userRepository.save(user);
    }

    @GetMapping("/all-users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}