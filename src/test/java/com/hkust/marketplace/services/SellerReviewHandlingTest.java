package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 2.10 seller reply or flag, and task 3.8 admin hide
 */
class SellerReviewHandlingTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void replyNotifiesTheBuyerAndFlagHidesTheReview() {
        Review review = reviewedSale();
        SessionManager.login(store.findUser("charlie"));

        ReviewService.replyToReview(review.getId(), "glad it helped");
        assertEquals("glad it helped", review.getSellerReply());
        assertTrue(store.getNotificationsFor("alice").stream()
                .anyMatch(n -> "Review Reply".equals(n.getTitle())));

        ReviewService.flagReview(review.getId(), "spam");
        assertTrue(review.isHidden());
        assertFalse(store.getReviewsForSeller("charlie").contains(review));

        SessionManager.login(store.findUser("admin"));
        ReviewService.setHidden(review.getId(), false);
        assertFalse(review.isHidden());
        assertTrue(store.getReviewsForSeller("charlie").contains(review));
    }

    private Review reviewedSale() {
        User alice = store.findUser("alice");
        SessionManager.login(alice);
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 80.0, null, null, null);
        SessionManager.login(store.findUser("charlie"));
        OfferService.acceptOffer(offer.getId());
        Deal deal = store.getAllDeals().stream()
                .filter(d -> "L100".equals(d.getListingId()))
                .reduce((a, b) -> b)
                .orElseThrow();
        SessionManager.login(alice);
        DealService.markComplete(deal.getId());
        return ReviewService.submitReview(deal, alice, 5, "good");
    }
}
