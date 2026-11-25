package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.utils.DummyDataService;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * auto-return for overdue borrows (task 1.7)
 * the check runs every second, but the store is only touched on the fx thread
 * a passed dueAt moves the deal to EXPIRED and the listing back to LIVE
 */
public final class BorrowScheduler {

    private static ScheduledExecutorService executor;

    private BorrowScheduler() {
        // static service
    }

    /**
     * starts the one second timer, safe to call twice
     */
    public static synchronized void start() {
        if (executor != null && !executor.isShutdown()) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "borrow-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(() -> {
            try {
                // the in-memory store is shared with the ui, so update it there
                Platform.runLater(() -> checkExpired(LocalDateTime.now()));
            } catch (IllegalStateException ignored) {
                // app is shutting down and the toolkit is already gone
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * finds borrows whose due time has passed and closes them
     * public so the unit tests can call it with a fake clock
     */
    public static void checkExpired(LocalDateTime now) {
        for (Deal deal : DummyDataService.getInstance().getAllDeals()) {
            if (deal.getDealType() != ListingType.LEND
                    || deal.getStatus() != DealStatus.ACTIVE
                    || deal.getDueAt() == null
                    || deal.getDueAt().isAfter(now)) {
                continue;
            }

            deal.setStatus(DealStatus.EXPIRED);
            deal.setCompletedAt(now);

            Listing listing = DummyDataService.getInstance().findListing(deal.getListingId());
            if (listing != null
                    && (listing.getStatus() == ListingStatus.RESERVED
                    || listing.getStatus() == ListingStatus.LENT)) {
                listing.setStatus(ListingStatus.LIVE);
                DummyDataService.getInstance().updateListing(listing);
            }
            NotificationService.borrowExpired(deal);
        }
    }

    /**
     * stops the timer when the app closes
     */
    public static synchronized void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
