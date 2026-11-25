package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OfferServiceEdgeTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    private User login(String username) {
        User user = store.findUser(username);
        SessionManager.login(user);
        return user;
    }

    @Test
    void missingAndAnsweredOffersAreRejected() {
        login("charlie");
        assertThrows(IllegalArgumentException.class, () -> OfferService.acceptOffer("missing"));
        assertThrows(IllegalArgumentException.class, () -> OfferService.declineOffer("missing"));
        assertThrows(IllegalArgumentException.class, () -> OfferService.counterOffer("missing", 10.0, null));

        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 100.0, null, null, null);
        login("charlie");
        OfferService.acceptOffer(offer.getId());

        assertThrows(IllegalStateException.class, () -> OfferService.acceptOffer(offer.getId()));
        assertThrows(IllegalStateException.class, () -> OfferService.declineOffer(offer.getId()));
        assertThrows(IllegalStateException.class, () -> OfferService.counterOffer(offer.getId(), 50.0, null));
    }

    @Test
    void sellerMustOwnTheListingAndListingMustBeLive() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 100.0, null, null, null);

        login("diana");
        assertThrows(SecurityException.class, () -> OfferService.acceptOffer(offer.getId()));
        assertThrows(SecurityException.class, () -> OfferService.declineOffer(offer.getId()));
        assertThrows(SecurityException.class, () -> OfferService.counterOffer(offer.getId(), 80.0, null));

        store.findListing("L100").setStatus(ListingStatus.SOLD);
        login("charlie");
        assertThrows(IllegalStateException.class, () -> OfferService.acceptOffer(offer.getId()));
    }

    @Test
    void giveawayCannotBeAcceptedAsANormalOffer() {
        Offer offer = new Offer();
        offer.setListingId("L102");
        offer.setBuyerUsername("alice");
        offer.setSellerUsername("diana");
        offer.setType(ListingType.GIVEAWAY);
        store.addOffer(offer);

        login("diana");
        assertThrows(IllegalStateException.class, () -> OfferService.acceptOffer(offer.getId()));
        assertThrows(IllegalStateException.class, () -> OfferService.counterOffer(offer.getId(), 10.0, null));
    }

    @Test
    void counterValuesAreValidated() {
        User alice = login("alice");
        Offer sell = OfferService.submitOffer(store.findListing("L100"), alice, 100.0, null, null, null);
        login("charlie");

        assertThrows(IllegalArgumentException.class,
                () -> OfferService.counterOffer(sell.getId(), 0.0, null));
        assertThrows(IllegalArgumentException.class,
                () -> OfferService.counterOffer(sell.getId(), null, null));

        login("alice");
        Offer lend = OfferService.submitOffer(store.findListing("L103"), alice, null,
                LocalDate.now(), LocalDate.now().plusDays(2), null);
        login("diana");
        assertThrows(IllegalArgumentException.class,
                () -> OfferService.counterOffer(lend.getId(), null, LocalDate.now().minusDays(1)));
    }

    @Test
    void acceptCounterNeedsTheRightBuyerAndCounteredStatus() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L101"), alice, 200.0, null, null, null);
        login("charlie");
        OfferService.counterOffer(offer.getId(), 180.0, null);

        login("bob");
        assertThrows(SecurityException.class, () -> OfferService.acceptCounter(offer.getId()));

        login("alice");
        OfferService.acceptCounter(offer.getId());
        assertThrows(IllegalStateException.class, () -> OfferService.acceptCounter(offer.getId()));
    }

    @Test
    void acceptCounterRejectsMissingTermsAndClosedListing() {
        User alice = login("alice");
        Offer offer = OfferService.submitOffer(store.findListing("L100"), alice, 100.0, null, null, null);
        offer.setStatus(OfferStatus.COUNTERED);
        login("alice");
        assertThrows(IllegalStateException.class, () -> OfferService.acceptCounter(offer.getId()));

        Offer lend = new Offer();
        lend.setListingId("L103");
        lend.setBuyerUsername("alice");
        lend.setSellerUsername("diana");
        lend.setType(ListingType.LEND);
        lend.setBorrowStart(LocalDate.now());
        lend.setStatus(OfferStatus.COUNTERED);
        store.addOffer(lend);
        assertThrows(IllegalStateException.class, () -> OfferService.acceptCounter(lend.getId()));

        login("alice");
        Offer closed = OfferService.submitOffer(store.findListing("L101"), alice, 100.0, null, null, null);
        login("charlie");
        OfferService.counterOffer(closed.getId(), 90.0, null);
        store.findListing("L101").setStatus(ListingStatus.SOLD);
        login("alice");
        assertThrows(IllegalStateException.class, () -> OfferService.acceptCounter(closed.getId()));
    }

    @Test
    void staleListingObjectCannotSkipTheStatusCheck() {
        User alice = login("alice");
        var listing = store.findListing("L100");
        listing.setStatus(ListingStatus.SOLD);

        assertThrows(IllegalStateException.class,
                () -> OfferService.submitOffer(listing, alice, 50.0, null, null, null));
    }
}
