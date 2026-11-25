package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.utils.DummyDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 1.3 browse marketplace card rules: live-only listings, primary photo,
 * seller rating, verified badge and availability label
 */
class BrowseServiceTest {

    @BeforeEach
    void setUp() {
        TestState.reset();
    }

    @Test
    void liveListingsOnlyContainsApprovedLiveListings() {
        List<Listing> live = BrowseService.liveListings();

        assertEquals(4, live.size());
        assertTrue(live.stream().allMatch(l -> l.getStatus() == ListingStatus.LIVE));
        assertTrue(live.stream().noneMatch(l -> "Mini Fridge".equals(l.getTitle())));
    }

    @Test
    void primaryPhotoUsesFirstUsablePath() {
        Listing listing = listing("charlie", ListingStatus.LIVE);
        listing.getPhotoPaths().add(null);
        listing.getPhotoPaths().add("  ");
        listing.getPhotoPaths().add("/images/uploads/a.png");
        listing.getPhotoPaths().add("/images/uploads/b.png");

        assertEquals("/images/uploads/a.png", BrowseService.primaryPhotoPath(listing));

        listing.getPhotoPaths().clear();
        assertNull(BrowseService.primaryPhotoPath(listing));
        assertNull(BrowseService.primaryPhotoPath(null));
    }

    @Test
    void detailPhotosAreCappedAtSix() {
        Listing listing = listing("charlie", ListingStatus.LIVE);
        for (int i = 0; i < 8; i++) {
            listing.getPhotoPaths().add("/images/uploads/" + i + ".png");
        }

        List<String> paths = BrowseService.photoPaths(listing);

        assertEquals(6, paths.size());
        assertEquals("/images/uploads/0.png", paths.get(0));
        assertEquals("/images/uploads/5.png", paths.get(5));
    }

    @Test
    void sellerRatingLabelUsesSellerAccount() {
        DummyDataService.getInstance().findUser("charlie").setAverageRating(4.7);
        DummyDataService.getInstance().findUser("charlie").setRatingCount(12);

        assertEquals("4.7 ★ (12)", BrowseService.sellerRatingLabel(listing("charlie", ListingStatus.LIVE)));
        assertEquals("No ratings yet", BrowseService.sellerRatingLabel(listing("ghost", ListingStatus.LIVE)));
    }

    @Test
    void verifiedBadgeOnlyForVerifiedSellers() {
        assertTrue(BrowseService.showVerifiedBadge(listing("charlie", ListingStatus.LIVE)));
        assertTrue(BrowseService.showVerifiedBadge(listing("diana", ListingStatus.LIVE)));
        assertFalse(BrowseService.showVerifiedBadge(listing("alice", ListingStatus.LIVE)));
        assertFalse(BrowseService.showVerifiedBadge(listing("ghost", ListingStatus.LIVE)));
    }

    @Test
    void availabilityLabelMarksLiveListingsAvailable() {
        assertEquals("Available", BrowseService.availabilityLabel(listing("charlie", ListingStatus.LIVE)));
        assertEquals("Reserved", BrowseService.availabilityLabel(listing("charlie", ListingStatus.RESERVED)));
        assertEquals("Unavailable", BrowseService.availabilityLabel(null));
    }

    @Test
    void displayLabelsMatchListingType() {
        Listing sell = listing("charlie", ListingStatus.LIVE);
        sell.setType(ListingType.SELL);
        sell.setPrice(180.0);
        assertEquals("For Sale", sell.getTypeDisplay());
        assertEquals("HK$ 180", sell.getPriceDisplay());

        Listing lend = listing("diana", ListingStatus.LIVE);
        lend.setType(ListingType.LEND);
        assertEquals("For Lending", lend.getTypeDisplay());
        assertEquals("Borrow", lend.getPriceDisplay());

        Listing giveaway = listing("diana", ListingStatus.LIVE);
        giveaway.setType(ListingType.GIVEAWAY);
        assertEquals("Giveaway", giveaway.getTypeDisplay());
        assertEquals("Free", giveaway.getPriceDisplay());
    }

    private Listing listing(String sellerUsername, ListingStatus status) {
        Listing listing = new Listing();
        listing.setSellerUsername(sellerUsername);
        listing.setStatus(status);
        return listing;
    }
}
