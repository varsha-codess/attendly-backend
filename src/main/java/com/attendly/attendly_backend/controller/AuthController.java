package com.attendly.attendly_backend.controller;

import com.attendly.attendly_backend.entity.User;
import com.attendly.attendly_backend.repository.UserRepository;
import com.attendly.attendly_backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.get("role"); // "TEACHER" or "STUDENT"
        String fullName = body.get("fullName");

        if (userRepository.findByUsername(username).isPresent()) {
            return Map.of("error", "Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // hashed, not plaintext
        user.setRole(role);
        user.setFullName(fullName);
        userRepository.save(user);

        return Map.of("message", "Registered successfully");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return Map.of("error", "Invalid username or password");
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return Map.of(
                "token", token,
                "role", user.getRole(),
                "username", user.getUsername()
        );
    }
}