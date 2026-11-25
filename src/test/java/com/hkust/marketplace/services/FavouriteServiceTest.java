package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.4 favourite/like rules for the listing detail button and favourites list
 */
class FavouriteServiceTest {

    private final DummyDataService store = DummyDataService.getInstance();

    @BeforeEach
    void setUp() {
        TestState.reset();
    }

    @Test
    void toggleAddsAndRemovesListing() {
        User alice = store.findUser("alice");
        Listing listing = store.findListing("L100");
        assertTrue(store.getFavourites("alice").isEmpty());

        assertTrue(FavouriteService.toggle(alice, listing));
        assertTrue(FavouriteService.isFavourite(alice, listing));
        assertTrue(store.isFavourite("ALICE", listing.getId()));

        assertFalse(FavouriteService.toggle(alice, listing));
        assertFalse(FavouriteService.isFavourite(alice, listing));
    }

    @Test
    void favouritesForReturnsExistingListings() {
        User alice = store.findUser("alice");
        Listing first = store.findListing("L100");
        Listing second = store.findListing("L101");

        FavouriteService.toggle(alice, first);
        FavouriteService.toggle(alice, second);

        List<Listing> saved = FavouriteService.favouritesFor("alice");
        assertEquals(2, saved.size());
        assertTrue(saved.stream().anyMatch(l -> "L100".equals(l.getId())));
        assertTrue(saved.stream().anyMatch(l -> "L101".equals(l.getId())));
        assertTrue(FavouriteService.favouritesFor("bob").isEmpty());
    }

    @Test
    void favouritesIgnoreMissingOrNullInput() {
        User alice = store.findUser("alice");
        Listing listing = store.findListing("L100");

        assertFalse(FavouriteService.toggle(null, listing));
        assertFalse(FavouriteService.toggle(alice, null));
        assertFalse(FavouriteService.isFavourite(null, listing));
        assertFalse(FavouriteService.isFavourite(alice, null));
        assertTrue(FavouriteService.favouritesFor(null).isEmpty());
    }
}
