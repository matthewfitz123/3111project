package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void listingReviewNotificationsAreCreated() {
        NotificationService.listingApproved(store.findListing("L100"));
        NotificationService.listingRejected(store.findListing("L101"), "bad photos");

        assertTrue(hasNotification("charlie", "Listing Approved"));
        assertTrue(hasNotification("charlie", "Listing Rejected"));
    }

    @Test
    void offerAndChatNotificationsAreCreated() {
        User alice = store.findUser("alice");
        SessionManager.login(alice);
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                100.0, null, null, null);
        assertTrue(hasNotification("charlie", "New Offer"));

        ChatMessage message = new ChatMessage();
        message.setSenderUsername("alice");
        message.setContent("hello");
        NotificationService.newChatMessage(message, "charlie");
        assertTrue(hasNotification("charlie", "New Chat Message"));

        NotificationService.offerAccepted(offer);
        assertTrue(hasNotification("alice", "Offer Accepted"));
    }

    @Test
    void reviewAndDealNotificationsAreCreated() {
        Review review = new Review();
        review.setSellerUsername("charlie");
        review.setBuyerUsername("alice");
        review.setListingTitle("Calculus Textbook");
        review.setRating(5);
        NotificationService.reviewReceived(review);
        NotificationService.reviewReplied(review);
        assertTrue(hasNotification("charlie", "New Review"));
        assertTrue(hasNotification("alice", "Review Reply"));

        Deal deal = new Deal();
        deal.setBuyerUsername("alice");
        deal.setSellerUsername("charlie");
        deal.setListingTitle("Calculus Textbook");
        NotificationService.dealCompleted(deal);
        assertTrue(hasNotification("alice", "Deal Completed"));
        assertTrue(hasNotification("charlie", "Deal Completed"));
    }

    @Test
    void borrowRemindersAreCreated() {
        Deal deal = new Deal();
        deal.setBuyerUsername("alice");
        deal.setSellerUsername("charlie");
        deal.setListingTitle("Calculator");
        deal.setDueAt(java.time.LocalDateTime.now().plusHours(1));

        NotificationService.borrowDueSoon(deal);
        NotificationService.borrowExpired(deal);

        assertTrue(hasNotification("alice", "Borrow Due Soon"));
        assertTrue(hasNotification("alice", "Borrow Expired"));
        assertTrue(hasNotification("charlie", "Borrow Ended"));
    }

    private boolean hasNotification(String username, String title) {
        return store.getNotificationsFor(username).stream()
                .anyMatch(n -> title.equals(n.getTitle()));
    }
}
