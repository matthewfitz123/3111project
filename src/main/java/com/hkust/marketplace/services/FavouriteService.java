package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;

import java.util.ArrayList;
import java.util.List;

/**
 * buyer favourite/like rules for listing detail and the favourites screen (task 1.4)
 * stores favourite listing ids per user in the in-memory data store
 */
public final class FavouriteService {

    private FavouriteService() {
        // static service
    }

    /**
     * toggles a listing in the user's favourites and returns the new saved state
     */
    public static boolean toggle(User user, Listing listing) {
        if (user == null || listing == null || listing.getId() == null) {
            return false;
        }
        return DummyDataService.getInstance().toggleFavourite(user.getUsername(), listing.getId());
    }

    /**
     * true when the listing is currently saved by the user
     */
    public static boolean isFavourite(User user, Listing listing) {
        if (user == null || listing == null || listing.getId() == null) {
            return false;
        }
        return DummyDataService.getInstance().isFavourite(user.getUsername(), listing.getId());
    }

    /**
     * returns the favourited listings that still exist for one user
     */
    public static List<Listing> favouritesFor(String username) {
        List<Listing> result = new ArrayList<>();
        DummyDataService store = DummyDataService.getInstance();
        for (String listingId : store.getFavourites(username)) {
            Listing listing = store.findListing(listingId);
            if (listing != null) {
                result.add(listing);
            }
        }
        return result;
    }
}
