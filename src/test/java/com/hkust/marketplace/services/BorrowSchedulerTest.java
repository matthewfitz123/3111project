package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BorrowSchedulerTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void overdueBorrowIsExpiredAndListingGoesLive() {
        Deal deal = createAcceptedLendDeal();
        deal.setDueAt(LocalDateTime.now().minusMinutes(1));

        BorrowScheduler.checkExpired(LocalDateTime.now());

        assertEquals(DealStatus.EXPIRED, deal.getStatus());
        assertEquals(ListingStatus.LIVE, store.findListing("L103").getStatus());
        assertTrue(hasNotification("alice", "Borrow Expired"));
        assertTrue(hasNotification("diana", "Borrow Ended"));
    }

    @Test
    void futureBorrowIsLeftAlone() {
        Deal deal = createAcceptedLendDeal();
        deal.setDueAt(LocalDateTime.now().plusDays(1));

        BorrowScheduler.checkExpired(LocalDateTime.now());

        assertEquals(DealStatus.ACTIVE, deal.getStatus());
        assertEquals(ListingStatus.RESERVED, store.findListing("L103").getStatus());
    }

    private Deal createAcceptedLendDeal() {
        User alice = store.findUser("alice");
        SessionManager.login(alice);
        Offer offer = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(3), null);
        SessionManager.login(store.findUser("diana"));
        OfferService.acceptOffer(offer.getId());
        return store.getAllDeals().stream()
                .filter(d -> "L103".equals(d.getListingId()))
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private boolean hasNotification(String username, String title) {
        return store.getNotificationsFor(username).stream()
                .anyMatch(n -> title.equals(n.getTitle()));
    }
}
