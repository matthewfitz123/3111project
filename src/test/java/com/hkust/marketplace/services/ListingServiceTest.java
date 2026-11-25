package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListingServiceTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    private User login(String username) {
        User user = store.findUser(username);
        SessionManager.login(user);
        return user;
    }

    @Test
    void liveListingWithoutOffersCanBeModified() {
        Listing listing = store.findListing("L100");
        login("charlie");

        assertTrue(ListingService.canModify(listing));
        assertEquals(0, ListingService.pendingOfferCount(listing));
    }

    @Test
    void pendingOffersLockEditingAndDeleting() {
        User alice = login("alice");
        OfferService.submitOffer(store.findListing("L100"), alice, 120.0, null, null, null);
        login("charlie");
        Listing listing = store.findListing("L100");

        assertFalse(ListingService.canModify(listing));
        assertEquals(1, ListingService.pendingOfferCount(listing));
        assertThrows(IllegalStateException.class, () -> ListingService.editListing(
                "L100", "New title", "desc", "Textbooks", "Used", "Library", 120.0, null));
        assertThrows(IllegalStateException.class, () -> ListingService.deleteListing("L100"));
    }

    @Test
    void soldListingIsLocked() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 150.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        Listing listing = store.findListing("L100");

        assertFalse(ListingService.canModify(listing));
        assertThrows(IllegalStateException.class, () -> ListingService.deleteListing("L100"));
    }

    @Test
    void rejectedListingCanBeEditedBackIntoReview() {
        Listing listing = store.findListing("L100");
        listing.setStatus(ListingStatus.REJECTED);
        listing.setRejectionReason("old reason");
        login("charlie");

        ListingService.editListing("L100", "Better title", "new description",
                "Textbooks", "Like New", "Library", 170.0, null);

        assertEquals("Better title", listing.getTitle());
        assertEquals(ListingStatus.PENDING_REVIEW, listing.getStatus());
        assertEquals(null, listing.getRejectionReason());
    }

    @Test
    void anotherSellerCannotEditTheListing() {
        login("diana");

        assertThrows(SecurityException.class, () -> ListingService.editListing(
                "L100", "hijack", "desc", "Textbooks", "Used", "Library", 10.0, null));
        assertThrows(SecurityException.class, () -> ListingService.deleteListing("L100"));
    }

    @Test
    void deletingAnUnlockedListingRemovesIt() {
        login("charlie");

        ListingService.deleteListing("L101");

        assertEquals(null, store.findListing("L101"));
    }
}
