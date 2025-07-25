package com.example.workmanager.controller;

import com.example.workmanager.dto.LoginRequest;
import com.example.workmanager.dto.RegisterRequest;
import com.example.workmanager.dto.UserResponse;
import com.example.workmanager.model.User;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.service.AuthService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.login(request);
            return ResponseEntity.ok(new UserResponse(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(new UserResponse(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

}