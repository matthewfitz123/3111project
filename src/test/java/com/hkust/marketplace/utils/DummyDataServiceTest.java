package com.hkust.marketplace.utils;

import com.hkust.marketplace.TestState;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.Report;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyDataServiceTest {

    private DummyDataService store;

    @BeforeEach
    void reset() {
        TestState.reset();
        store = DummyDataService.getInstance();
    }

    @Test
    void seededAccountsAreHashedAndHaveTheRightRoles() {
        User alice = store.findUser("alice");
        User charlie = store.findUser("charlie");
        User admin = store.findUser("admin");

        assertEquals(UserRole.BUYER, alice.getRole());
        assertEquals(UserRole.SELLER, charlie.getRole());
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(alice.getPasswordHash().startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verify("Password12", alice.getPasswordHash()));
        assertTrue(PasswordUtil.verify("Admin12345", admin.getPasswordHash()));
    }

    @Test
    void onlyLiveListingsShowInTheStoreBrowseList() {
        List<Listing> live = store.getLiveListings();

        assertEquals(4, live.size());
        assertTrue(live.stream().allMatch(l -> l.getStatus() == ListingStatus.LIVE));
        assertFalse(live.stream().anyMatch(l -> l.getId().equals("L104")));
        assertEquals(1, store.getPendingListings().size());
    }

    @Test
    void sellerListingsAreFilteredByOwner() {
        List<Listing> charlie = store.getListingsBySeller("charlie");
        List<Listing> diana = store.getListingsBySeller("diana");

        assertEquals(2, charlie.size());
        assertTrue(charlie.stream().allMatch(l -> l.getSellerUsername().equals("charlie")));
        assertEquals(3, diana.size());
    }

    @Test
    void viewCountCanBeIncremented() {
        int before = store.findListing("L100").getViewCount();

        store.incrementViewCount("L100");
        store.incrementViewCount("missing-listing");

        assertEquals(before + 1, store.findListing("L100").getViewCount());
    }

    @Test
    void notificationsCanBeMarkedRead() {
        List<Notification> notifications = store.getNotificationsFor("alice");
        assertFalse(notifications.isEmpty());
        assertTrue(store.getUnreadNotificationCount("alice") > 0);

        store.markNotificationRead(notifications.get(0).getId());
        assertTrue(notifications.get(0).isRead());

        store.markAllNotificationsRead("alice");
        assertEquals(0, store.getUnreadNotificationCount("alice"));
    }

    @Test
    void reportsCanBeAddedAndResolved() {
        Report report = new Report();
        report.setListingId("L100");
        report.setReporterUsername("alice");
        report.setReason("test reason");

        store.addReport(report);

        assertNotNull(report.getId());
        assertEquals(1, store.getReports().size());
        store.markReportResolved(report.getId());
        assertTrue(store.getReports().get(0).isResolved());
    }

    @Test
    void usersCanBeDeactivatedAndAudited() {
        store.deactivateUser("bob");
        assertFalse(store.findUser("bob").isActive());

        store.reactivateUser("bob");
        assertTrue(store.findUser("bob").isActive());

        store.logAudit("admin", "TEST", "detail");
        assertTrue(store.getAuditLog().get(0).contains("admin"));
        assertTrue(store.getAuditLog().get(0).contains("TEST"));
    }

    @Test
    void aNewListingStartsAsPendingReview() {
        Listing listing = new Listing();
        listing.setTitle("Test book");
        listing.setDescription("test");
        listing.setCategory("Textbooks");
        listing.setType(ListingType.SELL);
        listing.setCondition("Used");
        listing.setPrice(50.0);
        listing.setMeetupLocation("Library");
        listing.setSellerUsername("charlie");
        listing.setSellerFullName("Charlie Lee");

        store.addListing(listing);

        assertNotNull(listing.getId());
        assertEquals(ListingStatus.PENDING_REVIEW, listing.getStatus());
        assertEquals(1, store.getListingsBySeller("charlie").stream()
                .filter(l -> l.getId().equals(listing.getId())).count());
    }
}
