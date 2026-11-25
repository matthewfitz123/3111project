package com.hkust.marketplace.utils;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionManagerTest {

    @BeforeEach
    void reset() {
        TestState.reset();
    }

    @Test
    void missingSessionIsRejected() {
        assertNull(SessionManager.getCurrentUsername());
        assertFalse(SessionManager.isLoggedIn());
        assertThrows(SecurityException.class, () -> SessionManager.requireRole(UserRole.BUYER));
    }

    @Test
    void buyerCanUseBuyerGuard() {
        User alice = DummyDataService.getInstance().findUser("alice");
        SessionManager.login(alice);

        assertEquals("alice", SessionManager.getCurrentUsername());
        assertTrue(SessionManager.isBuyer());
        assertFalse(SessionManager.isSeller());
        assertSame(alice, SessionManager.requireRole(UserRole.BUYER));
    }

    @Test
    void wrongRoleIsRejected() {
        SessionManager.login(DummyDataService.getInstance().findUser("alice"));

        assertThrows(SecurityException.class, () -> SessionManager.requireRole(UserRole.SELLER));
        assertThrows(SecurityException.class, () -> SessionManager.requireRole(UserRole.ADMIN));
    }

    @Test
    void logoutClearsTheUser() {
        SessionManager.login(DummyDataService.getInstance().findUser("charlie"));
        SessionManager.logout();

        assertNull(SessionManager.getCurrentUser());
        assertFalse(SessionManager.isLoggedIn());
    }
}
