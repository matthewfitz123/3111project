package com.hkust.marketplace.utils;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.7 checks used by the buyer profile form
 * the form itself only shows success and failure alerts
 */
class BuyerProfileRulesTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void aStrongPasswordReplacesTheStoredHash() {
        User alice = store.findUser("alice");
        String previous = alice.getPasswordHash();

        assertEquals("OK", DummyDataService.checkPasswordStrength("NewPassword1"));
        alice.setFullName("Alice Chan");
        alice.setPasswordHash(PasswordUtil.hash("NewPassword1"));

        assertEquals("Alice Chan", store.findUser("alice").getFullName());
        assertTrue(PasswordUtil.verify("NewPassword1", alice.getPasswordHash()));
        assertNotEquals(previous, alice.getPasswordHash());
    }

    @Test
    void aWeakOrMismatchedPasswordIsRejectedByTheSharedRules() {
        assertNotEquals("OK", DummyDataService.checkPasswordStrength("short"));
        assertNotEquals("OK", DummyDataService.checkPasswordStrength("alllowercase1"));
        assertNotEquals("OK", DummyDataService.checkPasswordStrength("NOLOWERCASE1"));
        assertNotEquals("OK", DummyDataService.checkPasswordStrength("NoDigitsHere"));
        assertNotEquals("NewPassword1", "NewPassword2");
    }

    @Test
    void blankNameIsNotRejectedByTheUserModel() {
        User alice = store.findUser("alice");
        alice.setFullName("");
        assertEquals("", alice.getFullName());
    }
}
