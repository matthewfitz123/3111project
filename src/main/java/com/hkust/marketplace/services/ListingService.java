package com.hkust.marketplace.services;

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

/**
 * seller edits and deletes on their own listings (step 20)
 * the rules live here so they hold even if a button is not disabled
 */
public final class ListingService {

    private ListingService() {
        // static service
    }

    /**
     * only live, pending or rejected listings with no waiting offer can change
     */
    public static boolean canModify(Listing listing) {
        if (listing == null) {
            return false;
        }
        boolean editableStatus = listing.getStatus() == ListingStatus.LIVE
                || listing.getStatus() == ListingStatus.PENDING_REVIEW
                || listing.getStatus() == ListingStatus.REJECTED;
        return editableStatus && pendingOfferCount(listing) == 0;
    }

    public static long pendingOfferCount(Listing listing) {
        return DummyDataService.getInstance().getOffersForListing(listing.getId()).stream()
                .filter(offer -> offer.getStatus() == OfferStatus.PENDING)
                .count();
    }

    public static synchronized void editListing(String listingId, String title, String description,
                                                String category, String condition, String meetup,
                                                Double price, Integer lendingDays) {
        Listing listing = requireModifiableOwnListing(listingId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be empty");
        }
        if (category == null || condition == null || meetup == null) {
            throw new IllegalArgumentException("pick a category, condition and meet-up spot");
        }
        if (listing.getType() == ListingType.SELL
                && (price == null || !Double.isFinite(price) || price <= 0)) {
            throw new IllegalArgumentException("enter a price above zero");
        }
        if (listing.getType() == ListingType.LEND && (lendingDays == null || lendingDays <= 0)) {
            throw new IllegalArgumentException("enter a lending period above zero");
        }

        listing.setTitle(title.trim());
        listing.setDescription(description == null ? "" : description.trim());
        listing.setCategory(category);
        listing.setCondition(condition);
        listing.setMeetupLocation(meetup);
        listing.setPrice(listing.getType() == ListingType.SELL ? price : null);
        listing.setLendingDays(listing.getType() == ListingType.LEND ? lendingDays : null);

        // a rejected listing goes back into the review queue after an edit
        if (listing.getStatus() == ListingStatus.REJECTED) {
            listing.setStatus(ListingStatus.PENDING_REVIEW);
            listing.setRejectionReason(null);
            listing.setReviewedAt(null);
        }
        DummyDataService.getInstance().updateListing(listing);
    }

    public static synchronized void deleteListing(String listingId) {
        Listing listing = requireModifiableOwnListing(listingId);

        // a buyer holding a counter for this listing should not be left waiting
        for (Offer offer : DummyDataService.getInstance().getOffersForListing(listing.getId())) {
            if (offer.getStatus() == OfferStatus.COUNTERED) {
                offer.setStatus(OfferStatus.DECLINED);
                offer.setRespondedAt(LocalDateTime.now());
                DummyDataService.getInstance().updateOffer(offer);
                NotificationService.offerDeclined(offer);
            }
        }
        DummyDataService.getInstance().removeListing(listing.getId());
    }

    private static Listing requireModifiableOwnListing(String listingId) {
        User seller = SessionManager.requireRole(UserRole.SELLER);
        Listing listing = listingId == null ? null : DummyDataService.getInstance().findListing(listingId);
        if (listing == null) {
            throw new IllegalArgumentException("that listing no longer exists");
        }
        if (!listing.getSellerUsername().equalsIgnoreCase(seller.getUsername())) {
            throw new SecurityException("that listing is not yours");
        }
        if (!canModify(listing)) {
            throw new IllegalStateException(pendingOfferCount(listing) > 0
                    ? "answer or decline the pending offers before changing this listing"
                    : "a " + listing.getStatus().name().toLowerCase().replace('_', ' ')
                    + " listing cannot be edited or deleted");
        }
        return listing;
    }
}
