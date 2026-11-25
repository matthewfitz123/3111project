package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.7 My Deals rules that live in DealService and BorrowScheduler
 */
class BuyerMyDealsTest {

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
    void currentAndPastDealsStayOnTheBuyerList() {
        Deal active = acceptSell();
        login("alice");
        DealService.markComplete(active.getId());

        assertTrue(store.getDealsForBuyer("alice").stream()
                .anyMatch(d -> d.getId().equals(active.getId())
                        && d.getStatus() == DealStatus.COMPLETED));
        assertEquals(store.findListing(active.getListingId()).getTitle(), active.getListingTitle());
        assertNotNull(store.getOrCreateThread(active.getListingId(), "alice").getId());
    }

    @Test
    void earlyReturnShowsADueDateAndPutsTheListingLive() {
        Deal deal = acceptLend();
        assertNotNull(deal.getDueAt());
        assertTrue(deal.getDueAt().isAfter(LocalDateTime.now().minusMinutes(1)));

        login("alice");
        DealService.returnEarly(deal.getId());

        assertEquals(DealStatus.RETURNED, deal.getStatus());
        assertEquals(ListingStatus.LIVE, store.findListing("L103").getStatus());
    }

    @Test
    void autoReturnExpiresTheBorrowAndFreesTheListing() {
        Deal deal = acceptLend();
        deal.setDueAt(LocalDateTime.now().minusSeconds(5));

        BorrowScheduler.checkExpired(LocalDateTime.now());

        assertEquals(DealStatus.EXPIRED, deal.getStatus());
        assertEquals(ListingStatus.LIVE, store.findListing("L103").getStatus());
    }

    @Test
    void completingAPurchaseLeavesItUnratedForTheReviewPrompt() {
        Deal deal = acceptSell();
        login("alice");
        DealService.markComplete(deal.getId());

        assertEquals(DealStatus.COMPLETED, deal.getStatus());
        assertFalse(deal.isRated());
        assertEquals(ListingStatus.SOLD, store.findListing("L100").getStatus());
    }

    @Test
    void aPendingOfferIsNotYetADeal() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L101"), alice,
                40.0, null, null, null);

        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertTrue(store.getDealsForBuyer("alice").stream()
                .noneMatch(d -> "L101".equals(d.getListingId())));
    }

    private Deal acceptSell() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                150.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        return latest("L100");
    }

    private Deal acceptLend() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(2), null);
        login("diana");
        OfferService.acceptOffer(offer.getId());
        return latest("L103");
    }

    private Deal latest(String listingId) {
        return store.getAllDeals().stream()
                .filter(d -> listingId.equals(d.getListingId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
