package com.luiscampillo.hotelnova.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 password hashing. Uses MessageDigest from the JDK so no extra
 * dependencies are needed.
 *
 * Note: SHA-256 is fast and unsalted, which is fine for an academic project
 * but unsuitable for production. Real systems should use BCrypt or Argon2.
 */
public final class PasswordEncoder {

    private PasswordEncoder() { }

    /** Returns the hex-encoded SHA-256 hash of the given plaintext. */
    public static String hash(String plain) {
        if (plain == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /** Verifies a plaintext password against a stored hash. */
    public static boolean matches(String plain, String storedHash) {
        return storedHash != null && storedHash.equals(hash(plain));
    }
}
