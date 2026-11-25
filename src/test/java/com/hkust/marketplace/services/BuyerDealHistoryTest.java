package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.9 buyer history labels, filters and the update after a deal is completed
 */
class BuyerDealHistoryTest {

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
    void completingADealAddsItWithBuyerLabels() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                150.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getDealsForBuyer("alice").get(store.getDealsForBuyer("alice").size() - 1);

        assertFalse(DealHistoryService.buyerHistory("alice").contains(deal));

        login("alice");
        DealService.markComplete(deal.getId());

        List<Deal> history = DealHistoryService.buyerHistory("alice");
        assertTrue(history.contains(deal));
        assertEquals(deal.getListingTitle(), history.get(0).getListingTitle());
        assertTrue(DealHistoryService.sellerDisplay(deal).contains("charlie"));
        assertEquals("Bought", DealHistoryService.typeLabel(deal));
        assertEquals("Completed", DealHistoryService.statusLabel(deal));
        assertEquals(LocalDate.now(), DealHistoryService.historyDate(deal));
    }

    @Test
    void filtersMatchDateCategoryAndSeller() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                90.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getAllDeals().stream()
                .filter(d -> "L100".equals(d.getListingId()))
                .reduce((a, b) -> b)
                .orElseThrow();
        login("alice");
        DealService.markComplete(deal.getId());

        assertTrue(DealHistoryService.matches(deal, LocalDate.now(), LocalDate.now(),
                "Textbooks", "Bought", "Completed", "charlie", false));
        assertFalse(DealHistoryService.matches(deal, LocalDate.now().plusDays(1), null,
                null, null, null, null, false));
        assertFalse(DealHistoryService.matches(deal, null, null,
                "Electronics", null, null, null, false));
        assertFalse(DealHistoryService.matches(deal, null, null,
                null, null, null, "diana", false));
    }

    @Test
    void anExpiredBorrowIsNotBuyerHistory() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(1), null);
        login("diana");
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getAllDeals().stream()
                .filter(d -> "L103".equals(d.getListingId()))
                .reduce((a, b) -> b)
                .orElseThrow();
        deal.setDueAt(java.time.LocalDateTime.now().minusMinutes(1));
        BorrowScheduler.checkExpired(java.time.LocalDateTime.now());

        assertEquals(DealStatus.EXPIRED, deal.getStatus());
        assertFalse(DealHistoryService.buyerHistory("alice").contains(deal));
        assertEquals("Borrowed", DealHistoryService.typeLabel(deal));
    }
}
