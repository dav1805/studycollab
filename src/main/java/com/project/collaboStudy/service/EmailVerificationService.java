// src/main/java/com/project/collaboStudy/service/EmailVerificationService.java

package com.project.collaboStudy.service;

import com.project.collaboStudy.model.TokenType;
import com.project.collaboStudy.model.User;
import com.project.collaboStudy.model.VerificationToken;
import com.project.collaboStudy.repository.UserRepository;
import com.project.collaboStudy.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public void confirmRegistration(String token) {
        // Correct logic: find the token by its string and its type (REGISTRATION)
        VerificationToken verificationToken = tokenRepository.findByTokenAndTokenType(token, TokenType.REGISTRATION)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Delete the token after successful verification to prevent reuse
        tokenRepository.delete(verificationToken);
    }
}