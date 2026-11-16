package com.hkust.marketplace.utils;

import com.hkust.marketplace.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * in-memory data store for the frontend demo
 * all data is lost when the application closes
 * todo: replace this service with real repositories / api clients
 */
public class DummyDataService {

    private static final DummyDataService INSTANCE = new DummyDataService();

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Listing> listings = new LinkedHashMap<>();
    private final List<Deal> deals = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();

    // step 3 additions, all in memory like everything else here
    private final List<Offer> offers = new ArrayList<>();
    private final Map<String, ChatThread> chatThreads = new LinkedHashMap<>();
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final List<Report> reports = new ArrayList<>();
    // each line looks like "timestamp | actor | action | detail"
    private final List<String> auditLog = new ArrayList<>();

    private final AtomicInteger listingIdCounter = new AtomicInteger(100);
    private final AtomicInteger dealIdCounter = new AtomicInteger(500);
    private final AtomicInteger notifIdCounter = new AtomicInteger(1);
    private final AtomicInteger reviewIdCounter = new AtomicInteger(1);
    private final AtomicInteger offerIdCounter = new AtomicInteger(1);
    private final AtomicInteger chatIdCounter = new AtomicInteger(1);
    private final AtomicInteger reportIdCounter = new AtomicInteger(1);

    // predefined hkust locations
    public static final List<String> HKUST_LOCATIONS = List.of(
            "Academic Building (LG1)",
            "Library",
            "Student Centre",
            "Hall 1 Lobby",
            "Hall 2 Lobby",
            "Hall 3 Lobby",
            "Hall 4 Lobby",
            "Hall 5 Lobby",
            "Hall 6 Lobby",
            "Hall 7 Lobby",
            "Hall 8 Lobby",
            "Hall 9 Lobby",
            "UG Hall 1",
            "UG Hall 2",
            "North Gate",
            "South Gate",
            "Shaw Auditorium",
            "Coffee Shop (LG5)",
            "Sports Hall"
    );

    public static final List<String> CATEGORIES = List.of(
            "Textbooks", "Electronics", "Furniture", "Clothing",
            "Stationery", "Sports Equipment", "Kitchenware", "Others"
    );

    public static final List<String> CONDITIONS = List.of(
            "New", "Like New", "Used", "For Parts"
    );

    private DummyDataService() {
        seedData();
    }

    public static DummyDataService getInstance() {
        return INSTANCE;
    }

    private void seedData() {
        // demo passwords are hashed so they match the real registration code
        // seeded admin account
        User admin = new User("admin", "System Admin", PasswordUtil.hash("Admin12345"), UserRole.ADMIN);
        admin.setFullName("HKUST Marketplace Admin");
        users.put("admin", admin);

        // demo buyers
        User buyer1 = new User("alice", "Alice Chan", PasswordUtil.hash("Password12"), UserRole.BUYER);
        buyer1.setResidenceHall("Hall 3");
        users.put("alice", buyer1);

        User buyer2 = new User("bob", "Bob Wong", PasswordUtil.hash("Password12"), UserRole.BUYER);
        buyer2.setResidenceHall("Off-campus");
        users.put("bob", buyer2);

        // demo sellers
        User seller1 = new User("charlie", "Charlie Lee", PasswordUtil.hash("Password12"), UserRole.SELLER);
        seller1.setBio("Final year CSE student selling textbooks and gadgets.");
        seller1.setPreferredMeetup("Library");
        seller1.setAverageRating(4.7);
        seller1.setRatingCount(12);
        users.put("charlie", seller1);

        User seller2 = new User("diana", "Diana Ng", PasswordUtil.hash("Password12"), UserRole.SELLER);
        seller2.setBio("Selling furniture and kitchen items before graduation.");
        seller2.setPreferredMeetup("Hall 5 Lobby");
        seller2.setAverageRating(4.3);
        seller2.setRatingCount(8);
        users.put("diana", seller2);

        // demo listings
        Listing l1 = createListing("Calculus Textbook (Stewart)", "Barely used, 8th edition. Perfect for MATH 1013.",
                "Textbooks", ListingType.SELL, "Like New", 180.0, null, "Library", "charlie", "Charlie Lee");
        l1.setStatus(ListingStatus.LIVE);
        listings.put(l1.getId(), l1);

        Listing l2 = createListing("MacBook Pro Charger 61W", "Original Apple charger, works perfectly.",
                "Electronics", ListingType.SELL, "Used", 250.0, null, "Academic Building (LG1)", "charlie", "Charlie Lee");
        l2.setStatus(ListingStatus.LIVE);
        listings.put(l2.getId(), l2);

        Listing l3 = createListing("IKEA Desk Lamp", "Good condition, LED bulb included.",
                "Furniture", ListingType.GIVEAWAY, "Used", null, null, "Hall 5 Lobby", "diana", "Diana Ng");
        l3.setStatus(ListingStatus.LIVE);
        listings.put(l3.getId(), l3);

        Listing l4 = createListing("Graphing Calculator TI-84", "Can lend for one semester.",
                "Electronics", ListingType.LEND, "Like New", null, 90, "Student Centre", "diana", "Diana Ng");
        l4.setStatus(ListingStatus.LIVE);
        listings.put(l4.getId(), l4);

        Listing l5 = createListing("Mini Fridge", "Works well, cleaning required.",
                "Furniture", ListingType.SELL, "Used", 400.0, null, "Hall 2 Lobby", "diana", "Diana Ng");
        l5.setStatus(ListingStatus.PENDING_REVIEW);
        listings.put(l5.getId(), l5);

        // demo deals
        Deal d1 = new Deal();
        d1.setId("D" + dealIdCounter.getAndIncrement());
        d1.setListingId(l1.getId());
        d1.setListingTitle(l1.getTitle());
        d1.setBuyerUsername("alice");
        d1.setSellerUsername("charlie");
        d1.setDealType(ListingType.SELL);
        d1.setStatus(DealStatus.ACTIVE);
        d1.setOfferPrice(160.0);
        deals.add(d1);

        // demo notifications
        notifications.add(new Notification("alice", "Offer Accepted",
                "Charlie accepted your offer of HK$160 for Calculus Textbook.", "Offer"));
        notifications.add(new Notification("charlie", "New Offer Received",
                "Alice offered HK$160 for your Calculus Textbook.", "Offer"));
        notifications.add(new Notification("diana", "Listing Pending",
                "Your Mini Fridge listing is awaiting admin review.", "System"));
        notifications.add(new Notification("admin", "New Listing Submitted",
                "Diana submitted 'Mini Fridge' for review.", "System"));

        // demo review
        Review r1 = new Review();
        r1.setId("R" + reviewIdCounter.getAndIncrement());
        r1.setSellerUsername("charlie");
        r1.setBuyerUsername("bob");
        r1.setListingTitle("Old Chemistry Book");
        r1.setRating(5);
        r1.setComment("Very smooth transaction, book in excellent condition!");
        reviews.add(r1);
    }

    private Listing createListing(String title, String desc, String cat, ListingType type,
                                  String condition, Double price, Integer days,
                                  String meetup, String sellerUser, String sellerName) {
        Listing l = new Listing();
        l.setId("L" + listingIdCounter.getAndIncrement());
        l.setTitle(title);
        l.setDescription(desc);
        l.setCategory(cat);
        l.setType(type);
        l.setCondition(condition);
        l.setPrice(price);
        l.setLendingDays(days);
        l.setMeetupLocation(meetup);
        l.setSellerUsername(sellerUser);
        l.setSellerFullName(sellerName);
        l.setPostedAt(LocalDateTime.now().minusDays(new Random().nextInt(10)));
        return l;
    }

    // ========== user methods ==========
    public boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public User findUser(String username) {
        return users.get(username.toLowerCase());
    }

    public void registerUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public List<User> getUsersByRole(UserRole role) {
        return users.values().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }

    // ========== listing methods ==========
    public Collection<Listing> getAllListings() {
        return listings.values();
    }

    public List<Listing> getLiveListings() {
        return listings.values().stream()
                .filter(l -> l.getStatus() == ListingStatus.LIVE)
                .collect(Collectors.toList());
    }

    public List<Listing> getPendingListings() {
        return listings.values().stream()
                .filter(l -> l.getStatus() == ListingStatus.PENDING_REVIEW)
                .collect(Collectors.toList());
    }

    public List<Listing> getListingsBySeller(String username) {
        return listings.values().stream()
                .filter(l -> l.getSellerUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public Listing findListing(String id) {
        return listings.get(id);
    }

    public void addListing(Listing listing) {
        if (listing.getId() == null) {
            listing.setId("L" + listingIdCounter.getAndIncrement());
        }
        listings.put(listing.getId(), listing);
    }

    public void updateListing(Listing listing) {
        listings.put(listing.getId(), listing);
    }

    public void removeListing(String id) {
        listings.remove(id);
    }

    // ========== deal methods ==========
    public List<Deal> getDealsForBuyer(String username) {
        return deals.stream()
                .filter(d -> d.getBuyerUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public List<Deal> getAllDeals() {
        return new ArrayList<>(deals);
    }

    public void addDeal(Deal deal) {
        if (deal.getId() == null) {
            deal.setId("D" + dealIdCounter.getAndIncrement());
        }
        deals.add(deal);
    }

    // ========== notification methods ==========
    public List<Notification> getNotificationsFor(String username) {
        return notifications.stream()
                .filter(n -> n.getUsername().equalsIgnoreCase(username))
                .sorted(Comparator.comparing(Notification::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public void addNotification(Notification n) {
        if (n.getId() == null) {
            n.setId("N" + notifIdCounter.getAndIncrement());
        }
        notifications.add(n);
    }

    // ========== review methods ==========
    public List<Review> getReviewsForSeller(String username) {
        return reviews.stream()
                .filter(r -> r.getSellerUsername().equalsIgnoreCase(username) && !r.isHidden())
                .collect(Collectors.toList());
    }

    public List<Review> getAllReviews() {
        return new ArrayList<>(reviews);
    }

    public void addReview(Review r) {
        if (r.getId() == null) {
            r.setId("R" + reviewIdCounter.getAndIncrement());
        }
        reviews.add(r);
    }

    // ========== offer methods ==========

    /**
     * saves a new offer and gives it an id when it does not have one
     */
    public void addOffer(Offer offer) {
        if (offer == null) {
            return;
        }
        // offers get their own counter so ids stay short and readable
        if (offer.getId() == null) {
            offer.setId("O" + offerIdCounter.getAndIncrement());
        }
        offers.add(offer);
    }

    /**
     * finds one offer by id, returns null when it is not there
     */
    public Offer findOffer(String id) {
        // the list is tiny so a plain loop is fine
        for (Offer offer : offers) {
            if (offer.getId().equals(id)) {
                return offer;
            }
        }
        return null;
    }

    /**
     * every offer on a listing, whatever the status
     */
    public List<Offer> getOffersForListing(String listingId) {
        return offers.stream()
                .filter(offer -> offer.getListingId() != null
                        && offer.getListingId().equals(listingId))
                .collect(Collectors.toList());
    }

    /**
     * every offer a buyer made, callers can filter the status themselves
     */
    public List<Offer> getOffersForBuyer(String username) {
        return offers.stream()
                .filter(offer -> offer.getBuyerUsername() != null
                        && offer.getBuyerUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    /**
     * pending offers for a seller, found by joining through the listing
     */
    public List<Offer> getPendingOffersForSeller(String username) {
        List<Offer> pending = new ArrayList<>();
        for (Offer offer : offers) {
            // only pending ones are interesting for the seller screen
            if (offer.getStatus() != OfferStatus.PENDING) {
                continue;
            }
            // the listing knows who actually owns the item
            Listing listing = findListing(offer.getListingId());
            if (listing != null && listing.getSellerUsername().equalsIgnoreCase(username)) {
                pending.add(offer);
            }
        }
        return pending;
    }

    /**
     * replaces an existing offer with the updated version
     */
    public void updateOffer(Offer offer) {
        for (int i = 0; i < offers.size(); i++) {
            if (offers.get(i).getId().equals(offer.getId())) {
                offers.set(i, offer);
                return;
            }
        }
        // later steps should only update offers that exist
        throw new IllegalArgumentException("no offer with id " + offer.getId());
    }

    // ========== chat methods ==========

    /**
     * finds the thread for this listing and buyer or creates a fresh one
     */
    public ChatThread getOrCreateThread(String listingId, String buyerUsername) {
        // one thread per listing + buyer pair
        for (ChatThread thread : chatThreads.values()) {
            if (thread.getListingId().equals(listingId)
                    && thread.getBuyerUsername().equalsIgnoreCase(buyerUsername)) {
                return thread;
            }
        }
        Listing listing = findListing(listingId);
        if (listing == null) {
            throw new IllegalArgumentException("no listing with id " + listingId);
        }
        ChatThread thread = new ChatThread();
        // one counter is enough, prefixes keep thread and message ids apart
        thread.setId("T" + chatIdCounter.getAndIncrement());
        thread.setListingId(listingId);
        thread.setListingTitle(listing.getTitle());
        thread.setBuyerUsername(buyerUsername);
        thread.setSellerUsername(listing.getSellerUsername());
        chatThreads.put(thread.getId(), thread);
        return thread;
    }

    /**
     * stores a chat message and gives it an id when needed
     */
    public void addChatMessage(ChatMessage message) {
        if (message == null) {
            return;
        }
        if (message.getId() == null) {
            message.setId("M" + chatIdCounter.getAndIncrement());
        }
        chatMessages.add(message);
    }

    /**
     * every message in one thread, oldest first
     */
    public List<ChatMessage> getMessagesForThread(String threadId) {
        return chatMessages.stream()
                .filter(message -> message.getThreadId() != null
                        && message.getThreadId().equals(threadId))
                .sorted(Comparator.comparing(ChatMessage::getSentAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * case-insensitive search across every stored message
     */
    public List<ChatMessage> searchMessages(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        String needle = keyword.toLowerCase();
        return chatMessages.stream()
                .filter(message -> message.getContent() != null
                        && message.getContent().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    /**
     * threads where the user is either the buyer or the seller
     */
    public List<ChatThread> getThreadsForUser(String username) {
        return chatThreads.values().stream()
                .filter(thread -> (thread.getBuyerUsername() != null
                        && thread.getBuyerUsername().equalsIgnoreCase(username))
                        || (thread.getSellerUsername() != null
                        && thread.getSellerUsername().equalsIgnoreCase(username)))
                .collect(Collectors.toList());
    }

    // ========== report methods ==========

    /**
     * saves a report from a buyer
     */
    public void addReport(Report report) {
        if (report == null) {
            return;
        }
        if (report.getId() == null) {
            report.setId("RP" + reportIdCounter.getAndIncrement());
        }
        reports.add(report);
    }

    /**
     * all reports, appends go at the end
     */
    public List<Report> getReports() {
        return new ArrayList<>(reports);
    }

    /**
     * marks one report as handled by admin
     */
    public void markReportResolved(String id) {
        for (Report report : reports) {
            if (report.getId().equals(id)) {
                report.setResolved(true);
                return;
            }
        }
    }

    // ========== deal helpers ==========

    /**
     * updates one deal status, used by offers, returns and the scheduler
     */
    public void updateDealStatus(String dealId, DealStatus status) {
        for (Deal deal : deals) {
            if (deal.getId().equals(dealId)) {
                deal.setStatus(status);
                return;
            }
        }
    }

    /**
     * active deals on one listing, handy for locking edits
     */
    public List<Deal> getActiveDealsForListing(String listingId) {
        return deals.stream()
                .filter(deal -> deal.getListingId() != null
                        && deal.getListingId().equals(listingId)
                        && deal.getStatus() == DealStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // ========== user + audit helpers ==========

    /**
     * soft disables an account so the login screen can block it
     */
    public void deactivateUser(String username) {
        User user = findUser(username);
        if (user != null) {
            user.setActive(false);
        }
    }

    /**
     * switches an account back on
     */
    public void reactivateUser(String username) {
        User user = findUser(username);
        if (user != null) {
            user.setActive(true);
        }
    }

    /**
     * returns a copy of the audit log, oldest first
     */
    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    /**
     * adds one line to the audit log
     */
    public void logAudit(String actor, String action, String detail) {
        auditLog.add(LocalDateTime.now() + " | " + actor + " | " + action + " | " + detail);
    }

    // ========== listing view helper ==========

    /**
     * bumps a listing view count by one
     */
    public void incrementViewCount(String listingId) {
        Listing listing = findListing(listingId);
        if (listing != null) {
            listing.setViewCount(listing.getViewCount() + 1);
        }
    }

    // rules used by buyer and seller registration
    public static String checkPasswordStrength(String password) {
        // empty is the easiest one to catch
        if (password == null || password.isEmpty()) return "Password cannot be empty.";
        // keep it between 10 and 64 chars
        if (password.length() < 10) return "Password must be at least 10 characters.";
        if (password.length() > 64) return "Password must be at most 64 characters.";
        // mix upper lower and a digit so it is not too weak
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        // tell the caller what is missing
        if (!hasUpper || !hasLower || !hasDigit) {
            return "Password should contain uppercase, lowercase and a digit.";
        }
        return "OK";
    }
}
