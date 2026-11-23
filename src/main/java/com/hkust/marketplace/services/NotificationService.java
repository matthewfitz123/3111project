package com.hkust.marketplace.services;

import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;

/**
 * one place for all the notification strings
 * later screens just call a typed method instead of building text by hand
 */
public final class NotificationService {

    private static final String SYSTEM = "System";
    private static final String OFFER = "Offer";
    private static final String CHAT = "Chat";
    private static final String REVIEW = "Rating";

    private NotificationService() {
        // static helpers only
    }

    /**
     * sends a plain notification to one user
     */
    public static void notify(String username, String title, String message, String category) {
        if (username == null || username.isBlank()) {
            return;
        }
        DummyDataService.getInstance().addNotification(
                new Notification(username, title, message, category));
    }

    /**
     * tells the seller their listing went live
     */
    public static void listingApproved(Listing listing) {
        if (listing == null) {
            return;
        }
        notify(listing.getSellerUsername(), "Listing Approved",
                "your listing '" + listing.getTitle() + "' is now live",
                SYSTEM);
    }

    /**
     * tells the seller why a listing was rejected
     */
    public static void listingRejected(Listing listing, String reason) {
        if (listing == null) {
            return;
        }
        notify(listing.getSellerUsername(), "Listing Rejected",
                "your listing '" + listing.getTitle() + "' was rejected: " + reason,
                SYSTEM);
    }

    /**
     * tells the seller a buyer has offered, asked to borrow or claimed an item
     */
    public static void offerReceived(Offer offer) {
        if (offer == null) {
            return;
        }
        String title = listingTitle(offer.getListingId());
        String buyer = displayName(offer.getBuyerUsername());
        String message;
        switch (offer.getType()) {
            case SELL -> message = buyer + " offered HK$" + money(offer.getOfferPrice())
                    + " for '" + title + "'";
            case LEND -> message = buyer + " asked to borrow '" + title + "' from "
                    + offer.getBorrowStart() + " to " + offer.getBorrowEnd();
            case GIVEAWAY -> message = buyer + " claimed your giveaway '" + title + "'";
            default -> message = buyer + " sent a request for '" + title + "'";
        }
        notify(offer.getSellerUsername(), "New Offer", message, OFFER);
    }

    /**
     * tells the buyer their offer was accepted
     */
    public static void offerAccepted(Offer offer) {
        if (offer == null) {
            return;
        }
        notify(offer.getBuyerUsername(), "Offer Accepted",
                "your offer for '" + listingTitle(offer.getListingId()) + "' was accepted",
                OFFER);
    }

    /**
     * tells the buyer their offer was declined
     */
    public static void offerDeclined(Offer offer) {
        if (offer == null) {
            return;
        }
        notify(offer.getBuyerUsername(), "Offer Declined",
                "your offer for '" + listingTitle(offer.getListingId()) + "' was declined",
                OFFER);
    }

    /**
     * tells the buyer about the seller's counter offer
     */
    public static void offerCountered(Offer offer) {
        if (offer == null) {
            return;
        }
        String detail = offer.getType() == com.hkust.marketplace.models.ListingType.SELL
                ? "HK$" + money(offer.getCounterPrice())
                : "a new return date of " + offer.getCounterEnd();
        notify(offer.getBuyerUsername(), "Offer Countered",
                "the seller countered '" + listingTitle(offer.getListingId()) + "' with " + detail,
                OFFER);
    }

    /**
     * tells one person there is a new chat message
     */
    public static void newChatMessage(ChatMessage message, String recipient) {
        if (message == null) {
            return;
        }
        String sender = displayName(message.getSenderUsername());
        String preview = message.getContent() == null ? "" : message.getContent();
        if (preview.length() > 80) {
            preview = preview.substring(0, 77) + "...";
        }
        notify(recipient, "New Chat Message", sender + ": " + preview, CHAT);
    }

    /**
     * lets both sides know a deal is finished
     */
    public static void dealCompleted(Deal deal) {
        if (deal == null) {
            return;
        }
        notify(deal.getBuyerUsername(), "Deal Completed",
                "you completed the deal for '" + deal.getListingTitle() + "'", SYSTEM);
        notify(deal.getSellerUsername(), "Deal Completed",
                "the deal for '" + deal.getListingTitle() + "' is complete", SYSTEM);
    }

    /**
     * tells the seller a review came in
     */
    public static void reviewReceived(Review review) {
        if (review == null) {
            return;
        }
        notify(review.getSellerUsername(), "New Review",
                displayName(review.getBuyerUsername()) + " left a " + review.getRating()
                        + " star review on '" + review.getListingTitle() + "'",
                REVIEW);
    }

    /**
     * tells the buyer the seller replied to their review
     */
    public static void reviewReplied(Review review) {
        if (review == null) {
            return;
        }
        notify(review.getBuyerUsername(), "Review Reply",
                "the seller replied to your review of '" + review.getListingTitle() + "'",
                REVIEW);
    }

    /**
     * tells the seller an admin removed their listing
     */
    public static void listingRemoved(Listing listing, String reason) {
        if (listing == null) {
            return;
        }
        notify(listing.getSellerUsername(), "Listing Removed",
                "'" + listing.getTitle() + "' was removed by an admin: " + reason,
                SYSTEM);
    }

    /**
     * reminder that a borrow is nearly due
     */
    public static void borrowDueSoon(Deal deal) {
        if (deal == null) {
            return;
        }
        notify(deal.getBuyerUsername(), "Borrow Due Soon",
                "'" + deal.getListingTitle() + "' is due back on " + deal.getDueAt(),
                SYSTEM);
    }

    /**
     * tells the buyer their borrow period has ended
     */
    public static void borrowExpired(Deal deal) {
        if (deal == null) {
            return;
        }
        notify(deal.getBuyerUsername(), "Borrow Expired",
                "the borrow period for '" + deal.getListingTitle() + "' has ended",
                SYSTEM);
    }

    // looks up a listing title for offers, falls back to the id when it is gone
    private static String listingTitle(String listingId) {
        Listing listing = DummyDataService.getInstance().findListing(listingId);
        return listing == null ? "listing " + listingId : listing.getTitle();
    }

    // grabs a full name when we have one, otherwise the username
    private static String displayName(String username) {
        User user = DummyDataService.getInstance().findUser(username);
        return user == null || user.getFullName() == null || user.getFullName().isBlank()
                ? username : user.getFullName();
    }

    private static String money(Double value) {
        return value == null ? "-" : String.format("%.0f", value);
    }
}
