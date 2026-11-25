package com.hkust.marketplace.data;

import com.hkust.marketplace.models.ChatMessage;
import com.hkust.marketplace.models.ChatThread;
import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.Report;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * saves the whole in-memory marketplace into sqlite and loads it back
 * the store stays the source of truth while the app is running
 */
public final class PersistenceService {

    private static boolean enabled = !Boolean.getBoolean("marketplace.persistence.disabled");

    private PersistenceService() {
    }

    /**
     * tests disable this so they never touch the real data file
     */
    public static synchronized void setEnabled(boolean value) {
        enabled = value;
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    /**
     * loads the saved store when the database already has users
     * returns false for a fresh database so the caller can seed it
     */
    public static synchronized boolean load(DummyDataService store) {
        if (!enabled) {
            return false;
        }
        try (Connection connection = Database.getConnection()) {
            if (count(connection, "users") == 0) {
                return false;
            }

            DummyDataService.Snapshot snapshot = new DummyDataService.Snapshot();
            loadUsers(connection, snapshot);
            loadListings(connection, snapshot);
            loadOffers(connection, snapshot);
            loadChats(connection, snapshot);
            loadDeals(connection, snapshot);
            loadNotifications(connection, snapshot);
            loadReviews(connection, snapshot);
            loadReports(connection, snapshot);
            loadFavourites(connection, snapshot);
            loadAudit(connection, snapshot);
            store.restore(snapshot);
            return true;
        } catch (SQLException ex) {
            throw new IllegalStateException("could not load the marketplace database", ex);
        }
    }

    /**
     * writes every current row in one transaction
     * this is called when the app closes, not on every click
     */
    public static synchronized void save(DummyDataService store) {
        if (!enabled) {
            return;
        }
        DummyDataService.Snapshot snapshot = store.snapshot();
        try (Connection connection = Database.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                clear(connection);
                insertUsers(connection, snapshot);
                insertListings(connection, snapshot);
                insertOffers(connection, snapshot);
                insertChats(connection, snapshot);
                insertDeals(connection, snapshot);
                insertNotifications(connection, snapshot);
                insertReviews(connection, snapshot);
                insertReports(connection, snapshot);
                insertFavourites(connection, snapshot);
                insertAudit(connection, snapshot);
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("could not save the marketplace database", ex);
        }
    }

    private static int count(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private static void clear(Connection connection) throws SQLException {
        String[] tables = {"users", "listings", "listing_photos", "offers", "chat_threads",
                "chat_messages", "deals", "notifications", "reviews", "reports",
                "favourites", "audit_log"};
        try (Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.executeUpdate("DELETE FROM " + table);
            }
        }
    }

    private static void loadUsers(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setResidenceHall(rs.getString("residence_hall"));
                user.setBio(rs.getString("bio"));
                user.setPreferredMeetup(rs.getString("preferred_meetup"));
                user.setActive(rs.getInt("active") == 1);
                user.setCreatedAt(dateTime(rs.getString("created_at")));
                user.setAverageRating(rs.getDouble("average_rating"));
                user.setRatingCount(rs.getInt("rating_count"));
                user.setVerified(rs.getInt("verified") == 1);
                s.users.put(user.getUsername(), user);
            }
        }
    }

    private static void loadListings(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM listings")) {
            while (rs.next()) {
                Listing listing = new Listing();
                listing.setId(rs.getString("id"));
                listing.setTitle(rs.getString("title"));
                listing.setDescription(rs.getString("description"));
                listing.setCategory(rs.getString("category"));
                listing.setType(ListingType.valueOf(rs.getString("type")));
                listing.setStatus(ListingStatus.valueOf(rs.getString("status")));
                listing.setCondition(rs.getString("condition_text"));
                listing.setPrice(nullableDouble(rs, "price"));
                listing.setLendingDays(nullableInt(rs, "lending_days"));
                listing.setMeetupLocation(rs.getString("meetup_location"));
                listing.setSellerUsername(rs.getString("seller_username"));
                listing.setSellerFullName(rs.getString("seller_full_name"));
                listing.setPostedAt(dateTime(rs.getString("posted_at")));
                listing.setReviewedAt(dateTime(rs.getString("reviewed_at")));
                listing.setRejectionReason(rs.getString("rejection_reason"));
                listing.setViewCount(rs.getInt("view_count"));
                s.listings.put(listing.getId(), listing);
            }
        }
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM listing_photos ORDER BY listing_id, position")) {
            while (rs.next()) {
                Listing listing = s.listings.get(rs.getString("listing_id"));
                if (listing != null) {
                    listing.getPhotoPaths().add(rs.getString("path"));
                }
            }
        }
    }

    private static void loadOffers(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM offers")) {
            while (rs.next()) {
                Offer offer = new Offer();
                offer.setId(rs.getString("id"));
                offer.setListingId(rs.getString("listing_id"));
                offer.setBuyerUsername(rs.getString("buyer_username"));
                offer.setSellerUsername(rs.getString("seller_username"));
                offer.setType(ListingType.valueOf(rs.getString("type")));
                offer.setStatus(OfferStatus.valueOf(rs.getString("status")));
                offer.setOfferPrice(nullableDouble(rs, "offer_price"));
                offer.setBorrowStart(date(rs.getString("borrow_start")));
                offer.setBorrowEnd(date(rs.getString("borrow_end")));
                offer.setMessage(rs.getString("message"));
                offer.setCounterPrice(nullableDouble(rs, "counter_price"));
                offer.setCounterEnd(date(rs.getString("counter_end")));
                offer.setCreatedAt(dateTime(rs.getString("created_at")));
                offer.setRespondedAt(dateTime(rs.getString("responded_at")));
                s.offers.add(offer);
            }
        }
    }

    private static void loadChats(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM chat_threads")) {
            while (rs.next()) {
                ChatThread thread = new ChatThread();
                thread.setId(rs.getString("id"));
                thread.setListingId(rs.getString("listing_id"));
                thread.setListingTitle(rs.getString("listing_title"));
                thread.setBuyerUsername(rs.getString("buyer_username"));
                thread.setSellerUsername(rs.getString("seller_username"));
                thread.setCreatedAt(dateTime(rs.getString("created_at")));
                s.chatThreads.put(thread.getId(), thread);
            }
        }
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM chat_messages")) {
            while (rs.next()) {
                ChatMessage message = new ChatMessage();
                message.setId(rs.getString("id"));
                message.setThreadId(rs.getString("thread_id"));
                message.setSenderUsername(rs.getString("sender_username"));
                message.setContent(rs.getString("content"));
                message.setSentAt(dateTime(rs.getString("sent_at")));
                message.setFormalOffer(rs.getInt("is_formal_offer") == 1);
                message.setOfferId(rs.getString("offer_id"));
                s.chatMessages.add(message);
            }
        }
    }

    private static void loadDeals(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM deals")) {
            while (rs.next()) {
                Deal deal = new Deal();
                deal.setId(rs.getString("id"));
                deal.setListingId(rs.getString("listing_id"));
                deal.setListingTitle(rs.getString("listing_title"));
                deal.setBuyerUsername(rs.getString("buyer_username"));
                deal.setSellerUsername(rs.getString("seller_username"));
                deal.setDealType(ListingType.valueOf(rs.getString("deal_type")));
                deal.setStatus(DealStatus.valueOf(rs.getString("status")));
                deal.setOfferPrice(nullableDouble(rs, "offer_price"));
                deal.setBorrowStart(date(rs.getString("borrow_start")));
                deal.setBorrowEnd(date(rs.getString("borrow_end")));
                deal.setCreatedAt(dateTime(rs.getString("created_at")));
                deal.setCompletedAt(dateTime(rs.getString("completed_at")));
                deal.setDueAt(dateTime(rs.getString("due_at")));
                deal.setRated(rs.getInt("rated") == 1);
                s.deals.add(deal);
            }
        }
    }

    private static void loadNotifications(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM notifications")) {
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getString("id"));
                n.setUsername(rs.getString("username"));
                n.setTitle(rs.getString("title"));
                n.setMessage(rs.getString("message"));
                n.setCategory(rs.getString("category"));
                n.setTimestamp(dateTime(rs.getString("timestamp")));
                n.setRead(rs.getInt("read_flag") == 1);
                s.notifications.add(n);
            }
        }
    }

    private static void loadReviews(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reviews")) {
            while (rs.next()) {
                Review review = new Review();
                review.setId(rs.getString("id"));
                review.setSellerUsername(rs.getString("seller_username"));
                review.setBuyerUsername(rs.getString("buyer_username"));
                review.setListingTitle(rs.getString("listing_title"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));
                review.setSellerReply(rs.getString("seller_reply"));
                review.setHidden(rs.getInt("hidden") == 1);
                review.setCreatedAt(dateTime(rs.getString("created_at")));
                review.setFlagged(rs.getInt("flagged") == 1);
                review.setFlagReason(rs.getString("flag_reason"));
                review.setDealId(rs.getString("deal_id"));
                s.reviews.add(review);
            }
        }
    }

    private static void loadReports(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reports")) {
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getString("id"));
                report.setListingId(rs.getString("listing_id"));
                report.setReporterUsername(rs.getString("reporter_username"));
                report.setReason(rs.getString("reason"));
                report.setCreatedAt(dateTime(rs.getString("created_at")));
                report.setResolved(rs.getInt("resolved") == 1);
                s.reports.add(report);
            }
        }
    }

    private static void loadFavourites(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM favourites")) {
            while (rs.next()) {
                s.favourites.computeIfAbsent(rs.getString("username"), k -> new HashSet<>())
                        .add(rs.getString("listing_id"));
            }
        }
    }

    private static void loadAudit(Connection c, DummyDataService.Snapshot s) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT line FROM audit_log ORDER BY position")) {
            while (rs.next()) {
                s.auditLog.add(rs.getString("line"));
            }
        }
    }

    private static void insertUsers(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO users (username, full_name, password_hash, role, residence_hall, bio, "
                + "preferred_meetup, active, created_at, average_rating, rating_count, verified) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (User u : s.users.values()) {
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getFullName());
                ps.setString(3, u.getPasswordHash());
                ps.setString(4, u.getRole().name());
                ps.setString(5, u.getResidenceHall());
                ps.setString(6, u.getBio());
                ps.setString(7, u.getPreferredMeetup());
                ps.setInt(8, u.isActive() ? 1 : 0);
                ps.setString(9, dateTime(u.getCreatedAt()));
                ps.setDouble(10, u.getAverageRating());
                ps.setInt(11, u.getRatingCount());
                ps.setInt(12, u.isVerified() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertListings(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO listings (id, title, description, category, type, status, condition_text, "
                + "price, lending_days, meetup_location, seller_username, seller_full_name, posted_at, "
                + "reviewed_at, rejection_reason, view_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Listing l : s.listings.values()) {
                ps.setString(1, l.getId());
                ps.setString(2, l.getTitle());
                ps.setString(3, l.getDescription());
                ps.setString(4, l.getCategory());
                ps.setString(5, l.getType().name());
                ps.setString(6, l.getStatus().name());
                ps.setString(7, l.getCondition());
                setNullableDouble(ps, 8, l.getPrice());
                setNullableInt(ps, 9, l.getLendingDays());
                ps.setString(10, l.getMeetupLocation());
                ps.setString(11, l.getSellerUsername());
                ps.setString(12, l.getSellerFullName());
                ps.setString(13, dateTime(l.getPostedAt()));
                ps.setString(14, dateTime(l.getReviewedAt()));
                ps.setString(15, l.getRejectionReason());
                ps.setInt(16, l.getViewCount());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        String photoSql = "INSERT INTO listing_photos (listing_id, position, path) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(photoSql)) {
            for (Listing l : s.listings.values()) {
                int position = 0;
                for (String path : l.getPhotoPaths()) {
                    ps.setString(1, l.getId());
                    ps.setInt(2, position++);
                    ps.setString(3, path);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private static void insertOffers(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO offers (id, listing_id, buyer_username, seller_username, type, status, "
                + "offer_price, borrow_start, borrow_end, message, counter_price, counter_end, created_at, "
                + "responded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Offer o : s.offers) {
                ps.setString(1, o.getId());
                ps.setString(2, o.getListingId());
                ps.setString(3, o.getBuyerUsername());
                ps.setString(4, o.getSellerUsername());
                ps.setString(5, o.getType().name());
                ps.setString(6, o.getStatus().name());
                setNullableDouble(ps, 7, o.getOfferPrice());
                ps.setString(8, date(o.getBorrowStart()));
                ps.setString(9, date(o.getBorrowEnd()));
                ps.setString(10, o.getMessage());
                setNullableDouble(ps, 11, o.getCounterPrice());
                ps.setString(12, date(o.getCounterEnd()));
                ps.setString(13, dateTime(o.getCreatedAt()));
                ps.setString(14, dateTime(o.getRespondedAt()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertChats(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String threadSql = "INSERT INTO chat_threads (id, listing_id, listing_title, buyer_username, "
                + "seller_username, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(threadSql)) {
            for (ChatThread t : s.chatThreads.values()) {
                ps.setString(1, t.getId());
                ps.setString(2, t.getListingId());
                ps.setString(3, t.getListingTitle());
                ps.setString(4, t.getBuyerUsername());
                ps.setString(5, t.getSellerUsername());
                ps.setString(6, dateTime(t.getCreatedAt()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        String messageSql = "INSERT INTO chat_messages (id, thread_id, sender_username, content, sent_at, "
                + "is_formal_offer, offer_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(messageSql)) {
            for (ChatMessage m : s.chatMessages) {
                ps.setString(1, m.getId());
                ps.setString(2, m.getThreadId());
                ps.setString(3, m.getSenderUsername());
                ps.setString(4, m.getContent());
                ps.setString(5, dateTime(m.getSentAt()));
                ps.setInt(6, m.isFormalOffer() ? 1 : 0);
                ps.setString(7, m.getOfferId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertDeals(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO deals (id, listing_id, listing_title, buyer_username, seller_username, "
                + "deal_type, status, offer_price, borrow_start, borrow_end, created_at, completed_at, due_at, "
                + "rated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Deal d : s.deals) {
                ps.setString(1, d.getId());
                ps.setString(2, d.getListingId());
                ps.setString(3, d.getListingTitle());
                ps.setString(4, d.getBuyerUsername());
                ps.setString(5, d.getSellerUsername());
                ps.setString(6, d.getDealType().name());
                ps.setString(7, d.getStatus().name());
                setNullableDouble(ps, 8, d.getOfferPrice());
                ps.setString(9, date(d.getBorrowStart()));
                ps.setString(10, date(d.getBorrowEnd()));
                ps.setString(11, dateTime(d.getCreatedAt()));
                ps.setString(12, dateTime(d.getCompletedAt()));
                ps.setString(13, dateTime(d.getDueAt()));
                ps.setInt(14, d.isRated() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertNotifications(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO notifications (id, username, title, message, category, timestamp, read_flag) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Notification n : s.notifications) {
                ps.setString(1, n.getId());
                ps.setString(2, n.getUsername());
                ps.setString(3, n.getTitle());
                ps.setString(4, n.getMessage());
                ps.setString(5, n.getCategory());
                ps.setString(6, dateTime(n.getTimestamp()));
                ps.setInt(7, n.isRead() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertReviews(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO reviews (id, seller_username, buyer_username, listing_title, rating, "
                + "comment, seller_reply, hidden, created_at, flagged, flag_reason, deal_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Review r : s.reviews) {
                ps.setString(1, r.getId());
                ps.setString(2, r.getSellerUsername());
                ps.setString(3, r.getBuyerUsername());
                ps.setString(4, r.getListingTitle());
                ps.setInt(5, r.getRating());
                ps.setString(6, r.getComment());
                ps.setString(7, r.getSellerReply());
                ps.setInt(8, r.isHidden() ? 1 : 0);
                ps.setString(9, dateTime(r.getCreatedAt()));
                ps.setInt(10, r.isFlagged() ? 1 : 0);
                ps.setString(11, r.getFlagReason());
                ps.setString(12, r.getDealId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertReports(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO reports (id, listing_id, reporter_username, reason, created_at, resolved) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Report r : s.reports) {
                ps.setString(1, r.getId());
                ps.setString(2, r.getListingId());
                ps.setString(3, r.getReporterUsername());
                ps.setString(4, r.getReason());
                ps.setString(5, dateTime(r.getCreatedAt()));
                ps.setInt(6, r.isResolved() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void insertFavourites(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO favourites (username, listing_id) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Map.Entry<String, Set<String>> entry : s.favourites.entrySet()) {
                for (String listingId : entry.getValue()) {
                    ps.setString(1, entry.getKey());
                    ps.setString(2, listingId);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private static void insertAudit(Connection c, DummyDataService.Snapshot s) throws SQLException {
        String sql = "INSERT INTO audit_log (position, line) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int position = 0;
            for (String line : s.auditLog) {
                ps.setInt(1, position++);
                ps.setString(2, line);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static String dateTime(LocalDateTime value) {
        return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static LocalDateTime dateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private static String date(LocalDate value) {
        return value == null ? null : value.toString();
    }

    private static LocalDate date(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static void setNullableDouble(PreparedStatement ps, int index, Double value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.REAL);
        } else {
            ps.setDouble(index, value);
        }
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
