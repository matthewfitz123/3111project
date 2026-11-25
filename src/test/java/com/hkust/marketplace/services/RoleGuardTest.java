package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * a buyer session cannot run seller or admin actions
 */
class RoleGuardTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
        SessionManager.login(store.findUser("alice"));
    }

    @Test
    void buyerCannotAcceptOffersEditListingsOrHideReviews() {
        assertThrows(SecurityException.class, () -> OfferService.acceptOffer("missing"));
        assertThrows(SecurityException.class, () -> ListingService.deleteListing("L100"));
        assertThrows(SecurityException.class, () -> ListingService.approveListing(store.findListing("L104")));
        assertThrows(SecurityException.class, () -> ReviewService.setHidden("missing", true));
        assertThrows(SecurityException.class,
                () -> StatsService.computeForSeller(store.findUser("charlie")));
    }

    @Test
    void sellerCannotApproveAListing() {
        User charlie = store.findUser("charlie");
        SessionManager.login(charlie);
        assertThrows(SecurityException.class,
                () -> ListingService.approveListing(store.findListing("L104")));
    }
}
