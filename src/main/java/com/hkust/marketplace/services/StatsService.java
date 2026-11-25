package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * seller dashboard statistics (step 28)
 * everything is computed on the fly from the in-memory store
 */
public final class StatsService {

    private StatsService() {
        // static service
    }

    /**
     * gathers every number the seller stats screen shows for one seller
     */
    public static SellerStats computeForSeller(User seller) {
        if (seller == null) {
            throw new SecurityException("not logged in");
        }
        // stats are private to the seller, buyers and admins are rejected here
        SessionManager.requireRole(UserRole.SELLER);
        if (!seller.getUsername().equalsIgnoreCase(SessionManager.getCurrentUsername())) {
            throw new SecurityException("stats are only visible to the signed in seller");
        }

        DummyDataService store = DummyDataService.getInstance();
        SellerStats stats = new SellerStats();
        stats.setRatingCount(seller.getRatingCount());
        stats.setAverageRating(seller.getRatingCount() == 0 ? 0 : seller.getAverageRating());

        List<Listing> listings = store.getListingsBySeller(seller.getUsername());
        List<Offer> offers = store.getOffersForSeller(seller.getUsername());
        List<Deal> deals = store.getDealsForSeller(seller.getUsername());

        stats.setListingCount(listings.size());
        stats.setTotalViews(0);
        stats.setTotalOffers(offers.size());

        // one row per listing so the bar chart can show views, offers and deals side by side
        Map<String, ListingStat> byListing = new java.util.LinkedHashMap<>();
        for (Listing listing : listings) {
            ListingStat row = new ListingStat();
            row.setListingId(listing.getId());
            row.setTitle(listing.getTitle());
            row.setViews(listing.getViewCount());
            stats.setTotalViews(stats.getTotalViews() + listing.getViewCount());
            stats.getStatusCounts().merge(listing.getStatus(), 1, Integer::sum);
            byListing.put(listing.getId(), row);
        }
        for (Offer offer : offers) {
            ListingStat row = byListing.get(offer.getListingId());
            if (row != null) {
                row.setOffers(row.getOffers() + 1);
            }
        }
        stats.setCompletedDeals(0);
        for (Deal deal : deals) {
            stats.getDealOutcomes().merge(deal.getStatus(), 1, Integer::sum);
            if (deal.getStatus() == DealStatus.COMPLETED || deal.getStatus() == DealStatus.RETURNED) {
                stats.setCompletedDeals(stats.getCompletedDeals() + 1);
            }
            ListingStat row = byListing.get(deal.getListingId());
            if (row != null) {
                row.setDeals(row.getDeals() + 1);
            }
        }
        stats.setPerListing(new ArrayList<>(byListing.values()));
        return stats;
    }

    /**
     * all the numbers behind the seller stats screen
     */
    public static class SellerStats {
        private int listingCount;
        private int totalViews;
        private int totalOffers;
        private int completedDeals;
        private int ratingCount;
        private double averageRating;
        private final Map<DealStatus, Integer> dealOutcomes = new EnumMap<>(DealStatus.class);
        private final Map<ListingStatus, Integer> statusCounts = new EnumMap<>(ListingStatus.class);
        private List<ListingStat> perListing = new ArrayList<>();

        public int getListingCount() { return listingCount; }
        public void setListingCount(int listingCount) { this.listingCount = listingCount; }

        public int getTotalViews() { return totalViews; }
        public void setTotalViews(int totalViews) { this.totalViews = totalViews; }

        public int getTotalOffers() { return totalOffers; }
        public void setTotalOffers(int totalOffers) { this.totalOffers = totalOffers; }

        public int getCompletedDeals() { return completedDeals; }
        public void setCompletedDeals(int completedDeals) { this.completedDeals = completedDeals; }

        public int getRatingCount() { return ratingCount; }
        public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

        public Map<DealStatus, Integer> getDealOutcomes() { return dealOutcomes; }
        public Map<ListingStatus, Integer> getStatusCounts() { return statusCounts; }
        public List<ListingStat> getPerListing() { return perListing; }
        public void setPerListing(List<ListingStat> perListing) { this.perListing = perListing; }
    }

    /**
     * views, offers and deals for one listing, one bar chart row
     */
    public static class ListingStat {
        private String listingId;
        private String title;
        private int views;
        private int offers;
        private int deals;

        public String getListingId() { return listingId; }
        public void setListingId(String listingId) { this.listingId = listingId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getViews() { return views; }
        public void setViews(int views) { this.views = views; }

        public int getOffers() { return offers; }
        public void setOffers(int offers) { this.offers = offers; }

        public int getDeals() { return deals; }
        public void setDeals(int deals) { this.deals = deals; }
    }
}
