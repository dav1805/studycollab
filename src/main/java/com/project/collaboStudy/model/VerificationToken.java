package com.project.collaboStudy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private LocalDateTime expiryDate;


    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER) //
    // Added cascade
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
}