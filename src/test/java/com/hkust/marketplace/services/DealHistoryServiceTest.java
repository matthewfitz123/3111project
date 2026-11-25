package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.utils.DummyDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DealHistoryServiceTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void buyerHistoryOnlyContainsCompletedAndReturnedDeals() {
        LocalDateTime older = LocalDateTime.now().minusDays(3);
        LocalDateTime newer = LocalDateTime.now().minusDays(1);
        Deal completed = addDeal("alice", "charlie", "L100", ListingType.SELL,
                DealStatus.COMPLETED, older);
        Deal returned = addDeal("alice", "diana", "L103", ListingType.LEND,
                DealStatus.RETURNED, newer);
        addDeal("alice", "charlie", "L101", ListingType.SELL, DealStatus.ACTIVE, null);
        Deal expired = addDeal("alice", "diana", "L103", ListingType.LEND, DealStatus.EXPIRED, newer);
        addDeal("alice", "charlie", "L102", ListingType.GIVEAWAY,
                DealStatus.CANCELLED, null);

        List<Deal> history = DealHistoryService.buyerHistory("alice");

        assertEquals(List.of(returned.getId(), expired.getId(), completed.getId()),
                history.stream().map(Deal::getId).toList());
        assertTrue(DealHistoryService.allDeals().stream()
                .anyMatch(deal -> deal.getStatus() == DealStatus.ACTIVE));
    }

    @Test
    void labelsAndDatesUseTheDealFields() {
        LocalDateTime completedAt = LocalDateTime.of(2026, 1, 10, 12, 0);
        Deal sell = addDeal("alice", "charlie", "L100", ListingType.SELL,
                DealStatus.COMPLETED, completedAt);
        Deal lend = addDeal("alice", "diana", "L103", ListingType.LEND,
                DealStatus.RETURNED, completedAt);
        lend.setBorrowStart(LocalDate.of(2026, 1, 5));
        Deal give = addDeal("bob", "diana", "L102", ListingType.GIVEAWAY,
                DealStatus.COMPLETED, completedAt);
        Deal missing = addDeal("alice", "charlie", "L999", ListingType.SELL,
                DealStatus.COMPLETED, completedAt);

        assertEquals("Bought", DealHistoryService.typeLabel(sell));
        assertEquals("Borrowed", DealHistoryService.typeLabel(lend));
        assertEquals("Received", DealHistoryService.typeLabel(give));
        assertEquals("Sold", DealHistoryService.adminTypeLabel(sell));
        assertEquals("Lent", DealHistoryService.adminTypeLabel(lend));
        assertEquals("Given", DealHistoryService.adminTypeLabel(give));
        assertEquals("Completed", DealHistoryService.statusLabel(sell));
        assertEquals("Returned", DealHistoryService.statusLabel(lend));
        assertEquals("Textbooks", DealHistoryService.category(sell));
        assertEquals("Unknown", DealHistoryService.category(missing));
        assertEquals(LocalDate.of(2026, 1, 9), DealHistoryService.startDate(sell));
        assertEquals(LocalDate.of(2026, 1, 5), DealHistoryService.startDate(lend));
        assertEquals(LocalDate.of(2026, 1, 10), DealHistoryService.endDate(sell));
        assertEquals(LocalDate.of(2026, 1, 10), DealHistoryService.historyDate(sell));
    }

    @Test
    void matchesAppliesTheSharedFilters() {
        LocalDateTime completedAt = LocalDateTime.of(2026, 2, 10, 12, 0);
        Deal textbooks = addDeal("alice", "charlie", "L100", ListingType.SELL,
                DealStatus.COMPLETED, completedAt);
        Deal electronics = addDeal("alice", "diana", "L103", ListingType.LEND,
                DealStatus.RETURNED, completedAt.plusDays(1));

        assertTrue(DealHistoryService.matches(textbooks,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28),
                "All Categories", null, null, "", false));
        assertFalse(DealHistoryService.matches(textbooks,
                LocalDate.of(2026, 3, 1), null,
                "All Categories", null, null, "", false));

        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "Textbooks", null, null, "", false));
        assertFalse(DealHistoryService.matches(textbooks, null, null,
                "Electronics", null, null, "", false));

        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "All Categories", "Bought", null, "", false));
        assertTrue(DealHistoryService.matches(electronics, null, null,
                "All Categories", "Borrowed", null, "", false));
        assertFalse(DealHistoryService.matches(electronics, null, null,
                "All Categories", "Bought", null, "", false));
        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "All Categories", "Sold", null, "", true));
        assertFalse(DealHistoryService.matches(textbooks, null, null,
                "All Categories", "Bought", null, "", true));

        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "All Categories", null, "Completed", "", false));
        assertFalse(DealHistoryService.matches(textbooks, null, null,
                "All Categories", null, "Returned", "", false));

        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "All Categories", null, null, "charlie", false));
        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "All Categories", null, null, "Calculus", false));
        assertFalse(DealHistoryService.matches(textbooks, null, null,
                "All Categories", null, null, "alice", false));
        assertTrue(DealHistoryService.matches(textbooks, null, null,
                "All Categories", null, null, "alice", true));
    }

    private Deal addDeal(String buyer, String seller, String listingId, ListingType type,
                         DealStatus status, LocalDateTime completedAt) {
        Deal deal = new Deal();
        deal.setListingId(listingId);
        Listing listing = store.findListing(listingId);
        deal.setListingTitle(listing != null ? listing.getTitle() : "Missing item");
        deal.setBuyerUsername(buyer);
        deal.setSellerUsername(seller);
        deal.setDealType(type);
        deal.setStatus(status);
        deal.setCompletedAt(completedAt);
        deal.setCreatedAt(completedAt == null
                ? LocalDateTime.now().minusDays(1) : completedAt.minusDays(1));
        store.addDeal(deal);
        return deal;
    }
}
