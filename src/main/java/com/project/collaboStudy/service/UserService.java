// src/main/java/com/project/collaboStudy/service/UserService.java

package com.project.collaboStudy.service;

import com.project.collaboStudy.dto.PasswordChangeRequest;
import com.project.collaboStudy.dto.ProfileUpdateRequest; // Import the new DTO
import com.project.collaboStudy.model.User;
import com.project.collaboStudy.repository.UserRepository;
import com.project.collaboStudy.repository.VerificationTokenRepository; // ADD THIS IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final VerificationTokenRepository verificationTokenRepository; // ADD THIS FIELD

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getCurrentUser() {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    @Transactional
    public User updateUserProfile(ProfileUpdateRequest updatedProfile) {
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        user.setCourse(updatedProfile.getCourse());
        user.setGoals(updatedProfile.getGoals());

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password does not match.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void uploadProfilePicture(MultipartFile file) {
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        try {
            String fileName = fileStorageService.storeFile(file);
            user.setProfilePictureUrl(fileName);
            userRepository.save(user);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    // The problem is in the deleteAccount method. It needs to manually remove child records.
    @Transactional
    public void deleteAccount() {
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        // Step 1: Manually delete any associated verification tokens first.
        verificationTokenRepository.deleteByUser(user);

        // Step 2: Now that the foreign key constraint is satisfied, delete the user.
        userRepository.delete(user);
    }

}