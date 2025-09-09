package com.project.collaboStudy.config;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Generate a 256-bit (32-byte) key for HS256 algorithm
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();

        // Encode the key as a Base64 string
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Generated Base64 Key: " + base64Key);
    }
}