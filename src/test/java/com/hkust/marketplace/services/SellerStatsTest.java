package com.hkust.marketplace.services;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * task 2.9 numbers behind the seller bar and pie charts
 */
class SellerStatsTest {

    @BeforeEach
    void reset() {
        TestState.reset();
    }

    @Test
    void statsCountViewsOffersAndStatusSlices() {
        DummyDataService store = DummyDataService.getInstance();
        User charlie = store.findUser("charlie");
        SessionManager.login(charlie);
        int before = store.findListing("L100").getViewCount();
        store.incrementViewCount("L100");

        StatsService.SellerStats stats = StatsService.computeForSeller(charlie);

        assertTrue(stats.getListingCount() >= 1);
        assertTrue(stats.getTotalViews() >= before + 1);
        assertTrue(stats.getTotalOffers() >= 0);
        assertFalse(stats.getStatusCounts().isEmpty());
        assertFalse(stats.getPerListing().isEmpty());
        assertEquals(before + 1, stats.getPerListing().stream()
                .filter(row -> "L100".equals(row.getListingId()))
                .findFirst().orElseThrow().getViews());
    }
}
