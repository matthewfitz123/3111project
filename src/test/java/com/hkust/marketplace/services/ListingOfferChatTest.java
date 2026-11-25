package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.ChatThread;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.5 buyer actions from listing detail, plus the private listing chat:
 * a price offer, a borrow request, a giveaway claim, and a shared searchable thread
 */
class ListingOfferChatTest {

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
    void sellListingTakesAPendingPriceOffer() {
        User alice = login("alice");
        Listing listing = store.findListing("L100");

        Offer offer = OfferService.submitOffer(listing, alice, 120.0, null, null, null);

        assertEquals(ListingType.SELL, offer.getType());
        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertEquals(120.0, offer.getOfferPrice());
        assertEquals(ListingStatus.LIVE, listing.getStatus());
        assertTrue(store.getNotificationsFor("charlie").stream()
                .anyMatch(n -> "New Offer".equals(n.getTitle())));
    }

    @Test
    void lendListingTakesASameDayBorrowRequest() {
        User alice = login("alice");
        Listing listing = store.findListing("L103");
        LocalDate today = LocalDate.now();

        // the screens only pick dates; the 5-second demo minimum is a same-day request
        Offer offer = OfferService.submitOffer(listing, alice, null, today, today, "library pickup");

        assertEquals(ListingType.LEND, offer.getType());
        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertEquals(today, offer.getBorrowStart());
        assertEquals(today, offer.getBorrowEnd());
        assertEquals("library pickup", offer.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> OfferService.submitOffer(listing, alice, null,
                        today, today.minusDays(1), null));
    }

    @Test
    void giveawayClaimConfirmsForTheBuyerAndClosesTheListing() {
        User alice = login("alice");
        Listing listing = store.findListing("L102");

        Offer claim = OfferService.claimGiveaway(listing, alice);

        assertEquals(OfferStatus.ACCEPTED, claim.getStatus());
        assertEquals(ListingStatus.GIVEN, store.findListing("L102").getStatus());
        assertTrue(store.getNotificationsFor("alice").stream()
                .anyMatch(n -> "Claim Confirmed".equals(n.getTitle())));
        assertThrows(IllegalStateException.class,
                () -> OfferService.claimGiveaway(listing, login("bob")));
    }

    @Test
    void actionsAreRejectedWhenTheListingIsNotLive() {
        User alice = login("alice");
        Listing pending = store.findListing("L104");

        assertThrows(IllegalStateException.class,
                () -> OfferService.submitOffer(pending, alice, 40.0, null, null, null));
        assertThrows(IllegalStateException.class,
                () -> OfferService.claimGiveaway(pending, alice));
    }

    @Test
    void buyerAndSellerShareOneRetainedSearchableThread() {
        ChatThread thread = store.getOrCreateThread("L100", "alice");
        assertEquals(thread.getId(), store.getOrCreateThread("L100", "alice").getId());
        assertEquals("charlie", thread.getSellerUsername());

        store.addChatMessage(text(thread, "alice", "is the textbook still available"));
        store.addChatMessage(text(thread, "charlie", "yes, library meetup works"));

        List<ChatMessage> history = store.getMessagesForThread(thread.getId());
        assertEquals(2, history.size());
        assertEquals("alice", history.get(0).getSenderUsername());
        assertEquals("charlie", history.get(1).getSenderUsername());
        assertEquals(1, store.getThreadsForUser("alice").size());
        assertEquals(1, store.getThreadsForUser("charlie").size());

        List<ChatMessage> found = store.searchMessages("TEXTBOOK");
        assertEquals(1, found.size());
        assertEquals(thread.getId(), found.get(0).getThreadId());
        assertTrue(store.searchMessages("sold already").isEmpty());
    }

    @Test
    void formalOfferIsKeptOnTheSameThreadAndCanBeSearched() {
        User alice = login("alice");
        ChatThread thread = store.getOrCreateThread("L100", "alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice,
                90.0, null, null, "Sent via chat");

        ChatMessage formal = new ChatMessage();
        formal.setThreadId(thread.getId());
        formal.setSenderUsername("alice");
        formal.setFormalOffer(true);
        formal.setOfferId(offer.getId());
        formal.setContent("[Offer] HK$90");
        store.addChatMessage(formal);

        List<ChatMessage> history = store.getMessagesForThread(thread.getId());
        assertEquals(1, history.size());
        assertTrue(history.get(0).isFormalOffer());
        assertEquals(offer.getId(), history.get(0).getOfferId());
        assertFalse(store.searchMessages("HK$90").isEmpty());
    }

    private ChatMessage text(ChatThread thread, String sender, String content) {
        ChatMessage message = new ChatMessage();
        message.setThreadId(thread.getId());
        message.setSenderUsername(sender);
        message.setContent(content);
        return message;
    }
}
