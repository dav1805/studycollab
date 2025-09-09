// src/main/java/com/project/collaboStudy/controller/AuthController.java

package com.project.collaboStudy.controller;

import com.project.collaboStudy.dto.LoginRequest;
import com.project.collaboStudy.dto.PasswordResetRequest;
import com.project.collaboStudy.dto.RegisterRequest;
import com.project.collaboStudy.model.User;
import com.project.collaboStudy.service.AuthService;
import com.project.collaboStudy.service.EmailVerificationService; // New import
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService; // Inject new service

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Verification code sent to your email. Please verify your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {
        emailVerificationService.confirmRegistration(token); // Call the new service
        return ResponseEntity.ok("Account confirmed successfully!");
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestPasswordReset(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}