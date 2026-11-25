package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.8 buyer notification categories, timestamps and read state
 */
class BuyerNotificationBoardTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void offerChatAndReviewEventsAreCategorizedForTheBuyer() {
        User alice = store.findUser("alice");
        SessionManager.login(alice);
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                80.0, null, null, null);

        NotificationService.offerAccepted(offer);
        NotificationService.offerDeclined(offer);
        offer.setCounterPrice(70.0);
        NotificationService.offerCountered(offer);

        ChatMessage message = new ChatMessage();
        message.setSenderUsername("charlie");
        message.setContent("still available");
        NotificationService.newChatMessage(message, "alice");

        Review review = new Review();
        review.setBuyerUsername("alice");
        review.setSellerUsername("charlie");
        review.setListingTitle("Calculus Textbook (Stewart)");
        NotificationService.reviewReplied(review);

        NotificationService.notify("alice", "Campus fair", "tables at the concourse", "System");

        assertTrue(titled("alice", "Offer Accepted", "Offer"));
        assertTrue(titled("alice", "Offer Declined", "Offer"));
        assertTrue(titled("alice", "Offer Countered", "Offer"));
        assertTrue(titled("alice", "New Chat Message", "Chat"));
        assertTrue(titled("alice", "Review Reply", "Rating"));
        assertTrue(titled("alice", "Campus fair", "System"));

        List<Notification> board = store.getNotificationsFor("alice");
        assertFalse(board.isEmpty());
        assertNotNull(board.get(0).getTimestamp());
        assertTrue(!board.get(0).getTimestamp().isBefore(board.get(board.size() - 1).getTimestamp()));
    }

    @Test
    void dueSoonReminderCanBeStoredButNothingSchedulesIt() {
        Deal deal = new Deal();
        deal.setBuyerUsername("alice");
        deal.setListingTitle("Calculator");
        deal.setDueAt(java.time.LocalDateTime.now().plusHours(1));
        NotificationService.borrowDueSoon(deal);

        assertTrue(titled("alice", "Borrow Due Soon", "System"));
    }

    @Test
    void listingRemovedNotifiesTheSellerOnly() {
        Listing listing = store.findListing("L100");
        NotificationService.listingRemoved(listing, "duplicate post");

        assertTrue(titled("charlie", "Listing Removed", "System"));
        assertTrue(store.getNotificationsFor("alice").stream()
                .noneMatch(n -> "Listing Removed".equals(n.getTitle())));
    }

    @Test
    void readStateClearsTheUnreadCount() {
        NotificationService.notify("alice", "Hello", "one note", "System");
        assertTrue(store.getUnreadNotificationCount("alice") >= 1);

        for (Notification notification : store.getNotificationsFor("alice")) {
            store.markNotificationRead(notification.getId());
        }
        assertEquals(0, store.getUnreadNotificationCount("alice"));

        NotificationService.notify("alice", "Again", "another", "System");
        store.markAllNotificationsRead("alice");
        assertEquals(0, store.getUnreadNotificationCount("alice"));
    }

    private boolean titled(String username, String title, String category) {
        return store.getNotificationsFor(username).stream()
                .anyMatch(n -> title.equals(n.getTitle()) && category.equals(n.getCategory()));
    }
}
