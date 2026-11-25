package com.hkust.marketplace.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashIsSaltedAndVerifiable() {
        String hash = PasswordUtil.hash("Password12");

        assertTrue(hash.startsWith("pbkdf2$"));
        assertNotEquals("Password12", hash);
        assertTrue(PasswordUtil.verify("Password12", hash));
    }

    @Test
    void samePasswordGetsDifferentHashes() {
        String first = PasswordUtil.hash("Password12");
        String second = PasswordUtil.hash("Password12");

        assertNotEquals(first, second);
        assertTrue(PasswordUtil.verify("Password12", first));
        assertTrue(PasswordUtil.verify("Password12", second));
    }

    @Test
    void wrongPasswordIsRejected() {
        String hash = PasswordUtil.hash("Password12");

        assertFalse(PasswordUtil.verify("password12", hash));
        assertFalse(PasswordUtil.verify("Password13", hash));
        assertFalse(PasswordUtil.verify("", hash));
    }

    @Test
    void malformedStoredValueNeverThrows() {
        assertFalse(PasswordUtil.verify("Password12", null));
        assertFalse(PasswordUtil.verify(null, PasswordUtil.hash("Password12")));
        assertFalse(PasswordUtil.verify("Password12", "plaintext"));
        assertFalse(PasswordUtil.verify("Password12", "pbkdf2$abc$salt$hash"));
        assertFalse(PasswordUtil.verify("Password12", "pbkdf2$1000$notbase64$notbase64"));
    }

    @Test
    void hashingNullIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
    }
}
