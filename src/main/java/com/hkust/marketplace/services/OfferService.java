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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * offer, borrow request and giveaway claim rules (step 15)
 * both public methods are synchronized and re-check the listing status
 * inside the lock so two buyers cannot both win a giveaway
 */
public final class OfferService {

    private OfferService() {
        // static service
    }

    /**
     * creates a price offer for a SELL listing or a borrow request for LEND
     *
     * @param listing the listing the buyer clicked
     * @param buyer   the signed in buyer
     * @param price   price for SELL, ignored for LEND
     * @param start   pickup date for LEND, ignored for SELL
     * @param end     return date for LEND, ignored for SELL
     * @param note    optional message for the seller
     * @return the pending offer
     */
    public static synchronized Offer submitOffer(Listing listing, User buyer, Double price,
                                                 LocalDate start, LocalDate end, String note) {
        requireRealBuyer(buyer);
        Listing fresh = requireLiveListing(listing);

        if (fresh.getType() == ListingType.GIVEAWAY) {
            throw new IllegalStateException("giveaways are claimed instead of offered");
        }

        Offer offer = baseOffer(fresh, buyer);
        offer.setMessage(note == null || note.isBlank() ? null : note.trim());

        if (fresh.getType() == ListingType.SELL) {
            if (price == null || !Double.isFinite(price) || price <= 0) {
                throw new IllegalArgumentException("enter a price above zero");
            }
            offer.setOfferPrice(price);
        } else if (fresh.getType() == ListingType.LEND) {
            if (start == null || end == null) {
                throw new IllegalArgumentException("pick both a start and end date");
            }
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("the end date cannot be before the start date");
            }
            // the demo uses local dates, so same-day is the smallest period we can show
            offer.setBorrowStart(start);
            offer.setBorrowEnd(end);
        }

        offer.setStatus(OfferStatus.PENDING);
        DummyDataService.getInstance().addOffer(offer);
        NotificationService.offerReceived(offer);
        return offer;
    }

    /**
     * claims a giveaway in one atomic step
     * the first buyer wins, everyone after that gets a no longer available error
     */
    public static synchronized Offer claimGiveaway(Listing listing, User buyer) {
        requireRealBuyer(buyer);
        Listing fresh = requireLiveListing(listing);
        if (fresh.getType() != ListingType.GIVEAWAY) {
            throw new IllegalStateException("this listing is not a giveaway");
        }

        // the status change happens inside this synchronized method on purpose
        fresh.setStatus(ListingStatus.GIVEN);
        DummyDataService.getInstance().updateListing(fresh);

        Offer offer = baseOffer(fresh, buyer);
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        DummyDataService.getInstance().addOffer(offer);

        Deal deal = new Deal();
        deal.setListingId(fresh.getId());
        deal.setListingTitle(fresh.getTitle());
        deal.setBuyerUsername(buyer.getUsername());
        deal.setSellerUsername(fresh.getSellerUsername());
        deal.setDealType(ListingType.GIVEAWAY);
        deal.setStatus(DealStatus.ACTIVE);
        DummyDataService.getInstance().addDeal(deal);

        NotificationService.offerReceived(offer);
        NotificationService.notify(buyer.getUsername(), "Claim Confirmed",
                "you claimed '" + fresh.getTitle() + "'", "Offer");
        return offer;
    }

    // shared setup for both offer types
    private static Offer baseOffer(Listing listing, User buyer) {
        Offer offer = new Offer();
        offer.setListingId(listing.getId());
        offer.setBuyerUsername(buyer.getUsername());
        offer.setSellerUsername(listing.getSellerUsername());
        offer.setType(listing.getType());
        return offer;
    }

    // only the logged in buyer can act, not somebody passing another user object
    private static void requireRealBuyer(User buyer) {
        User current = SessionManager.requireRole(UserRole.BUYER);
        if (buyer == null || !buyer.getUsername().equalsIgnoreCase(current.getUsername())) {
            throw new SecurityException("you can only act as yourself");
        }
    }

    // always re-read the store so a stale listing object cannot skip the status check
    private static Listing requireLiveListing(Listing listing) {
        if (listing == null || listing.getId() == null) {
            throw new IllegalStateException("that listing no longer exists");
        }
        Listing fresh = DummyDataService.getInstance().findListing(listing.getId());
        if (fresh == null || fresh.getStatus() != ListingStatus.LIVE) {
            throw new IllegalStateException("that listing is no longer available");
        }
        return fresh;
    }
}
