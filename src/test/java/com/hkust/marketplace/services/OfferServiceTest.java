package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfferServiceTest {

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
    void sellOfferIsCreatedAsPending() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                150.0, null, null, "can pick up tonight");

        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertEquals("alice", offer.getBuyerUsername());
        assertEquals("charlie", offer.getSellerUsername());
        assertEquals(150.0, offer.getOfferPrice());
        assertEquals("can pick up tonight", offer.getMessage());
        assertNotNull(offer.getId());
        assertTrue(store.getPendingOffersForSeller("charlie").stream()
                .anyMatch(o -> o.getId().equals(offer.getId())));
        assertTrue(hasNotification("charlie", "New Offer"));
    }

    @Test
    void invalidSellPricesAreRejected() {
        User alice = login("alice");
        Listing listing = store.findListing("L100");

        assertThrows(IllegalArgumentException.class,
                () -> OfferService.submitOffer(listing, alice, 0.0, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> OfferService.submitOffer(listing, alice, -5.0, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> OfferService.submitOffer(listing, alice, null, null, null, null));
    }

    @Test
    void nonLiveAndGiveawayListingsCannotBeOfferedOn() {
        User alice = login("alice");

        assertThrows(IllegalStateException.class,
                () -> OfferService.submitOffer(store.findListing("L104"), alice,
                        100.0, null, null, null));
        assertThrows(IllegalStateException.class,
                () -> OfferService.submitOffer(store.findListing("L102"), alice,
                        100.0, null, null, null));
    }

    @Test
    void borrowRequestValidatesItsDates() {
        User alice = login("alice");
        Listing listing = store.findListing("L103");

        Offer offer = OfferService.submitOffer(listing, alice, null,
                LocalDate.now(), LocalDate.now().plusDays(2), null);
        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertNotNull(offer.getBorrowStart());
        assertNotNull(offer.getBorrowEnd());

        assertThrows(IllegalArgumentException.class,
                () -> OfferService.submitOffer(listing, alice, null,
                        LocalDate.now(), LocalDate.now().minusDays(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> OfferService.submitOffer(listing, alice, null, null, null, null));
    }

    @Test
    void giveawayClaimCreatesAnActiveDealAndLocksTheListing() {
        User alice = login("alice");
        Listing listing = store.findListing("L102");

        Offer claim = OfferService.claimGiveaway(listing, alice);

        assertEquals(OfferStatus.ACCEPTED, claim.getStatus());
        assertEquals(ListingStatus.GIVEN, listing.getStatus());
        List<Deal> deals = store.getAllDeals();
        Deal deal = deals.get(deals.size() - 1);
        assertEquals("L102", deal.getListingId());
        assertEquals(DealStatus.ACTIVE, deal.getStatus());
        assertTrue(hasNotification("diana", "New Offer"));
        assertThrows(IllegalStateException.class,
                () -> OfferService.claimGiveaway(listing, alice));
    }

    @Test
    void concurrentClaimsOnlyLetOneBuyerWin() throws Exception {
        User alice = login("alice");
        Listing listing = store.findListing("L102");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> first = pool.submit(() -> tryClaim(listing, alice, start));
            Future<Boolean> second = pool.submit(() -> tryClaim(listing, alice, start));
            start.countDown();
            int successes = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);

            assertEquals(1, successes);
            assertEquals(ListingStatus.GIVEN, listing.getStatus());
            assertEquals(1, store.getAllDeals().stream()
                    .filter(d -> d.getListingId().equals("L102")).count());
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean tryClaim(Listing listing, User buyer, CountDownLatch start) {
        try {
            start.await(2, TimeUnit.SECONDS);
            OfferService.claimGiveaway(listing, buyer);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Test
    void sellerCanAcceptAndDeclineOffers() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                150.0, null, null, null);
        login("charlie");

        OfferService.acceptOffer(offer.getId());

        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        assertEquals(ListingStatus.SOLD, store.findListing("L100").getStatus());
        assertTrue(hasNotification("alice", "Offer Accepted"));

        // a fresh offer on another listing can be declined without locking it
        login("alice");
        Offer decline = OfferService.submitOffer(store.findListing("L101"), alice,
                200.0, null, null, null);
        login("charlie");
        OfferService.declineOffer(decline.getId());
        assertEquals(OfferStatus.DECLINED, decline.getStatus());
        assertEquals(ListingStatus.LIVE, store.findListing("L101").getStatus());
    }

    @Test
    void counterOfferCanBeAcceptedByTheBuyer() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L101"), alice,
                200.0, null, null, null);
        login("charlie");
        OfferService.counterOffer(offer.getId(), 180.0, null);
        assertEquals(OfferStatus.COUNTERED, offer.getStatus());
        assertEquals(180.0, offer.getCounterPrice());

        login("alice");
        OfferService.acceptCounter(offer.getId());

        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        assertEquals(ListingStatus.SOLD, store.findListing("L101").getStatus());
        assertTrue(store.getAllDeals().stream()
                .anyMatch(d -> d.getListingId().equals("L101") && d.getStatus() == DealStatus.ACTIVE));
    }

    @Test
    void onlyABuyerCanSubmitAnOffer() {
        assertThrows(SecurityException.class,
                () -> OfferService.submitOffer(store.findListing("L100"), null,
                        100.0, null, null, null));

        User charlie = login("charlie");
        assertThrows(SecurityException.class,
                () -> OfferService.submitOffer(store.findListing("L100"), charlie,
                        100.0, null, null, null));
        assertThrows(SecurityException.class,
                () -> OfferService.claimGiveaway(store.findListing("L102"), charlie));
    }

    private boolean hasNotification(String username, String title) {
        return store.getNotificationsFor(username).stream()
                .anyMatch(n -> title.equals(n.getTitle()));
    }
}
