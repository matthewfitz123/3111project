package com.hkust.marketplace.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordRulesTest {

    @Test
    void acceptsAValidPassword() {
        assertEquals("OK", DummyDataService.checkPasswordStrength("Password12"));
        assertEquals("OK", DummyDataService.checkPasswordStrength("Abcdefghi1"));
    }

    @Test
    void rejectsShortPasswords() {
        String message = DummyDataService.checkPasswordStrength("Ab1");
        assertNotNull(message);
        assertTrue(message.contains("10"));
    }

    @Test
    void rejectsMissingCharacterTypes() {
        assertNotNull(DummyDataService.checkPasswordStrength("lowercase123"));
        assertNotNull(DummyDataService.checkPasswordStrength("UPPERCASE123"));
        assertNotNull(DummyDataService.checkPasswordStrength("NoDigitsHere"));
    }

    @Test
    void enforcesTheMaximumLength() {
        String valid = "Aa1" + "x".repeat(61);
        String tooLong = "Aa1" + "x".repeat(62);

        assertEquals("OK", DummyDataService.checkPasswordStrength(valid));
        assertNotNull(DummyDataService.checkPasswordStrength(tooLong));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertNotNull(DummyDataService.checkPasswordStrength(null));
        assertNotNull(DummyDataService.checkPasswordStrength(""));
    }
}
