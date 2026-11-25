package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.2 buyer login rules: required fields, known user, buyer role only,
 * password verification, active account and session creation
 */
class AuthServiceTest {

    private final DummyDataService store = DummyDataService.getInstance();

    @BeforeEach
    void setUp() {
        TestState.reset();
    }

    @Test
    void buyerLoginSetsSessionAndReturnsUser() {
        User user = AuthService.loginBuyer("alice", "Password12");

        assertEquals(UserRole.BUYER, user.getRole());
        assertEquals("alice", user.getUsername());
        assertSame(user, SessionManager.getCurrentUser());
        assertTrue(SessionManager.isBuyer());
        assertEquals("alice", SessionManager.getCurrentUsername());
    }

    @Test
    void usernameMatchIsCaseInsensitive() {
        User user = AuthService.loginBuyer(" ALICE ", "Password12");

        assertEquals("alice", user.getUsername());
        assertSame(user, SessionManager.getCurrentUser());
    }

    @Test
    void newlyRegisteredBuyerCanLogin() {
        RegistrationService.registerBuyer("newbuyer", "New Buyer",
                "Password12", "Password12", "Hall 7");

        User user = AuthService.loginBuyer("NEWBUYER", "Password12");

        assertEquals("newbuyer", user.getUsername());
        assertTrue(SessionManager.isBuyer());
    }

    @Test
    void rejectsMissingCredentials() {
        assertEquals("Please enter both username and password.",
                assertThrows(IllegalArgumentException.class,
                        () -> AuthService.loginBuyer("", "Password12")).getMessage());
        assertEquals("Please enter both username and password.",
                assertThrows(IllegalArgumentException.class,
                        () -> AuthService.loginBuyer("alice", "")).getMessage());
        assertEquals("Please enter both username and password.",
                assertThrows(IllegalArgumentException.class,
                        () -> AuthService.loginBuyer(null, "Password12")).getMessage());
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void rejectsUnknownUser() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginBuyer("ghost", "Password12"));

        assertEquals("User not found. Please check your username.", ex.getMessage());
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void rejectsNonBuyerAccounts() {
        String[][] credentials = {
                {"charlie", "Password12"},
                {"admin", "Admin12345"}
        };
        for (String[] credential : credentials) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> AuthService.loginBuyer(credential[0], credential[1]));
            assertEquals("This account is not a Buyer account. Please use the correct portal.",
                    ex.getMessage());
            assertNull(SessionManager.getCurrentUser());
        }
    }

    @Test
    void rejectsWrongPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginBuyer("alice", "WrongPass12"));

        assertEquals("Incorrect password. Please try again.", ex.getMessage());
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void rejectsDeactivatedBuyer() {
        store.deactivateUser("alice");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginBuyer("alice", "Password12"));

        assertEquals("This account has been deactivated. Contact admin.", ex.getMessage());
        assertNull(SessionManager.getCurrentUser());
    }
}
