// src/main/java/com/project/collaboStudy/service/AuthService.java

package com.project.collaboStudy.service;

import com.project.collaboStudy.dto.LoginRequest;
import com.project.collaboStudy.dto.PasswordResetRequest;
import com.project.collaboStudy.dto.RegisterRequest;
import com.project.collaboStudy.model.Role;
import com.project.collaboStudy.model.TokenType;
import com.project.collaboStudy.model.User;
import com.project.collaboStudy.model.UserRole;
import com.project.collaboStudy.model.VerificationToken;
import com.project.collaboStudy.repository.RoleRepository;
import com.project.collaboStudy.repository.UserRepository;
import com.project.collaboStudy.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Role assignedRole;
        if ("admin@gmail.com".equalsIgnoreCase(request.getEmail())) {
            assignedRole = roleRepository.findByName(UserRole.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalArgumentException("Admin role not found."));
        } else {
            assignedRole = roleRepository.findByName(UserRole.ROLE_MEMBER)
                    .orElseThrow(() -> new IllegalArgumentException("Member role not found."));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(assignedRole))
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationToken.setTokenType(TokenType.REGISTRATION);
        tokenRepository.save(verificationToken);
        String confirmationUrl = "http://localhost:8080/api/auth/confirm?token=" + token;
        emailService.sendEmail(savedUser.getEmail(), "Account Confirmation", "Please click the link to confirm your account: " + confirmationUrl);

        return savedUser;
    }

    public String login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(request.getUsername());
        }

        User user = userOptional.orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Account not enabled. Please check your email for a confirmation link.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        return jwtService.generateToken(user.getUsername());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with this email not found."));

        tokenRepository.deleteByUserIdAndTokenType(user.getId(), TokenType.PASSWORD_RESET);

        String token = UUID.randomUUID().toString();
        VerificationToken passwordResetToken = new VerificationToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordResetToken.setTokenType(TokenType.PASSWORD_RESET);
        tokenRepository.save(passwordResetToken);

        String resetUrl = "http://localhost:3000/auth/reset-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Password Reset Request", "Please use the following link to reset your password: " + resetUrl);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        VerificationToken token = tokenRepository.findByTokenAndTokenType(request.getToken(), TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenRepository.delete(token);
    }
}