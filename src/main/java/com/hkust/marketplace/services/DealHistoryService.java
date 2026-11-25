package com.hkust.marketplace.services;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * read-only deal history queries and display helpers (step 25)
 * shared by the buyer history screen and the admin all-deals screen
 */
public final class DealHistoryService {

    private DealHistoryService() {
        // static service
    }

    /**
     * completed deals for one buyer, newest first
     */
    public static List<Deal> buyerHistory(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return DummyDataService.getInstance().getDealsForBuyer(username).stream()
                .filter(DealHistoryService::isCompletedDeal)
                .sorted(Comparator.comparing(DealHistoryService::sortKey,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /**
     * completed deals for the buyer who is currently signed in
     */
    public static List<Deal> buyerHistoryForCurrentUser() {
        User user = SessionManager.requireRole(UserRole.BUYER);
        return buyerHistory(user.getUsername());
    }

    /**
     * every deal in the system, newest first
     */
    public static List<Deal> allDeals() {
        return DummyDataService.getInstance().getAllDeals().stream()
                .sorted(Comparator.comparing(DealHistoryService::sortKey,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /**
     * every deal for the admin who is currently signed in
     */
    public static List<Deal> allDealsForAdmin() {
        SessionManager.requireRole(UserRole.ADMIN);
        return allDeals();
    }

    /**
     * buyer history only shows deals that finished successfully
     */
    public static boolean isCompletedDeal(Deal deal) {
        return deal != null
                && (deal.getStatus() == DealStatus.COMPLETED
                || deal.getStatus() == DealStatus.RETURNED);
    }

    public static String typeLabel(Deal deal) {
        return deal == null ? "Unknown" : typeLabel(deal.getDealType());
    }

    public static String typeLabel(ListingType type) {
        if (type == null) {
            return "Unknown";
        }
        return switch (type) {
            case SELL -> "Bought";
            case LEND -> "Borrowed";
            case GIVEAWAY -> "Received";
        };
    }

    /**
     * admin-facing deal type, from the marketplace outcome point of view
     */
    public static String adminTypeLabel(Deal deal) {
        return deal == null ? "Unknown" : adminTypeLabel(deal.getDealType());
    }

    public static String adminTypeLabel(ListingType type) {
        if (type == null) {
            return "Unknown";
        }
        return switch (type) {
            case SELL -> "Sold";
            case LEND -> "Lent";
            case GIVEAWAY -> "Given";
        };
    }

    public static String statusLabel(Deal deal) {
        return deal == null ? "Unknown" : statusLabel(deal.getStatus());
    }

    public static String statusLabel(DealStatus status) {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case PENDING -> "Pending";
            case ACTIVE -> "Active";
            case COMPLETED -> "Completed";
            case RETURNED -> "Returned";
            case CANCELLED -> "Cancelled";
            case EXPIRED -> "Expired";
        };
    }

    /**
     * category comes from the listing because deals do not store it
     */
    public static String category(Deal deal) {
        if (deal == null || deal.getListingId() == null) {
            return "Unknown";
        }
        Listing listing = DummyDataService.getInstance().findListing(deal.getListingId());
        if (listing == null || listing.getCategory() == null || listing.getCategory().isBlank()) {
            return "Unknown";
        }
        return listing.getCategory();
    }

    public static String sellerDisplay(Deal deal) {
        if (deal == null) {
            return "Unknown";
        }
        String username = deal.getSellerUsername();
        String fullName = fullName(username);
        if (!fullName.isEmpty() && username != null && !username.isBlank()) {
            return fullName + " (@" + username + ")";
        }
        return username == null || username.isBlank() ? "Unknown" : username;
    }

    /**
     * lend deals start on the borrow start date, everything else on createdAt
     */
    public static LocalDate startDate(Deal deal) {
        if (deal == null) {
            return null;
        }
        if (deal.getDealType() == ListingType.LEND && deal.getBorrowStart() != null) {
            return deal.getBorrowStart();
        }
        if (deal.getCreatedAt() != null) {
            return deal.getCreatedAt().toLocalDate();
        }
        return deal.getBorrowStart();
    }

    public static LocalDate endDate(Deal deal) {
        return deal == null || deal.getCompletedAt() == null
                ? null : deal.getCompletedAt().toLocalDate();
    }

    /**
     * date used for history filtering and sorting: completion time when present
     */
    public static LocalDate historyDate(Deal deal) {
        LocalDateTime dateTime = sortKey(deal);
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    /**
     * shared filter predicate for the buyer and admin history tables
     */
    public static boolean matches(Deal deal, LocalDate from, LocalDate to, String category,
                                  String type, String status, String search, boolean adminView) {
        if (deal == null) {
            return false;
        }
        LocalDate date = historyDate(deal);
        if (from != null && (date == null || date.isBefore(from))) {
            return false;
        }
        if (to != null && (date == null || date.isAfter(to))) {
            return false;
        }
        if (hasFilter(category) && !"All Categories".equalsIgnoreCase(category)
                && !category(deal).equalsIgnoreCase(category)) {
            return false;
        }
        if (hasFilter(type) && !"All Types".equalsIgnoreCase(type)) {
            String label = adminView ? adminTypeLabel(deal) : typeLabel(deal);
            if (!label.equalsIgnoreCase(type)) {
                return false;
            }
        }
        if (hasFilter(status) && !"All Statuses".equalsIgnoreCase(status)
                && !statusLabel(deal).equalsIgnoreCase(status)) {
            return false;
        }
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        if (!query.isEmpty() && !searchableText(deal, adminView).contains(query)) {
            return false;
        }
        return true;
    }

    private static LocalDateTime sortKey(Deal deal) {
        if (deal == null) {
            return null;
        }
        return deal.getCompletedAt() != null ? deal.getCompletedAt() : deal.getCreatedAt();
    }

    private static boolean hasFilter(String value) {
        return value != null && !value.isBlank();
    }

    private static String searchableText(Deal deal, boolean adminView) {
        StringBuilder text = new StringBuilder();
        append(text, deal.getListingTitle());
        append(text, deal.getSellerUsername());
        append(text, fullName(deal.getSellerUsername()));
        if (adminView) {
            append(text, deal.getBuyerUsername());
            append(text, fullName(deal.getBuyerUsername()));
        }
        return text.toString().toLowerCase(Locale.ROOT);
    }

    private static void append(StringBuilder text, String value) {
        if (value != null && !value.isBlank()) {
            text.append(value).append(' ');
        }
    }

    private static String fullName(String username) {
        if (username == null || username.isBlank()) {
            return "";
        }
        User user = DummyDataService.getInstance().findUser(username);
        return user != null && user.getFullName() != null ? user.getFullName() : "";
    }
}
