package com.project.collaboStudy.controller;

import com.project.collaboStudy.dto.ProfileUpdateRequest;
import com.project.collaboStudy.model.User;
import com.project.collaboStudy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @PutMapping("/setup")
    public ResponseEntity<User> setupProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        // Correctly pass the 'request' DTO to the service method
        User user = userService.updateUserProfile(request);
        return ResponseEntity.ok(user);
    }
}