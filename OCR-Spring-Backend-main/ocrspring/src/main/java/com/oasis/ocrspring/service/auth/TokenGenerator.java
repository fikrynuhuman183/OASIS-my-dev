package com.oasis.ocrspring.service.auth;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

public class TokenGenerator {
    public static String generateRandomToken(int byteLength) {
        byte[] tokenBytes = new byte[byteLength];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(tokenBytes);
        return Hex.encodeHexString(tokenBytes);
    }

    public static void main(String[] args) {
        // Generate a 256-byte random token
        String randomToken = generateRandomToken(256);
    }
}
