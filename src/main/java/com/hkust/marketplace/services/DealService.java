package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

import java.time.LocalDateTime;

/**
 * buyer side deal rules for the my deals screen (step 18)
 * every method checks the signed in buyer owns the deal before changing anything
 */
public final class DealService {

    private DealService() {
        // static service
    }

    /**
     * buyer hands a borrowed item back before the due date
     * the deal becomes RETURNED and the listing goes live again
     */
    public static synchronized Deal returnEarly(String dealId) {
        Deal deal = requireOwnActiveDeal(dealId);
        if (deal.getDealType() != ListingType.LEND) {
            throw new IllegalStateException("only borrowed items can be returned");
        }

        deal.setStatus(DealStatus.RETURNED);
        deal.setCompletedAt(LocalDateTime.now());

        // the item is free again, so the seller sees it as live in my listings
        Listing listing = DummyDataService.getInstance().findListing(deal.getListingId());
        if (listing != null && (listing.getStatus() == ListingStatus.RESERVED
                || listing.getStatus() == ListingStatus.LENT)) {
            listing.setStatus(ListingStatus.LIVE);
            DummyDataService.getInstance().updateListing(listing);
        }
        NotificationService.borrowReturned(deal);
        return deal;
    }

    /**
     * buyer confirms they received a bought or claimed item
     * the deal becomes COMPLETED and is ready to be rated
     */
    public static synchronized Deal markComplete(String dealId) {
        Deal deal = requireOwnActiveDeal(dealId);
        if (deal.getDealType() == ListingType.LEND) {
            throw new IllegalStateException("borrowed items are returned, not completed");
        }

        deal.setStatus(DealStatus.COMPLETED);
        deal.setCompletedAt(LocalDateTime.now());
        deal.setRated(false);

        // the listing keeps its final status, this only fixes one that was never closed
        Listing listing = DummyDataService.getInstance().findListing(deal.getListingId());
        if (listing != null) {
            ListingStatus closed = deal.getDealType() == ListingType.SELL
                    ? ListingStatus.SOLD : ListingStatus.GIVEN;
            if (listing.getStatus() != closed) {
                listing.setStatus(closed);
                DummyDataService.getInstance().updateListing(listing);
            }
        }
        NotificationService.dealCompleted(deal);
        return deal;
    }

    // shared guard: the deal exists, belongs to the signed in buyer and is still active
    private static Deal requireOwnActiveDeal(String dealId) {
        User buyer = SessionManager.requireRole(UserRole.BUYER);
        Deal deal = dealId == null ? null : DummyDataService.getInstance().findDeal(dealId);
        if (deal == null) {
            throw new IllegalArgumentException("that deal no longer exists");
        }
        if (!buyer.getUsername().equalsIgnoreCase(deal.getBuyerUsername())) {
            throw new SecurityException("you can only change your own deals");
        }
        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new IllegalStateException("that deal is no longer active");
        }
        return deal;
    }
}
