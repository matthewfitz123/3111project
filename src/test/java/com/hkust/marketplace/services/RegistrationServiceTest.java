package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.1 buyer registration rules: unique username, required fields,
 * password strength/match and hashed credential storage
 */
class RegistrationServiceTest {

    private final DummyDataService store = DummyDataService.getInstance();

    @BeforeEach
    void setUp() {
        TestState.reset();
    }

    @Test
    void registersBuyerWithHashedPasswordAndOptionalResidence() {
        User user = RegistrationService.registerBuyer("NewBuyer", "New Buyer",
                "Password12", "Password12", "Hall 7");

        assertEquals(UserRole.BUYER, user.getRole());
        assertEquals("newbuyer", user.getUsername());
        assertEquals("New Buyer", user.getFullName());
        assertEquals("Hall 7", user.getResidenceHall());
        assertTrue(user.getPasswordHash().startsWith("pbkdf2$"));
        assertNotEquals("Password12", user.getPasswordHash());
        assertTrue(PasswordUtil.verify("Password12", user.getPasswordHash()));
        assertSame(user, store.findUser("NEWBUYER"));
        assertTrue(store.usernameExists("newbuyer"));
    }

    @Test
    void usernamesAreUniqueAcrossAllRoles() {
        for (String taken : new String[]{"admin", "alice", "charlie"}) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> RegistrationService.registerBuyer(taken.toUpperCase(), "Name",
                            "Password12", "Password12", null));
            assertTrue(ex.getMessage().contains("already taken"));
        }

        store.registerUser(new User("crossrole", "Cross Role",
                PasswordUtil.hash("Password12"), UserRole.SELLER));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RegistrationService.registerBuyer("CROSSROLE", "Name",
                        "Password12", "Password12", null));
        assertTrue(ex.getMessage().contains("already taken"));
    }

    @Test
    void rejectsBlankRequiredFields() {
        assertEquals("Username is required.",
                assertThrows(IllegalArgumentException.class,
                        () -> RegistrationService.registerBuyer("   ", "Name",
                                "Password12", "Password12", null)).getMessage());
        assertEquals("Full Name cannot be empty.",
                assertThrows(IllegalArgumentException.class,
                        () -> RegistrationService.registerBuyer("okname", " ",
                                "Password12", "Password12", null)).getMessage());
    }

    @Test
    void rejectsWeakPasswordAndMismatchedConfirmation() {
        IllegalArgumentException weak = assertThrows(IllegalArgumentException.class,
                () -> RegistrationService.registerBuyer("weakpwd", "Name",
                        "weak", "weak", null));
        assertTrue(weak.getMessage().contains("10"));

        IllegalArgumentException mismatch = assertThrows(IllegalArgumentException.class,
                () -> RegistrationService.registerBuyer("mismatch", "Name",
                        "Password12", "Password13", null));
        assertEquals("Passwords do not match.", mismatch.getMessage());
    }

    @Test
    void residenceIsOptional() {
        User noResidence = RegistrationService.registerBuyer("nohall", "No Hall",
                "Password12", "Password12", null);
        User blankResidence = RegistrationService.registerBuyer("blankhall", "Blank Hall",
                "Password12", "Password12", "  ");

        assertNull(noResidence.getResidenceHall());
        assertNull(blankResidence.getResidenceHall());
    }
}
