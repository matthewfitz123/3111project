package com.hkust.marketplace.utils;

import com.hkust.marketplace.data.PersistenceService;
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
    // buyer favourites are stored per lowercase username for task 1.4
    private final Map<String, Set<String>> favourites = new LinkedHashMap<>();
    // each line looks like "timestamp | actor | action | detail"
    private final List<String> auditLog = new ArrayList<>();

    private final AtomicInteger listingIdCounter = new AtomicInteger(100);
    private final AtomicInteger dealIdCounter = new AtomicInteger(500);
    private final AtomicInteger notifIdCounter = new AtomicInteger(1);
    private final AtomicInteger reviewIdCounter = new AtomicInteger(1);
    private final AtomicInteger offerIdCounter = new AtomicInteger(1);
    private final AtomicInteger chatIdCounter = new AtomicInteger(1);
    private final AtomicInteger reportIdCounter = new AtomicInteger(1);
    private boolean suppressPersistence;

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
        boolean loaded = false;
        if (PersistenceService.isEnabled()) {
            try {
                loaded = PersistenceService.load(this);
            } catch (RuntimeException ex) {
                System.err.println("[store] database load failed: " + ex.getMessage());
            }
        }
        if (!loaded) {
            // seed without writing a half built store to disk
            suppressPersistence = true;
            seedData();
            suppressPersistence = false;
            persist();
        }
    }

    // writes the whole store to sqlite, quietly so a db hiccup never kills the ui
    private void persist() {
        if (suppressPersistence || !PersistenceService.isEnabled()) {
            return;
        }
        try {
            PersistenceService.save(this);
        } catch (RuntimeException ex) {
            System.err.println("[store] database save failed: " + ex.getMessage());
        }
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
        seller1.setVerified(true);
        users.put("charlie", seller1);

        User seller2 = new User("diana", "Diana Ng", PasswordUtil.hash("Password12"), UserRole.SELLER);
        seller2.setBio("Selling furniture and kitchen items before graduation.");
        seller2.setPreferredMeetup("Hall 5 Lobby");
        seller2.setAverageRating(4.3);
        seller2.setRatingCount(8);
        seller2.setVerified(true);
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
        addNotification(new Notification("alice", "Offer Accepted",
                "Charlie accepted your offer of HK$160 for Calculus Textbook.", "Offer"));
        addNotification(new Notification("charlie", "New Offer Received",
                "Alice offered HK$160 for your Calculus Textbook.", "Offer"));
        addNotification(new Notification("diana", "Listing Pending",
                "Your Mini Fridge listing is awaiting admin review.", "System"));
        addNotification(new Notification("admin", "New Listing Submitted",
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

        // seller ratings always come from the stored reviews
        for (User seller : getUsersByRole(UserRole.SELLER)) {
            recalculateSellerRating(seller.getUsername());
        }
    }

    /**
     * a plain copy of every collection so the database layer can save it
     */
    public static final class Snapshot {
        public final Map<String, User> users = new LinkedHashMap<>();
        public final Map<String, Listing> listings = new LinkedHashMap<>();
        public final List<Deal> deals = new ArrayList<>();
        public final List<Notification> notifications = new ArrayList<>();
        public final List<Review> reviews = new ArrayList<>();
        public final List<Offer> offers = new ArrayList<>();
        public final Map<String, ChatThread> chatThreads = new LinkedHashMap<>();
        public final List<ChatMessage> chatMessages = new ArrayList<>();
        public final List<Report> reports = new ArrayList<>();
        public final Map<String, Set<String>> favourites = new LinkedHashMap<>();
        public final List<String> auditLog = new ArrayList<>();
    }

    /**
     * copies the live store into a snapshot for persistence
     */
    public Snapshot snapshot() {
        Snapshot s = new Snapshot();
        s.users.putAll(users);
        s.listings.putAll(listings);
        s.deals.addAll(deals);
        s.notifications.addAll(notifications);
        s.reviews.addAll(reviews);
        s.offers.addAll(offers);
        s.chatThreads.putAll(chatThreads);
        s.chatMessages.addAll(chatMessages);
        s.reports.addAll(reports);
        favourites.forEach((username, ids) -> s.favourites.put(username, new HashSet<>(ids)));
        s.auditLog.addAll(auditLog);
        return s;
    }

    /**
     * replaces the live store with a snapshot loaded from sqlite
     */
    public void restore(Snapshot s) {
        suppressPersistence = true;
        users.clear();
        users.putAll(s.users);
        listings.clear();
        listings.putAll(s.listings);
        deals.clear();
        deals.addAll(s.deals);
        notifications.clear();
        notifications.addAll(s.notifications);
        reviews.clear();
        reviews.addAll(s.reviews);
        offers.clear();
        offers.addAll(s.offers);
        chatThreads.clear();
        chatThreads.putAll(s.chatThreads);
        chatMessages.clear();
        chatMessages.addAll(s.chatMessages);
        reports.clear();
        reports.addAll(s.reports);
        favourites.clear();
        s.favourites.forEach((username, ids) -> favourites.put(username, new HashSet<>(ids)));
        auditLog.clear();
        auditLog.addAll(s.auditLog);

        listingIdCounter.set(nextNumber(s.listings.keySet(), "L", 100));
        dealIdCounter.set(nextNumber(s.deals.stream().map(Deal::getId).toList(), "D", 500));
        notifIdCounter.set(nextNumber(s.notifications.stream().map(Notification::getId).toList(), "N", 1));
        reviewIdCounter.set(nextNumber(s.reviews.stream().map(Review::getId).toList(), "R", 1));
        offerIdCounter.set(nextNumber(s.offers.stream().map(Offer::getId).toList(), "O", 1));
        int nextChat = Math.max(
                nextNumber(s.chatThreads.keySet(), "T", 1),
                nextNumber(s.chatMessages.stream().map(ChatMessage::getId).toList(), "M", 1));
        chatIdCounter.set(nextChat);
        reportIdCounter.set(nextNumber(s.reports.stream().map(Report::getId).toList(), "RP", 1));
        suppressPersistence = false;
    }

    // finds the next free numeric id for a prefix
    private static int nextNumber(java.util.Collection<String> ids, String prefix, int base) {
        int max = base - 1;
        for (String id : ids) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                    // ids from older data may not be numeric, skip them
                }
            }
        }
        return max + 1;
    }

    /**
     * clears everything and seeds the demo data again
     * this is only used by unit tests so each test starts from the same state
     */
    public void resetForTests() {
        suppressPersistence = true;
        users.clear();
        listings.clear();
        deals.clear();
        notifications.clear();
        reviews.clear();
        offers.clear();
        chatThreads.clear();
        chatMessages.clear();
        reports.clear();
        favourites.clear();
        auditLog.clear();

        listingIdCounter.set(100);
        dealIdCounter.set(500);
        notifIdCounter.set(1);
        reviewIdCounter.set(1);
        offerIdCounter.set(1);
        chatIdCounter.set(1);
        reportIdCounter.set(1);

        seedData();
        suppressPersistence = false;
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

    /**
     * checks whether a username is already used by any role (task 1.1)
     * matching is case-insensitive because users are keyed by lowercase name
     */
    public boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    /**
     * returns the user for a username regardless of letter case, or null when absent
     */
    public User findUser(String username) {
        return users.get(username.toLowerCase());
    }

    /**
     * stores a user under a lowercase username key so lookups stay case-insensitive
     * callers must check {@link #usernameExists(String)} first to avoid overwriting
     */
    public void registerUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
        persist();
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

    /**
     * returns only admin-approved live listings for the buyer browse screen (task 1.3)
     */
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
        persist();
    }

    public void updateListing(Listing listing) {
        listings.put(listing.getId(), listing);
        persist();
    }

    public void removeListing(String id) {
        listings.remove(id);
        persist();
    }

    // ========== favourite methods ==========

    /**
     * toggles one listing in a user's favourites and returns the new state (task 1.4)
     */
    public boolean toggleFavourite(String username, String listingId) {
        if (username == null || username.isBlank() || listingId == null || listingId.isBlank()) {
            return false;
        }
        Set<String> saved = favourites.computeIfAbsent(username.toLowerCase(), key -> new HashSet<>());
        if (!saved.add(listingId)) {
            saved.remove(listingId);
            persist();
            return false;
        }
        persist();
        return true;
    }

    /**
     * true when the listing is saved in the user's favourites
     */
    public boolean isFavourite(String username, String listingId) {
        if (username == null || listingId == null) {
            return false;
        }
        Set<String> saved = favourites.get(username.toLowerCase());
        return saved != null && saved.contains(listingId);
    }

    /**
     * favourite listing ids for one user, used by the favourites screen later
     */
    public Set<String> getFavourites(String username) {
        if (username == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(
                favourites.getOrDefault(username.toLowerCase(), Collections.emptySet()));
    }

    // ========== deal methods ==========
    public List<Deal> getDealsForBuyer(String username) {
        return deals.stream()
                .filter(d -> d.getBuyerUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public List<Deal> getDealsForSeller(String username) {
        return deals.stream()
                .filter(d -> d.getSellerUsername() != null
                        && d.getSellerUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public List<Deal> getAllDeals() {
        return new ArrayList<>(deals);
    }

    public Deal findDeal(String id) {
        for (Deal deal : deals) {
            if (deal.getId().equals(id)) {
                return deal;
            }
        }
        return null;
    }

    public void addDeal(Deal deal) {
        if (deal.getId() == null) {
            deal.setId("D" + dealIdCounter.getAndIncrement());
        }
        deals.add(deal);
        persist();
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
        persist();
    }

    /**
     * marks one notification as read, the board uses this per row
     */
    public void markNotificationRead(String id) {
        for (Notification notification : notifications) {
            if (id != null && id.equals(notification.getId())) {
                notification.setRead(true);
                persist();
                return;
            }
        }
    }

    /**
     * marks every notification for one user as read
     */
    public void markAllNotificationsRead(String username) {
        for (Notification notification : notifications) {
            if (notification.getUsername().equalsIgnoreCase(username)) {
                notification.setRead(true);
            }
        }
        persist();
    }

    /**
     * how many unread notifications a user has, used for the sidebar badge
     */
    public int getUnreadNotificationCount(String username) {
        return (int) notifications.stream()
                .filter(n -> n.getUsername().equalsIgnoreCase(username) && !n.isRead())
                .count();
    }

    // ========== review methods ==========
    public List<Review> getReviewsForSeller(String username) {
        return reviews.stream()
                .filter(r -> r.getSellerUsername().equalsIgnoreCase(username) && !r.isHidden())
                .collect(Collectors.toList());
    }

    /**
     * every review for a seller including hidden ones, for the seller's own screen
     */
    public List<Review> getAllReviewsForSeller(String username) {
        return reviews.stream()
                .filter(r -> r.getSellerUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public Review findReview(String id) {
        return reviews.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Review> getAllReviews() {
        return new ArrayList<>(reviews);
    }

    public void addReview(Review r) {
        if (r.getId() == null) {
            r.setId("R" + reviewIdCounter.getAndIncrement());
        }
        reviews.add(r);
        persist();
    }

    /**
     * average and count of a seller's visible reviews, call after any review change
     */
    public void recalculateSellerRating(String sellerUsername) {
        User seller = findUser(sellerUsername);
        if (seller == null) {
            return;
        }
        List<Review> visible = getReviewsForSeller(sellerUsername);
        double average = visible.stream().mapToInt(Review::getRating).average().orElse(0.0);
        seller.setAverageRating(Math.round(average * 10) / 10.0);
        seller.setRatingCount(visible.size());
        persist();
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
        persist();
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
     * every offer on the seller's listings, whatever the status
     */
    public List<Offer> getOffersForSeller(String username) {
        List<Offer> received = new ArrayList<>();
        for (Offer offer : offers) {
            Listing listing = findListing(offer.getListingId());
            if (listing != null && listing.getSellerUsername().equalsIgnoreCase(username)) {
                received.add(offer);
            }
        }
        return received;
    }

    /**
     * replaces an existing offer with the updated version
     */
    public void updateOffer(Offer offer) {
        for (int i = 0; i < offers.size(); i++) {
            if (offers.get(i).getId().equals(offer.getId())) {
                offers.set(i, offer);
                persist();
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
        persist();
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
        persist();
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
        persist();
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
                persist();
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
                persist();
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
            persist();
        }
    }

    /**
     * switches an account back on
     */
    public void reactivateUser(String username) {
        User user = findUser(username);
        if (user != null) {
            user.setActive(true);
            persist();
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
        persist();
    }

    // ========== listing view helper ==========

    /**
     * bumps a listing view count by one
     */
    public void incrementViewCount(String listingId) {
        Listing listing = findListing(listingId);
        if (listing != null) {
            listing.setViewCount(listing.getViewCount() + 1);
            persist();
        }
    }

    /**
     * shared password rules for registration (task 1.1)
     * requires 10-64 characters with at least one uppercase letter, one lowercase letter and one digit
     *
     * @return {@code "OK"} when the password is acceptable, otherwise the user-facing error message
     */
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
