package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;

import java.util.List;

/**
 * buyer browse-marketplace card rules (task 1.3)
 * keeps live-listing selection, thumbnail choice, seller rating,
 * verified badge and availability label logic out of the JavaFX card
 */
public final class BrowseService {

    private BrowseService() {
        // static service
    }

    /**
     * returns the admin-approved live listings shown on the browse screen
     */
    public static List<Listing> liveListings() {
        return DummyDataService.getInstance().getLiveListings();
    }

    /**
     * returns the first usable photo path for a listing thumbnail, or null when none exists
     */
    public static String primaryPhotoPath(Listing listing) {
        if (listing == null || listing.getPhotoPaths() == null) {
            return null;
        }
        for (String path : listing.getPhotoPaths()) {
            if (path != null && !path.isBlank()) {
                return path;
            }
        }
        return null;
    }

    /**
     * short seller rating label for the card, based on the stored seller account
     */
    public static String sellerRatingLabel(Listing listing) {
        User seller = seller(listing);
        if (seller == null || seller.getRatingCount() == 0) {
            return "No ratings yet";
        }
        return String.format("%.1f ★ (%d)", seller.getAverageRating(), seller.getRatingCount());
    }

    /**
     * true when the listing seller is a verified seller account
     */
    public static boolean showVerifiedBadge(Listing listing) {
        User seller = seller(listing);
        return seller != null && seller.getRole() == UserRole.SELLER && seller.isVerified();
    }

    /**
     * buyer-facing availability label; browse only renders live listings so this is normally Available
     */
    public static String availabilityLabel(Listing listing) {
        if (listing == null || listing.getStatus() == null) {
            return "Unavailable";
        }
        return switch (listing.getStatus()) {
            case LIVE -> "Available";
            case RESERVED -> "Reserved";
            case SOLD -> "Sold";
            case LENT -> "Lent";
            case GIVEN -> "Given";
            case PENDING_REVIEW -> "Pending review";
            case REJECTED -> "Rejected";
            case REMOVED -> "Removed";
        };
    }

    private static User seller(Listing listing) {
        if (listing == null || listing.getSellerUsername() == null) {
            return null;
        }
        return DummyDataService.getInstance().findUser(listing.getSellerUsername());
    }
}
