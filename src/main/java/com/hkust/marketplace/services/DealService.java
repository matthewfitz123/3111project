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
 * buyer My Deals actions (task 1.7)
 * return a borrow before it expires, or mark a purchase or claim complete
 * so the listing status and the later review prompt stay in step with the deal
 */
public final class DealService {

    private DealService() {
        // static service
    }

    /**
     * self-return for an active borrow (task 1.7)
     * the deal becomes RETURNED and a reserved or lent listing goes back to LIVE
     */
    public static synchronized void returnEarly(String dealId) {
        User buyer = SessionManager.requireRole(UserRole.BUYER);
        Deal deal = requireBuyerDeal(dealId, buyer);

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new IllegalStateException("this deal is not active");
        }
        if (deal.getDealType() != ListingType.LEND) {
            throw new IllegalStateException("only borrowed items can be returned");
        }

        deal.setStatus(DealStatus.RETURNED);
        deal.setCompletedAt(LocalDateTime.now());

        Listing listing = DummyDataService.getInstance().findListing(deal.getListingId());
        if (listing != null) {
            // the item is available again for other buyers
            if (listing.getStatus() == ListingStatus.RESERVED
                    || listing.getStatus() == ListingStatus.LENT) {
                listing.setStatus(ListingStatus.LIVE);
                DummyDataService.getInstance().updateListing(listing);
            }
        }

        NotificationService.borrowReturned(deal);
        NotificationService.notify(deal.getBuyerUsername(), "Return Recorded",
                "you returned '" + deal.getListingTitle() + "'", "System");
    }

    /**
     * marks a purchase or claim complete (task 1.7)
     * leaves rated false so My Deals can open the rate-seller prompt (task 1.11)
     * the listing stays SOLD or GIVEN
     */
    public static synchronized void markComplete(String dealId) {
        User buyer = SessionManager.requireRole(UserRole.BUYER);
        Deal deal = requireBuyerDeal(dealId, buyer);

        if (deal.getStatus() != DealStatus.ACTIVE) {
            throw new IllegalStateException("this deal is not active");
        }
        if (deal.getDealType() == ListingType.LEND) {
            throw new IllegalStateException("use return early for a borrowed item");
        }

        deal.setStatus(DealStatus.COMPLETED);
        deal.setRated(false);
        deal.setCompletedAt(LocalDateTime.now());
        // the listing stays sold or given, it is not available again
        NotificationService.dealCompleted(deal);
    }

    /**
     * finds a deal and makes sure the signed in buyer actually owns it
     */
    private static Deal requireBuyerDeal(String dealId, User buyer) {
        Deal deal = dealId == null ? null : DummyDataService.getInstance().findDeal(dealId);
        if (deal == null) {
            throw new IllegalArgumentException("that deal no longer exists");
        }
        if (deal.getBuyerUsername() == null
                || !deal.getBuyerUsername().equalsIgnoreCase(buyer.getUsername())) {
            throw new SecurityException("that deal is not yours");
        }
        return deal;
    }
}
