package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 2.2 seller login and task 3.1 seeded admin login
 */
class PortalLoginTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void storedPasswordsAreHashes() {
        for (User user : store.getAllUsers()) {
            assertTrue(user.getPasswordHash().startsWith("pbkdf2$"));
            assertFalse(user.getPasswordHash().equals("Password12"));
            assertFalse(user.getPasswordHash().equals("Admin12345"));
        }
        assertTrue(PasswordUtil.verify("Password12", store.findUser("charlie").getPasswordHash()));
        assertTrue(PasswordUtil.verify("Admin12345", store.findUser("admin").getPasswordHash()));
    }

    @Test
    void sellerLoginRejectsBuyersAndOpensASellerSession() {
        assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginSeller("alice", "Password12"));
        User charlie = AuthService.loginSeller("charlie", "Password12");
        assertEquals(UserRole.SELLER, charlie.getRole());
        assertEquals("charlie", SessionManager.getCurrentUsername());
    }

    @Test
    void adminLoginIsSeededAndRoleChecked() {
        assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginAdmin("alice", "Password12"));
        User admin = AuthService.loginAdmin("admin", "Admin12345");
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertEquals("admin", SessionManager.getCurrentUsername());

        store.deactivateUser("admin");
        SessionManager.logout();
        assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginAdmin("admin", "Admin12345"));
    }
}
