package com.luiscampillo.hotelnova.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void hashIsDeterministic() {
        assertEquals(PasswordEncoder.hash("admin123"), PasswordEncoder.hash("admin123"));
    }

    @Test
    void differentInputsProduceDifferentHashes() {
        assertNotEquals(PasswordEncoder.hash("admin123"), PasswordEncoder.hash("admin124"));
    }

    @Test
    void hashMatchesKnownValueForAdmin123() {
        // The seed in schema_postgres.sql uses this exact hash for the
        // default 'admin' user. If this test breaks, login will fail too.
        assertEquals(
                "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9",
                PasswordEncoder.hash("admin123"));
    }

    @Test
    void matchesReturnsTrueForCorrectPassword() {
        String hash = PasswordEncoder.hash("secret");
        assertTrue(PasswordEncoder.matches("secret", hash));
    }

    @Test
    void matchesReturnsFalseForWrongPassword() {
        String hash = PasswordEncoder.hash("secret");
        assertFalse(PasswordEncoder.matches("wrong", hash));
    }

    @Test
    void hashRejectsNullInput() {
        assertThrows(IllegalArgumentException.class, () -> PasswordEncoder.hash(null));
    }
}
