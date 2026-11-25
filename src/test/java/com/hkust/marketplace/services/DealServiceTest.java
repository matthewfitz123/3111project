package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DealServiceTest {

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
    void returningABorrowedItemPutsTheListingBackLive() {
        Deal deal = createAcceptedLendDeal();

        login("alice");
        DealService.returnEarly(deal.getId());

        assertEquals(DealStatus.RETURNED, deal.getStatus());
        assertNotNull(deal.getCompletedAt());
        assertEquals(ListingStatus.LIVE, store.findListing("L103").getStatus());
        assertTrue(hasNotification("diana", "Item Returned"));
        assertThrows(IllegalStateException.class,
                () -> DealService.returnEarly(deal.getId()));
    }

    @Test
    void completingASaleClosesTheDeal() {
        Deal deal = createAcceptedSellDeal();

        login("alice");
        DealService.markComplete(deal.getId());

        assertEquals(DealStatus.COMPLETED, deal.getStatus());
        assertNotNull(deal.getCompletedAt());
        assertEquals(ListingStatus.SOLD, store.findListing("L100").getStatus());
        assertTrue(hasNotification("charlie", "Deal Completed"));
    }

    @Test
    void actionHasToMatchTheDealType() {
        Deal lend = createAcceptedLendDeal();
        login("alice");
        assertThrows(IllegalStateException.class,
                () -> DealService.markComplete(lend.getId()));

        Deal sell = createAcceptedSellDeal();
        login("alice");
        assertThrows(IllegalStateException.class,
                () -> DealService.returnEarly(sell.getId()));
    }

    @Test
    void anotherBuyerCannotCloseSomeoneElsesDeal() {
        Deal deal = createAcceptedSellDeal();
        login("bob");

        assertThrows(SecurityException.class,
                () -> DealService.markComplete(deal.getId()));
    }

    private Deal createAcceptedSellDeal() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                150.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        return latestDealFor("L100");
    }

    private Deal createAcceptedLendDeal() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(2), null);
        login("diana");
        OfferService.acceptOffer(offer.getId());
        return latestDealFor("L103");
    }

    private Deal latestDealFor(String listingId) {
        return store.getAllDeals().stream()
                .filter(d -> listingId.equals(d.getListingId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private boolean hasNotification(String username, String title) {
        return store.getNotificationsFor(username).stream()
                .anyMatch(n -> title.equals(n.getTitle()));
    }
}
