package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * seller My Listings edit and delete (task 2.5) and admin review or takedown (tasks 3.2 and 3.7)
 */
public final class ListingService {

    private ListingService() {
        // static service
    }

    /**
     * true when the listing can still be changed
     * reserved, sold, lent, given and removed listings are locked
     */
    public static boolean canModify(Listing listing) {
        if (listing == null) {
            return false;
        }
        boolean editableStatus = listing.getStatus() == ListingStatus.LIVE
                || listing.getStatus() == ListingStatus.REJECTED
                || listing.getStatus() == ListingStatus.PENDING_REVIEW;
        return editableStatus && pendingOfferCount(listing) == 0;
    }

    /**
     * counts offers that are still waiting on the seller
     */
    public static long pendingOfferCount(Listing listing) {
        if (listing == null || listing.getId() == null) {
            return 0;
        }
        return DummyDataService.getInstance().getOffersForListing(listing.getId()).stream()
                .filter(offer -> offer.getStatus() == OfferStatus.PENDING)
                .count();
    }

    /**
     * updates the editable fields on a listing owned by the signed in seller
     * a rejected listing goes back into the review queue
     */
    public static synchronized void editListing(String listingId, String title, String description,
                                                String category, String condition, String meetup,
                                                Double price, Integer days) {
        User seller = SessionManager.requireRole(UserRole.SELLER);
        Listing listing = requireOwnedListing(listingId, seller);
        if (!canModify(listing)) {
            throw new IllegalStateException("this listing cannot be edited right now");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("pick a category");
        }
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("pick a condition");
        }
        if (meetup == null || meetup.isBlank()) {
            throw new IllegalArgumentException("pick a meet-up location");
        }
        if (listing.getType() == ListingType.SELL && (price == null || price <= 0)) {
            throw new IllegalArgumentException("enter a price above zero");
        }
        if (listing.getType() == ListingType.LEND && (days == null || days <= 0)) {
            throw new IllegalArgumentException("enter a lending period above zero");
        }

        listing.setTitle(title.trim());
        listing.setDescription(description == null ? "" : description.trim());
        listing.setCategory(category);
        listing.setCondition(condition);
        listing.setMeetupLocation(meetup);
        listing.setPrice(listing.getType() == ListingType.SELL ? price : null);
        listing.setLendingDays(listing.getType() == ListingType.LEND ? days : null);

        // a rejected listing gets another chance after the seller fixes it
        if (listing.getStatus() == ListingStatus.REJECTED) {
            listing.setStatus(ListingStatus.PENDING_REVIEW);
            listing.setRejectionReason(null);
            listing.setReviewedAt(null);
        }
        DummyDataService.getInstance().updateListing(listing);
    }

    /**
     * deletes a listing owned by the signed in seller after the UI confirms
     */
    public static synchronized void deleteListing(String listingId) {
        User seller = SessionManager.requireRole(UserRole.SELLER);
        Listing listing = requireOwnedListing(listingId, seller);
        if (!canModify(listing)) {
            throw new IllegalStateException("this listing cannot be deleted right now");
        }
        DummyDataService.getInstance().removeListing(listingId);
    }

    /**
     * approves a pending listing and tells the seller (task 3.2)
     */
    public static void approveListing(Listing listing) {
        SessionManager.requireRole(UserRole.ADMIN);
        if (listing == null) {
            throw new IllegalArgumentException("that listing no longer exists");
        }
        listing.setStatus(ListingStatus.LIVE);
        listing.setReviewedAt(LocalDateTime.now());
        DummyDataService.getInstance().updateListing(listing);
        NotificationService.listingApproved(listing);
    }

    /**
     * rejects a pending listing with a required reason and tells the seller (task 3.2)
     */
    public static void rejectListing(Listing listing, String reason) {
        SessionManager.requireRole(UserRole.ADMIN);
        if (listing == null) {
            throw new IllegalArgumentException("that listing no longer exists");
        }
        String text = reason == null ? "" : reason.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }
        listing.setStatus(ListingStatus.REJECTED);
        listing.setRejectionReason(text);
        listing.setReviewedAt(LocalDateTime.now());
        DummyDataService.getInstance().updateListing(listing);
        NotificationService.listingRejected(listing, text);
    }

    /**
     * takes a listing down, keeps it as REMOVED, and tells the seller plus
     * buyers with a pending or countered offer or an active deal (task 3.7)
     */
    public static void takeDownListing(Listing listing, String reason) {
        SessionManager.requireRole(UserRole.ADMIN);
        if (listing == null) {
            throw new IllegalArgumentException("that listing no longer exists");
        }
        String text = reason == null ? "" : reason.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }
        listing.setStatus(ListingStatus.REMOVED);
        listing.setRejectionReason(text);
        listing.setReviewedAt(LocalDateTime.now());
        DummyDataService store = DummyDataService.getInstance();
        store.updateListing(listing);
        NotificationService.listingRemoved(listing, text);
        String notice = "'" + listing.getTitle() + "' was removed by an admin: " + text;
        for (String buyer : affectedBuyers(listing)) {
            NotificationService.notify(buyer, "Listing Removed", notice, "System");
        }
    }

    private static Set<String> affectedBuyers(Listing listing) {
        DummyDataService store = DummyDataService.getInstance();
        Set<String> buyers = new LinkedHashSet<>();
        for (Offer offer : store.getOffersForListing(listing.getId())) {
            if (offer.getStatus() == OfferStatus.PENDING || offer.getStatus() == OfferStatus.COUNTERED) {
                buyers.add(offer.getBuyerUsername());
            }
        }
        for (Deal deal : store.getAllDeals()) {
            if (listing.getId().equals(deal.getListingId())
                    && (deal.getStatus() == DealStatus.PENDING || deal.getStatus() == DealStatus.ACTIVE)) {
                buyers.add(deal.getBuyerUsername());
            }
        }
        buyers.remove(listing.getSellerUsername());
        return buyers;
    }

    // shared guard so sellers cannot touch another seller's listing
    private static Listing requireOwnedListing(String listingId, User seller) {
        Listing listing = listingId == null ? null : DummyDataService.getInstance().findListing(listingId);
        if (listing == null) {
            throw new IllegalArgumentException("that listing no longer exists");
        }
        if (listing.getSellerUsername() == null
                || !listing.getSellerUsername().equalsIgnoreCase(seller.getUsername())) {
            throw new SecurityException("that listing is not yours");
        }
        return listing;
    }
}
