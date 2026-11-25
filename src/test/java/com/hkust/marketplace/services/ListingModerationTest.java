package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 3.2 review and task 3.7 takedown
 */
class ListingModerationTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
        SessionManager.login(store.findUser("admin"));
    }

    @Test
    void pendingListingCanBeApprovedOrRejectedWithAReason() {
        Listing pending = store.findListing("L104");
        assertEquals(ListingStatus.PENDING_REVIEW, pending.getStatus());

        ListingService.approveListing(pending);
        assertEquals(ListingStatus.LIVE, pending.getStatus());
        assertTrue(store.getNotificationsFor("diana").stream()
                .anyMatch(n -> "Listing Approved".equals(n.getTitle())));

        ListingService.rejectListing(pending, "photos are unclear");
        assertEquals(ListingStatus.REJECTED, pending.getStatus());
        assertEquals("photos are unclear", pending.getRejectionReason());
        assertThrows(IllegalArgumentException.class, () -> ListingService.rejectListing(pending, "  "));
    }

    @Test
    void takedownNotifiesTheBuyerWhoHadAPendingOffer() {
        User alice = store.findUser("alice");
        SessionManager.login(alice);
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 50.0, null, null, null);
        SessionManager.login(store.findUser("admin"));

        ListingService.takeDownListing(store.findListing("L100"), "against the rules");

        assertEquals(ListingStatus.REMOVED, store.findListing("L100").getStatus());
        assertTrue(store.getNotificationsFor("charlie").stream()
                .anyMatch(n -> "Listing Removed".equals(n.getTitle())));
        assertTrue(store.getNotificationsFor(offer.getBuyerUsername()).stream()
                .anyMatch(n -> "Listing Removed".equals(n.getTitle())));
    }
}
