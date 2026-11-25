package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.11 review after a completed purchase or returned borrow
 */
class BuyerReviewTest {

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
    void aCompletedDealStoresOneRatingAndUpdatesTheSeller() {
        Deal deal = completedSale();
        User alice = login("alice");

        int before = store.getReviewsForSeller("charlie").size();
        Review review = ReviewService.submitReview(deal, alice, 4, "met at the library");

        assertEquals(4, review.getRating());
        assertEquals("met at the library", review.getComment());
        assertEquals("alice", review.getBuyerUsername());
        assertTrue(deal.isRated());
        assertEquals(before + 1, store.getReviewsForSeller("charlie").size());
        assertTrue(store.findUser("charlie").getRatingCount() >= 1);
        assertTrue(store.findUser("charlie").getAverageRating() > 0);

        assertThrows(IllegalStateException.class,
                () -> ReviewService.submitReview(deal, alice, 5, "again"));
        assertThrows(IllegalArgumentException.class,
                () -> ReviewService.submitReview(anotherCompletedSale(), alice, 0, "no"));
    }

    @Test
    void theBuyerKeepsEveryReviewTheySubmitted() {
        Deal first = completedSale();
        User alice = login("alice");
        ReviewService.submitReview(first, alice, 5, "first");

        List<Review> mine = store.getAllReviews().stream()
                .filter(r -> "alice".equalsIgnoreCase(r.getBuyerUsername()))
                .filter(r -> "first".equals(r.getComment()))
                .toList();
        assertEquals(1, mine.size());
    }

    @Test
    void aReturnedBorrowCanBeReviewed() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(1), null);
        login("diana");
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getAllDeals().stream()
                .filter(d -> "L103".equals(d.getListingId()))
                .reduce((a, b) -> b)
                .orElseThrow();
        login("alice");
        DealService.returnEarly(deal.getId());

        assertEquals(DealStatus.RETURNED, deal.getStatus());
        User buyer = login("alice");
        Review review = ReviewService.submitReview(deal, buyer, 5, "thanks");
        assertEquals(5, review.getRating());
        assertTrue(deal.isRated());
    }

    private Deal completedSale() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                110.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getAllDeals().stream()
                .filter(d -> "L100".equals(d.getListingId()))
                .reduce((a, b) -> b)
                .orElseThrow();
        login("alice");
        DealService.markComplete(deal.getId());
        return deal;
    }

    private Deal anotherCompletedSale() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L101"), alice,
                20.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getAllDeals().stream()
                .filter(d -> "L101".equals(d.getListingId()))
                .reduce((a, b) -> b)
                .orElseThrow();
        login("alice");
        DealService.markComplete(deal.getId());
        return deal;
    }
}
