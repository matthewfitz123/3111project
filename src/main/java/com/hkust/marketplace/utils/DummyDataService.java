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

    private final AtomicInteger listingIdCounter = new AtomicInteger(100);
    private final AtomicInteger dealIdCounter = new AtomicInteger(500);
    private final AtomicInteger notifIdCounter = new AtomicInteger(1);
    private final AtomicInteger reviewIdCounter = new AtomicInteger(1);

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
