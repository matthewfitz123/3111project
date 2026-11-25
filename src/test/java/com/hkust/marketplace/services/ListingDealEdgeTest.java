package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListingDealEdgeTest {

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
    void nullListingsAreNotEditable() {
        assertFalse(ListingService.canModify(null));
        assertEquals(0, ListingService.pendingOfferCount(null));
    }

    @Test
    void editRejectsEmptyAndInvalidFields() {
        login("charlie");
        assertThrows(IllegalArgumentException.class, () -> ListingService.editListing(
                "L100", " ", "desc", "Textbooks", "Used", "Library", 10.0, null));
        assertThrows(IllegalArgumentException.class, () -> ListingService.editListing(
                "L100", "title", "desc", null, "Used", "Library", 10.0, null));
        assertThrows(IllegalArgumentException.class, () -> ListingService.editListing(
                "L100", "title", "desc", "Textbooks", null, "Library", 10.0, null));
        assertThrows(IllegalArgumentException.class, () -> ListingService.editListing(
                "L100", "title", "desc", "Textbooks", "Used", null, 10.0, null));
        assertThrows(IllegalArgumentException.class, () -> ListingService.editListing(
                "L100", "title", "desc", "Textbooks", "Used", "Library", 0.0, null));
        login("diana");
        assertThrows(IllegalArgumentException.class, () -> ListingService.editListing(
                "L103", "title", "desc", "Electronics", "Used", "Library", null, null));
    }

    @Test
    void editAndDeleteRejectMissingOrForeignListings() {
        login("diana");
        assertThrows(SecurityException.class, () -> ListingService.editListing(
                "L100", "title", "desc", "Textbooks", "Used", "Library", 10.0, null));
        assertThrows(IllegalArgumentException.class, () -> ListingService.deleteListing("missing"));
    }

    @Test
    void dealStatusGuardsAreEnforced() {
        Deal sell = createAcceptedSellDeal();
        login("alice");
        DealService.markComplete(sell.getId());
        assertThrows(IllegalStateException.class, () -> DealService.markComplete(sell.getId()));
        assertThrows(IllegalStateException.class, () -> DealService.returnEarly(sell.getId()));
        assertThrows(IllegalArgumentException.class, () -> DealService.markComplete("missing"));
    }

    @Test
    void schedulerIgnoresNullWrongTypeAndClosedDeals() {
        Deal lend = createAcceptedLendDeal();
        lend.setDueAt(null);
        BorrowScheduler.checkExpired(LocalDateTime.now());
        assertEquals(DealStatus.ACTIVE, lend.getStatus());

        Deal sell = new Deal();
        sell.setId("D-SELL");
        sell.setDealType(ListingType.SELL);
        sell.setStatus(DealStatus.ACTIVE);
        sell.setDueAt(LocalDateTime.now().minusHours(1));
        store.addDeal(sell);
        BorrowScheduler.checkExpired(LocalDateTime.now());
        assertEquals(DealStatus.ACTIVE, sell.getStatus());

        lend.setDueAt(LocalDateTime.now().minusMinutes(1));
        lend.setStatus(DealStatus.COMPLETED);
        BorrowScheduler.checkExpired(LocalDateTime.now());
        assertEquals(DealStatus.COMPLETED, lend.getStatus());
    }

    @Test
    void schedulerHandlesAMissingListing() {
        Deal deal = new Deal();
        deal.setId("D-MISSING");
        deal.setListingId("missing");
        deal.setDealType(ListingType.LEND);
        deal.setStatus(DealStatus.ACTIVE);
        deal.setDueAt(LocalDateTime.now().minusMinutes(1));
        store.addDeal(deal);

        BorrowScheduler.checkExpired(LocalDateTime.now());

        assertEquals(DealStatus.EXPIRED, deal.getStatus());
    }

    private Deal createAcceptedSellDeal() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 150.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        return store.getAllDeals().stream()
                .filter(d -> "L100".equals(d.getListingId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private Deal createAcceptedLendDeal() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(2), null);
        login("diana");
        OfferService.acceptOffer(offer.getId());
        return store.getAllDeals().stream()
                .filter(d -> "L103".equals(d.getListingId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
