package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 3.3 deactivate is a soft delete: the account cannot log in,
 * and listings stay in the store
 */
class AdminUserTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void deactivatingASellerBlocksLoginAndKeepsListings() {
        Listing listing = store.findListing("L100");
        store.deactivateUser("charlie");
        store.logAudit("admin", "USER_DELETED", "@charlie (soft delete; listings/deals/chat retained)");

        assertFalse(store.findUser("charlie").isActive());
        assertNotNull(store.findListing(listing.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> AuthService.loginSeller("charlie", "Password12"));
        assertTrue(store.getAuditLog().stream().anyMatch(line -> line.contains("USER_DELETED")));
    }
}
