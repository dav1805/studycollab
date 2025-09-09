// src/main/java/com/project/collaboStudy/repository/VerificationTokenRepository.java

package com.project.collaboStudy.repository;

import com.project.collaboStudy.model.TokenType;
import com.project.collaboStudy.model.User;
import com.project.collaboStudy.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    // Updated to be more specific
    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);

    // Added to prevent multiple tokens for the same user
    void deleteByUserIdAndTokenType(Long userId, TokenType tokenType);

    // Method to delete tokens by user for account deletion
    void deleteByUser(User user);
}