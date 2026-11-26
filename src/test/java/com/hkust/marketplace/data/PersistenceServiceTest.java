package com.hkust.marketplace.data;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceServiceTest {

    @TempDir
    Path tempDir;

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
        Database.setUrlForTests("jdbc:sqlite:" + tempDir.resolve("marketplace-test.db"));
        PersistenceService.setEnabled(true);
    }

    @AfterEach
    void disable() {
        PersistenceService.setEnabled(false);
    }

    @Test
    void seededStoreSurvivesSaveAndReload() {
        PersistenceService.save(store);

        store.resetForTests();
        assertTrue(PersistenceService.load(store));

        User alice = store.findUser("alice");
        assertNotNull(alice);
        assertEquals("Alice Chan", alice.getFullName());
        assertTrue(alice.getPasswordHash().startsWith("pbkdf2$"));
        assertEquals(4, store.getLiveListings().size());
        assertTrue(store.getNotificationsFor("alice").size() > 0);
    }

    @Test
    void newlyAddedListingIsWrittenToTheDatabase() {
        Listing listing = new Listing();
        listing.setTitle("Database test listing");
        listing.setDescription("saved through sqlite");
        listing.setCategory("Textbooks");
        listing.setType(ListingType.SELL);
        listing.setCondition("Used");
        listing.setPrice(30.0);
        listing.setMeetupLocation("Library");
        listing.setSellerUsername("charlie");
        listing.setSellerFullName("Charlie Lee");
        store.addListing(listing);

        String id = listing.getId();
        store.resetForTests();
        assertTrue(PersistenceService.load(store));

        Listing loaded = store.findListing(id);
        assertNotNull(loaded);
        assertEquals("Database test listing", loaded.getTitle());
        assertEquals(ListingStatus.PENDING_REVIEW, loaded.getStatus());
        assertEquals(30.0, loaded.getPrice());
    }

    @Test
    void clearKeepsOnlyTheAdminAndRefillRestoresDemoData() {
        Listing extra = new Listing();
        extra.setTitle("Temporary");
        extra.setType(ListingType.SELL);
        extra.setSellerUsername("charlie");
        store.addListing(extra);

        store.clearKeepingAdmin();
        assertEquals(1, store.getAllUsers().size());
        assertNotNull(store.findUser("admin"));
        assertTrue(PasswordUtil.verify("Admin12345", store.findUser("admin").getPasswordHash()));
        assertTrue(store.getLiveListings().isEmpty());

        store.resetForTests();
        assertTrue(PersistenceService.load(store));
        assertEquals(1, store.getAllUsers().size());
        assertNull(store.findListing(extra.getId()));
        assertTrue(store.getAllDeals().isEmpty());
        assertTrue(store.getNotificationsFor("alice").isEmpty());

        store.refillDemoData();
        store.resetForTests();
        assertTrue(PersistenceService.load(store));
        assertNotNull(store.findUser("alice"));
        assertNotNull(store.findUser("charlie"));
        assertEquals(4, store.getLiveListings().size());
        assertNotNull(store.findUser("admin"));
    }
}
