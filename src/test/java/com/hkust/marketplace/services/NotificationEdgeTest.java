package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.utils.DummyDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationEdgeTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void nullInputsAreIgnored() {
        assertDoesNotThrow(() -> {
            NotificationService.listingApproved(null);
            NotificationService.listingRejected(null, "why");
            NotificationService.offerReceived(null);
            NotificationService.offerAccepted(null);
            NotificationService.offerDeclined(null);
            NotificationService.offerCountered(null);
            NotificationService.newChatMessage(null, "alice");
            NotificationService.dealCompleted(null);
            NotificationService.reviewReceived(null);
            NotificationService.reviewReplied(null);
            NotificationService.listingRemoved(null, "why");
            NotificationService.borrowDueSoon(null);
            NotificationService.borrowExpired(null);
        });
        assertDoesNotThrow(() -> NotificationService.notify(null, "title", "msg", "System"));
        assertDoesNotThrow(() -> NotificationService.notify(" ", "title", "msg", "System"));
    }

    @Test
    void offersOfEveryTypeProduceMessages() {
        Offer lend = new Offer();
        lend.setListingId("L103");
        lend.setBuyerUsername("alice");
        lend.setSellerUsername("diana");
        lend.setType(ListingType.LEND);
        lend.setBorrowStart(LocalDate.now());
        lend.setBorrowEnd(LocalDate.now().plusDays(2));
        NotificationService.offerReceived(lend);
        NotificationService.offerCountered(lend);

        Offer giveaway = new Offer();
        giveaway.setListingId("L102");
        giveaway.setBuyerUsername("alice");
        giveaway.setSellerUsername("diana");
        giveaway.setType(ListingType.GIVEAWAY);
        NotificationService.offerReceived(giveaway);

        Offer missingListing = new Offer();
        missingListing.setListingId("missing");
        missingListing.setBuyerUsername("nobody");
        missingListing.setSellerUsername("diana");
        missingListing.setType(ListingType.SELL);
        missingListing.setOfferPrice(10.0);
        NotificationService.offerReceived(missingListing);
    }

    @Test
    void chatAndReviewPreviewsAreSafe() {
        ChatMessage message = new ChatMessage();
        message.setSenderUsername("alice");
        message.setContent("x".repeat(100));
        NotificationService.newChatMessage(message, "charlie");

        ChatMessage empty = new ChatMessage();
        empty.setSenderUsername(null);
        empty.setContent(null);
        NotificationService.newChatMessage(empty, "charlie");

        Review review = new Review();
        review.setSellerUsername("charlie");
        review.setBuyerUsername("alice");
        review.setListingTitle("book");
        review.setRating(3);
        NotificationService.reviewReceived(review);
        NotificationService.reviewReplied(review);
    }
}
