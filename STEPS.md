# HKUST Marketplace — Step-by-Step Build Roadmap

**Audience:** teammates using free AI coding models. **Do NOT skip steps and do NOT do steps out of order.**

---

## How to use this document

1. Work through the numbered steps **in order**. Each step is small and self-contained.
2. After each step, run the **Verify** check before moving on. If it fails, fix it before continuing — later steps depend on earlier ones.
3. When asking an AI model for help, give it **only the current step** plus the file(s) it names. Paste the exact signatures shown below — do not let it invent its own.
4. Commit (or copy) the project after every ~5 steps so you can roll back.
5. A database **is required** by the TA. Use local SQLite (`data/marketplace.db`); it is added in Step 30. The old “in-memory only” rule is superseded.

## Ground rules (read once)

- **Never store plaintext passwords.** All passwords go through `PasswordUtil` (Step 1).
- **RBAC is enforced in the service/store layer, not just by hiding buttons.** Every method that changes data must check `SessionManager.getCurrentUser()` role. Document this in `submission-pack/submission.md` as "service-layer access control" (there is no backend/API, so this is the correct interpretation).
- **`DummyDataService` is the single in-memory working store.** Extend it — do not create parallel stores or second singletons. Step 30 persists it to SQLite.
- Keep the existing code style: plain JavaFX, no external UI libraries, and no new Maven dependencies except JUnit/JaCoCo (Step 29) and `sqlite-jdbc` + `slf4j-nop` (Step 30).
- New screens follow the existing dashboard pattern: the dashboard FXML has a `contentPane` StackPane, and the controller builds sub-views in Java (see `BuyerDashboardController.showBrowse()` etc.). Simple dialogs can be new FXML; complex screens can be Java-built panes — match what the skeleton already does.

## Run / build commands (Windows)

```powershell
cd HKUST-Marketplace
.\mvnw.cmd clean javafx:run     # run the app
.\mvnw.cmd test                 # run JUnit tests (added in Phase 7)
```

Demo logins (after Step 1 these passwords still work — they get hashed at seed time): `admin/admin123`, `alice/password1`, `bob/password1`, `charlie/password1`, `diana/password1`.

---

## PHASE 0 — Foundations (everything depends on these)

### Step 1 — Password hashing (`PasswordUtil`)

**Why first:** every later step touches login/registration/profile. Doing this last means rewriting every auth check.

**Files:** new `src/main/java/com/hkust/marketplace/utils/PasswordUtil.java`; edit `utils/DummyDataService.java` (seeding); edit all three `*LoginController.java`; edit the three dashboard profile-edit handlers.

**Do exactly:**
- Create `PasswordUtil` with two static methods using only the JDK (`javax.crypto` + `java.security`, PBKDF2WithHmacSHA256, 65536 iterations, 256-bit key, 16-byte random salt per password):
  - `public static String hash(String plaintext)` → returns `"<saltHex>:<hashHex>"`
  - `public static boolean verify(String plaintext, String stored)` → returns false on malformed stored value, never throws
- In `DummyDataService.seedData()`, pass every seeded password through `PasswordUtil.hash(...)` (e.g. `new User("admin", "...", PasswordUtil.hash("admin123"), UserRole.ADMIN)`).
- In `BuyerLoginController`, `SellerLoginController`, `AdminLoginController`: replace `user.getPasswordHash().equals(password)` with `PasswordUtil.verify(password, user.getPasswordHash())`.
- Find the profile-edit dialogs in `BuyerDashboardController`, `SellerDashboardController`, `AdminDashboardController` (search for `New password`) and route new passwords through `PasswordUtil.hash` before calling `setPasswordHash`.
- Keep `DummyDataService.checkPasswordStrength` unchanged — the rules are already: ≥8 chars, ≥1 uppercase, ≥1 lowercase, ≥1 digit. Write these exact rules into `submission-pack/supplementary-notes.md` in Phase 7.

**Verify:** run app, log in as `alice`/`password1` — works. Register a new buyer, then confirm `DummyDataService.findUser("newname").getPasswordHash()` is NOT the plaintext (add a temporary println or check via debugger). Wrong password shows error.

### Step 2 — Missing models

**Why:** Offer, Chat and Report are referenced by ~15 later steps; `Deal.status` being a raw String causes bugs.

**Files:** new models in `models/`:

- `OfferStatus.java` — enum: `PENDING, ACCEPTED, DECLINED, COUNTERED`
- `Offer.java` — fields: `id` (String, "O"+counter), `listingId`, `buyerUsername`, `sellerUsername`, `type` (ListingType), `status` (OfferStatus), `offerPrice` (Double, SELL only), `borrowStart`/`borrowEnd` (LocalDate, LEND only), `message` (String, optional note), `counterPrice`/`counterStart`/`counterEnd` (seller's counter proposal), `createdAt`, `respondedAt`. Standard getters/setters, no-arg constructor.
- `ChatThread.java` — fields: `id`, `listingId`, `listingTitle`, `buyerUsername`, `sellerUsername`, `createdAt`. One thread per (listing, buyer) pair — enforce by lookup, not by constraint.
- `ChatMessage.java` — fields: `id`, `threadId`, `senderUsername`, `content`, `sentAt`, `isFormalOffer` (boolean — true when sent via "Make Offer"), `offerId` (String, links to Offer if formal).
- `Report.java` — fields: `id`, `listingId`, `reporterUsername`, `reason`, `createdAt`, `resolved` (boolean).
- `DealStatus.java` — enum: `PENDING, ACTIVE, COMPLETED, RETURNED, CANCELLED, EXPIRED`. **Edit `Deal.java`:** change `private String status` → `private DealStatus status` (constructor default `DealStatus.PENDING`) and update getters/setters. Then fix the one seed usage in `DummyDataService` (`d1.setStatus("Accepted")` → `DealStatus.ACTIVE`).

**Verify:** `mvnw.cmd compile` passes; app still runs and seeded deal shows correctly.

### Step 3 — Extend `DummyDataService`

**Files:** `utils/DummyDataService.java` only.

**Do exactly:** add collections + counters matching existing style:
```java
private final List<Offer> offers = new ArrayList<>();
private final Map<String, ChatThread> chatThreads = new LinkedHashMap<>();
private final List<ChatMessage> chatMessages = new ArrayList<>();
private final List<Report> reports = new ArrayList<>();
private final List<String> auditLog = new ArrayList<>();   // "timestamp | actor | action | detail"
private final AtomicInteger offerIdCounter = new AtomicInteger(1);
private final AtomicInteger chatIdCounter = new AtomicInteger(1);
private final AtomicInteger reportIdCounter = new AtomicInteger(1);
```
Methods (all `public`, mirror existing naming):
- Offers: `addOffer(Offer)`, `findOffer(String id)`, `getOffersForListing(String listingId)`, `getOffersForBuyer(String username)` (status != DECLINED by default? NO — return all, let callers filter), `getPendingOffersForSeller(String username)` (join through listings), `updateOffer(Offer)`.
- Chat: `getOrCreateThread(listingId, buyerUsername)` (find existing thread for listing+buyer else create with sellerUsername from the listing), `addChatMessage(ChatMessage)`, `getMessagesForThread(String threadId)` (sorted by sentAt), `searchMessages(String keyword)` (case-insensitive content contains, return messages), `getThreadsForUser(String username)` (buyer or seller side).
- Reports: `addReport(Report)`, `getReports()`, `markReportResolved(String id)`.
- Deals: `updateDealStatus(String dealId, DealStatus)` helper, `getActiveDealsForListing(String listingId)`.
- Users: `deactivateUser(String username)` (sets active=false), `reactivateUser`, `getAuditLog()` + `logAudit(String actor, String action, String detail)`.
- Listings: `incrementViewCount(String listingId)` (find + `setViewCount(getViewCount()+1)` — used by Step 12).

**Verify:** compile passes; no behaviour change yet.

### Step 4 — `SessionManager` role guards

**Files:** `utils/SessionManager.java`.

**Do exactly:** add:
```java
public static String getCurrentUsername() { return currentUser == null ? null : currentUser.getUsername(); }
public static User requireRole(UserRole... allowed) {
    if (currentUser == null) throw new SecurityException("Not logged in");
    for (UserRole r : allowed) if (currentUser.getRole() == r) return currentUser;
    throw new SecurityException("Role " + currentUser.getRole() + " not permitted");
}
```
**Verify:** compile passes.

### Step 5 — `SceneManager` parameterized navigation

**Why:** detail screens (listing, deal, thread) need to receive an id. Without this every later screen hacks around it.

**Files:** `utils/SceneManager.java`.

**Do exactly:** add an overload:
```java
public static <T> void switchScene(String fxmlName, Consumer<T> initializer) {
    // same body as switchScene, but after loader.load():
    //   T controller = loader.getController();
    //   if (initializer != null) initializer.accept(controller);
}
```
(Refactor: extract the existing body into a private `load(String, Consumer)`; keep all existing `showX()` methods calling it with null.)

**Verify:** compile passes; existing navigation unchanged.

---

## PHASE 1 — Auth & profiles

### Step 6 — Wire registration + login end-to-end

**Files:** `BuyerRegisterController`, `SellerRegisterController`, all three `*LoginController`, `SellerRegisterView.fxml` / `BuyerRegisterView.fxml` (only if fields missing).

**Do exactly:**
- Buyer register: after validation, `PasswordUtil.hash(password)` before `new User(...)` (the User constructor's 3rd arg is the stored hash — pass the hash, not plaintext).
- Seller register: same + store `bio` and `preferredMeetup` (check FXML has these fields; if not, add `TextField bioField`, `ComboBox<String> meetupCombo` populated from `DummyDataService.HKUST_LOCATIONS`, and wire in controller).
- All three logins: after `PasswordUtil.verify` succeeds, also check `user.isActive()` — show "Account deactivated. Contact admin." if false (Step 26 sets this).
- Role check on login already exists (`user.getRole() != UserRole.X` → error). Keep it.

**Verify:** register buyer+seller, log each into correct dashboard; try logging buyer creds at seller login → error message.

### Step 7 — Profile editing (1.7 / 2.6 / 3.4)

**Files:** the `showProfile()` methods in all three dashboard controllers.

**Do exactly:** each profile form edits: fullName (always), password (optional — if both password fields non-empty, validate via `checkPasswordStrength`, confirm match, then `setPasswordHash(PasswordUtil.hash(new))`), plus seller-only bio + preferredMeetup (ComboBox from `HKUST_LOCATIONS`), buyer-only residenceHall, admin-only fullName+password.

**Verify:** change name → persists after scene switch; change password → old fails, new works.

---

## PHASE 2 — Listing lifecycle

> Order matters: seller creates (8) → admin approves (9) → buyer browses (11) → detail (12). Do not reorder.

### Step 8 — Seller "Create Listing" (2.3)

**Files:** new `controllers/seller/CreateListingController.java` + `fxml/seller/CreateListingView.fxml`; edit `SellerDashboardController` (add nav button/section).

**Do exactly:**
- Form fields: title, description (TextArea), category ComboBox (`DummyDataService.CATEGORIES`), type ComboBox (SELL/LEND/GIVEAWAY), condition ComboBox (`CONDITIONS`), price TextField (enabled only when type=SELL), lendingDays TextField (only LEND), meetupLocation ComboBox (`HKUST_LOCATIONS`), photos.
- Photos: `FileChooser` (multi-select, `ExtensionFilter` images), max 6; copy each into `src/main/resources/images/uploads/` using `Files.copy`; store **relative paths** like `"/images/uploads/xxx.jpg"` in `listing.getPhotoPaths()` — never absolute paths.
- On submit: validate required fields per type, create `Listing` with seller = `SessionManager.getCurrentUser()`, status stays `PENDING_REVIEW` (default), `DummyDataService.addListing`, add notification to `admin` ("New listing submitted"), show confirmation, return to dashboard.

**Verify:** submit a SELL listing → appears in admin pending list later; try LEND without days → error.

### Step 9 — Admin listing review (3.2) + first real notifications

**Files:** new `controllers/admin/ListingReviewController.java` + `fxml/admin/ListingReviewView.fxml`; edit `AdminDashboardController`.

**Do exactly:**
- List `getPendingListings()` showing title, seller username+name, category, type, meetup, posted date, thumbnail (first photo path → `new Image(getClass().getResourceAsStream(path))`).
- Approve button → `setStatus(LIVE)`, `setReviewedAt(now)`, confirm dialog.
- Reject button → **require** reason in a TextInputDialog; set `REJECTED` + `rejectionReason`.
- Both: `DummyDataService.addNotification` to the seller ("Listing approved" / "Listing rejected: <reason>", category "System").
- This is the first real use of notifications — keep the pattern: every later event just calls `addNotification` the same way.

**Verify:** approve one listing → it shows in buyer browse (Step 11); reject one → seller notification contains reason.

### Step 10 — Seller My Listings read-only (2.4, part 1)

**Files:** `SellerDashboardController` (the `showMyListings`-equivalent method or new section).

**Do exactly:** list `getListingsBySeller(currentUser)` in a TableView or card list: title, category, type, status, viewCount, posted date. No edit/delete yet (Step 20).

**Verify:** seller sees own listings incl. PENDING/REJECTED with reasons.

### Step 11 — Buyer browse marketplace (1.3)

**Files:** `BuyerDashboardController.showBrowse()` (already renders live listings — upgrade it).

**Do exactly:** ensure it uses `getLiveListings()` only; add: keyword TextField (matches title+description, case-insensitive), category ComboBox + type ComboBox + condition ComboBox filters, price-range fields (SELL only), sort ComboBox (Newest / Price low→high / high→low / Title). Re-render list on any filter change.

**Verify:** search "textbook" → only matching live listings; sort by price works; PENDING/REJECTED never appear.

### Step 12 — Listing detail screen (1.4)

**Files:** new `controllers/common/ListingDetailController.java` + `fxml/common/ListingDetailView.fxml`; wire from browse cards and later from My Deals. Edit `SceneManager` usage via Step 5's overload.

**Do exactly:**
- `SceneManager.switchScene("common/ListingDetailView.fxml", c -> c.setListingId(id))`.
- Controller `setListingId`: load listing, **call `incrementViewCount` immediately** (requirement: count on open), render: all photos (ImageView carousel or HBox of thumbnails + main view), description, every field, seller name + `averageRating`/`ratingCount`, status, meetup, posted date.
- Buttons: "Chat" (Step 14), "Make Offer / Borrow / Claim" (Steps 15–17 — add disabled placeholders now), "Report Listing" (Step 26-lite: simple dialog → `addReport`, notify admin).

**Verify:** open a listing twice → viewCount increases by 2 (visible in seller My Listings).

---

## PHASE 3 — Interaction core (strict order)

### Step 13 — `NotificationService` helpers

**Files:** new `services/NotificationService.java` (new package `services/`).

**Do exactly:** thin typed wrappers so later steps never hand-build strings:
```java
public final class NotificationService {
    public static void notify(String username, String title, String message, String category)
    public static void listingApproved(Listing l) / listingRejected(Listing l, String reason)
    public static void offerReceived(Offer o) / offerAccepted(Offer o) / offerDeclined(Offer o) / offerCountered(Offer o)
    public static void newChatMessage(ChatMessage m, String recipient)
    public static void dealCompleted(Deal d) / reviewReceived(Review r) / reviewReplied(Review r)
    public static void listingRemoved(Listing l, String reason)   // Step 27 uses for affected buyers
    public static void borrowDueSoon(Deal d) / borrowExpired(Deal d)
}
```
Each delegates to `DummyDataService.addNotification(new Notification(...))`.

**Verify:** compile; refactor Step 9's two notifications to use `listingApproved/listingRejected`.

### Step 14 — Chat threads + panel (1.5 chat half)

**Files:** new `controllers/common/ChatPanelController.java` + `fxml/common/ChatPanelView.fxml` (or Java-built pane matching dashboard style); embed in `ListingDetailController` and later My Deals.

**Do exactly:**
- Open thread via `getOrCreateThread(listingId, buyerUsername)`; only the listing's seller and that buyer may post (check usernames, else disable input).
- UI: scrollable message list (sender name, timestamp, content), TextField + Send button. "Make Offer" button (formal offer) → implemented in Step 17.
- `searchMessages(keyword)` powers a search box above the list.
- On send: `addChatMessage`, append to UI, `NotificationService.newChatMessage` to the other party.

**Verify:** buyer opens listing → sends message → seller opens same listing → sees it; search finds it; third user cannot post.

### Step 15 — Offers: submit / claim / borrow request (1.5)

**Files:** new `services/OfferService.java`; buttons in `ListingDetailController`.

**Do exactly:**
```java
public final class OfferService {
    // SELL: price offer. LEND: borrowStart/borrowEnd dates. GIVEAWAY: claim.
    // ALL methods: re-check listing status == LIVE first (throw IllegalStateException otherwise).
    public static synchronized Offer submitOffer(Listing l, User buyer, Double price, LocalDate start, LocalDate end, String note)
    public static synchronized Offer claimGiveaway(Listing l, User buyer)
}
```
- `synchronized` + re-check inside the lock = the atomic-claim requirement (two buyers clicking Claim simultaneously → exactly one Offer/GIVEN transition succeeds; loser gets "no longer available").
- SELL offer: create `Offer` status PENDING → `NotificationService.offerReceived` to seller → confirmation dialog.
- LEND: validate end > start, and **minimum borrow duration = 5 seconds** for testing (i.e. `(end-start)` may be same-day; enforce `!end.isBefore(start)` and show hint "min. borrow: 5 seconds for demo"). Store dates on Offer.
- GIVEAWAY claim: immediately create Offer(status=ACCEPTED) + Deal + set listing `GIVEN` (or RESERVED→GIVEN via Step 16 path — simplest: treat claim as auto-accepted, create Deal status ACTIVE, listing GIVEN, notify seller). **Do it inside the synchronized method.**

**Verify:** two rapid claims on same giveaway → only one succeeds; offer on non-LIVE listing → error.

### Step 16 — Seller offer management (2.5)

**Files:** new `controllers/seller/OfferManagementController.java` + view; section in `SellerDashboardController` or per-listing in My Listings.

**Do exactly:** for each `getPendingOffersForSeller(me)`: show buyer, type, price/dates, note, "Open chat" (→ Step 14 thread), and:
- **Accept** → Offer ACCEPTED; create `Deal` (PENDING→ACTIVE, copy offerPrice/dates); listing → RESERVED (LEND) or SOLD (SELL); `offerAccepted` notify buyer.
- **Decline** → Offer DECLINED; listing stays LIVE; `offerDeclined` notify.
- **Counter** (SELL/LEND only) → TextInputDialog for new price or new end-date → Offer COUNTERED with `counterPrice`/`counterEnd` filled; `offerCountered` notify buyer (message includes the counter value).
- Buyer sees counter in My Deals → "accept counter" reuses `submitOffer`-style path creating a new PENDING offer with countered terms (or simply accept → seller auto-accepts; pick simplest: buyer's My Deals has "Accept counter" button that finalizes deal directly).

**Verify:** full loop: buyer offers → seller accepts → buyer My Deals shows ACTIVE deal, listing SOLD.

### Step 17 — Formal "Make Offer" inside chat (1.5)

**Files:** `ChatPanelController` + small dialog.

**Do exactly:** "Make Offer" button → dialog asking price (SELL) or end date (LEND) → calls the **same** `OfferService.submitOffer` as Step 15 → inserts a `ChatMessage` with `isFormalOffer=true`, `offerId` set, content like "[Offer] HK$200" → notifies seller. Do NOT duplicate offer logic.

**Verify:** offer made from chat appears in seller's offer management screen.

### Step 18 — Buyer My Deals (1.6)

**Files:** `BuyerDashboardController` deals section (upgrade existing `showDeals`).

**Do exactly:** list `getDealsForBuyer(me)` with status; per row buttons:
- "View listing" + "Open chat" (navigate via Step 5/12/14).
- LEND + status ACTIVE: show `borrowEnd` due date + "Return early" button → Deal RETURNED, listing back to LIVE, notify seller.
- SELL/GIVEAWAY + ACTIVE: "Mark complete" → Deal COMPLETED, `rated=false`, triggers review prompt (Step 22); listing SOLD/GIVEN stays.
- DECLINED/CANCELLED offers shown greyed (optional nice-to-have).

**Verify:** complete the Step 16 loop → deal appears, mark complete works, return-early works on a lend deal.

### Step 19 — `BorrowScheduler` auto-return (1.6 / 1.8)

**Files:** new `services/BorrowScheduler.java`; edit `MarketplaceApp` (start in `start()`, override `stop()`).

**Do exactly:**
```java
private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
// in MarketplaceApp.start(): BorrowScheduler.start();
// in MarketplaceApp.stop():  BorrowScheduler.shutdown();
```
- Every 1 second (demo-friendly): scan deals where `dealType==LEND`, status ACTIVE, `borrowEnd` passed (treat end as end-of-day; for the 5-second demo rule compare `borrowStart`/`borrowEnd` as `LocalDateTime` — **change Offer/Deal borrow fields note: keep LocalDate for display but compute expiry from `createdAt + lendingDays` is wrong; simplest correct approach: store `borrowEndDateTime` — if you kept LocalDate, add a `dueAt` LocalDateTime field to Deal in Step 2 retroactively NOW before proceeding**).
- ⚠️ **Important correction to Step 2:** Deal needs `private LocalDateTime dueAt;` — add it when you do Step 19 if not present. Scheduler sets status EXPIRED/RETURNED, listing → LIVE, `borrowExpired` notification.
- All UI-facing side effects must run inside `Platform.runLater(...)` — the scheduler thread must never touch JavaFX controls.

**Verify:** create lend deal with near-immediate dueAt → within ~2s listing returns to LIVE and buyer gets notification. App exits cleanly (no hanging non-daemon thread — call `exec.shutdownNow()` in `stop()`).

### Step 20 — Finish seller My Listings rules (2.4 part 2)

**Files:** `SellerDashboardController` My Listings + `OfferService`/`DummyDataService` helpers.

**Do exactly:** Edit allowed only when status is LIVE or REJECTED/PENDING (not RESERVED/SOLD/LENT/GIVEN/REMOVED) AND no PENDING offers (`getOffersForListing` filtered). Delete same rule. On delete/edit of a listing with past offers: confirm dialog warns "N buyers have offer history". If admin removes a listing with active offers (Step 27), that's where `listingRemoved` notifications fire — seller edit does NOT need to notify.

**Verify:** can't edit a SOLD listing; can edit LIVE with no pending offers; confirm dialog appears otherwise.

---

## PHASE 4 — Notifications board

### Step 21 — `NotificationBoardView` in all three dashboards (1.8 / 2.7 / 3.6)

**Files:** new `controllers/common/NotificationBoardController.java` + `fxml/common/NotificationBoardView.fxml` (or Java pane); wire into each dashboard's existing notifications section.

**Do exactly:** load `getNotificationsFor(currentUsername)` sorted newest-first; each row: category badge, title, message, timestamp; "Mark all read" + per-item mark-read (set `read=true`); optional category filter ComboBox. Unread count badge on the dashboard nav button.

**Verify:** trigger events from earlier steps (offer, rejection, chat) → all appear in the right user's board with correct category/timestamp.

---

## PHASE 5 — Reputation & history

### Step 22 — Reviews + rating recalc (1.10)

**Files:** new `services/ReviewService.java`; review dialog in `BuyerDashboardController` My Deals; rating display in `ListingDetailController` + seller profile.

**Do exactly:**
- On "Mark complete" (Step 18) or from My Deals row → dialog: 1–5 star ComboBox/spinner + comment TextArea → `ReviewService.submitReview(deal, buyer, rating, comment)`:
  - guard: deal COMPLETED, `!deal.isRated()`, buyer is deal's buyer → else error.
  - create `Review`, `deal.setRated(true)`, recalc seller: `averageRating = mean(non-hidden reviews)`, `ratingCount` = count.
  - `dealCompleted`/`reviewReceived` notifications.
- `ListingDetailController`: show seller avg rating + list of `getReviewsForSeller(seller)` (non-hidden).

**Verify:** complete deal → rate → seller avg updates; detail page shows it; double-rating blocked.

### Step 23 — Seller review handling (2.9)

**Files:** new `controllers/seller/ReviewsController.java` + view; section in seller dashboard.

**Do exactly:** list `getReviewsForSeller(me)` (include hidden flag state); "Reply" → TextInputDialog → `setSellerReply` → `reviewReplied` notification to the buyer (`review.getBuyerUsername()`); "Flag" → sets a flag/hidden pending admin (simplest: `setHidden(true)` + notify admin category "Moderation" — admin un-hides in Step 24).

**Verify:** reply → buyer notification board shows it.

### Step 24 — Admin ratings overview (3.8)

**Files:** new `controllers/admin/RatingsOverviewController.java` + view; section in admin dashboard.

**Do exactly:** list `getAllReviews()` (all, incl. hidden) with seller, buyer, rating, date, comment; filters: seller ComboBox, rating ComboBox (1–5), date range; "Hide"/"Unhide" toggle on `hidden`.

**Verify:** hide a review → disappears from buyer-facing `getReviewsForSeller` but stays in admin list.

### Step 25 — Deal history screens (1.9 / 3.5)

**Files:** buyer `DealHistoryController`/section + `controllers/admin/AllDealsController.java` + view.

**Do exactly:**
- Buyer: completed deals only — title, seller, deal date (`completedAt`), type (Bought/Borrowed/Received from dealType+status), filter by date range + category + seller search.
- Admin: `getAllDeals()` — title, buyer username, seller username, type, start, complete/return date, status; same filters + status filter.

**Verify:** both screens show consistent data; filters work.

---

## PHASE 6 — Admin management & stats

### Step 26 — Manage users (3.3)

**Files:** new `controllers/admin/ManageUsersController.java` + view.

**Do exactly:** table of all users (username, name, role, active, rating); actions per row with `Alert`/confirm dialogs:
- Deactivate/Reactivate → `deactivateUser`/`reactivateUser` + `logAudit`.
- Delete → **soft-delete only** (`deactivateUser` + logAudit "USER_DELETED") — document in `submission-pack/supplementary-notes.md` that listings/deals/chat are retained for integrity (this answers the spec's "clarify what happens" requirement).
- Edit name/role → update + audit.

**Verify:** deactivate `bob` → bob login shows "Account deactivated" (Step 6 check); audit log records it.

### Step 27 — Manage all listings (3.7)

**Files:** `controllers/admin/ManageListingsController.java` + view.

**Do exactly:** table of ALL listings any status; edit any field (reuse create-listing form read-only→editable variant); "Add listing on behalf of seller" → same form with seller ComboBox; "Remove" → require reason → status REMOVED + `rejectionReason`-style field + for every buyer with a PENDING/ACTIVE offer or deal on it: `NotificationService.listingRemoved`.

**Verify:** remove a listing that has an active offer → that buyer's notification board shows removal notice.

### Step 28 — Seller stats (2.8)

**Files:** new `controllers/seller/StatsController.java` + view; helper methods in `DummyDataService` or a `services/StatsService.java`.

**Do exactly:** compute for current seller: total views (sum `viewCount` of their listings), offers received (count offers on their listings), completed deals (deals COMPLETED/RETURNED), avg rating. Display numbers + JavaFX `BarChart` (views/offers/deals per listing) + `PieChart` (deal outcomes or listing status distribution).

**Verify:** charts render; numbers match manual count.

---

## PHASE 7 — Hardening & submission

### Step 29 — JUnit + JaCoCo

**Files:** `pom.xml`, new `src/test/java/...`.

**Do exactly:**
- Add deps: `junit-jupiter` (5.10.x), `jacoco-maven-plugin` (0.8.12) with `prepare-agent` + `report` executions bound to `test` phase.
- **Test only non-JavaFX classes** (tools that touch `javafx.*` won't run headless): `PasswordUtil` (hash/verify/malformed), `DummyDataService.checkPasswordStrength`, `OfferService.submitOffer/claimGiveaway` (concurrent claims → exactly one wins — use an ExecutorService firing N threads), `SessionManager.requireRole` (reset static state in `@AfterEach` via `SessionManager.logout()`), chat `searchMessages`, scheduler expiry logic.
- For scheduler testability: if `BorrowScheduler` hard-codes "now", extract a `checkExpired(LocalDateTime now)` method and test that directly.

**Verify:** `mvnw.cmd test` green; `target/site/jacoco/index.html` shows >65% branch coverage on `services`/`utils` packages. Add tests until it passes — this is a graded requirement.

### Step 30 — SQLite database (TA requirement)

**Files:** `pom.xml`, new `src/main/java/com/hkust/marketplace/data/Database.java`, new `src/main/java/com/hkust/marketplace/data/PersistenceService.java`, `DummyDataService.java`, `MarketplaceApp.java`, `TestState.java`.

**Do exactly:**
- Add `sqlite-jdbc` (3.46.x) and `slf4j-nop` to `pom.xml`.
- Create a local SQLite database at `data/marketplace.db`. The JDBC URL may be overridden with `-Dmarketplace.db.url=...`.
- Add a normalized schema with tables for users, listings, listing photos, offers, chat threads, chat messages, deals, notifications, reviews, reports, favourites and the audit log.
- Load the saved data on startup. Seed the demo data only when the database is empty.
- Save the whole store in one transaction after store changes and again on app close. Passwords stay as PBKDF2 hashes; never write plaintext.
- Disable persistence during JUnit (`marketplace.persistence.disabled=true`) so tests use `resetForTests()` and never touch the real database file. Add save/reload tests using a temporary SQLite file.
- Add `data/` and `*.db` to `.gitignore`.

**Verify:** delete `data/marketplace.db`, run the app, register/create/offer/deal data, close and reopen — the data is still there. `mvnw.cmd test` is green, and the SQLite file contains the expected tables. Do not commit the runtime database file.

### Step 31 — Docs, polish, ZIP

**Files:** `submission-pack/submission.md`, `submission-pack/supplementary-notes.md`, JavaDoc pass, screenshots.

**Do exactly:**
- Keep the TA write-up in `submission-pack/submission.md` and `submission-pack/supplementary-notes.md`: prerequisites (Java 21), `./mvnw javafx:run`, demo accounts table, **exact password rules** (10–64 chars, upper+lower+digit), architecture note (SQLite persistence, service-layer RBAC, soft-delete policy), and the checklist (execution screenshots, unit-test report, coverage report, JavaDoc).
- JavaDoc every public class/method (graded requirement).
- ZIP the `HKUST-Marketplace` folder **excluding** `.idea/`, `*.iml`, `target/`, `data/` and `*.db`.

**Verify:** fresh clone → `mvnw.cmd clean javafx:run` works; ZIP contents clean.

---

## Dependency cheat-sheet (why the order is strict)

| Later step needs | Provided by |
|---|---|
| Any login/register | Step 1 (PasswordUtil), Step 4 (roles) |
| Buyer sees listings | Step 9 (admin approve → LIVE) |
| Offers/chat/deals | Steps 2–3 (models+store), 13 (notifications), 15 (OfferService) |
| My Deals actions | Step 16 (deals created on accept), 19 (dueAt field!) |
| Reviews | Step 18 (COMPLETED deals), 22 |
| Notification board | Steps 13 + all event steps before it |
| Stats | Steps 10/15/18 (views, offers, deals already tracked) |
| Tests | Step 29 needs stable service APIs from Steps 13–19 |
| Database | Step 30 persists the store after Steps 1–29 |

## Common mistakes to avoid

- Storing plaintext or hashing twice (hash once at registration/seed; `verify` at login).
- Checking role only in the controller — put `SessionManager.requireRole` inside service methods too.
- Two buyers claiming one giveaway — must be `synchronized` + status re-check inside the lock (Step 15).
- Touching JavaFX controls from `BorrowScheduler` thread — always `Platform.runLater`.
- Forgetting `MarketplaceApp.stop()` → app won't exit.
- Committing `data/marketplace.db` / `*.db` — the runtime database is local and git-ignored.
- Leaving JUnit persistence enabled — tests must use `marketplace.persistence.disabled=true` and `resetForTests()`.
- Absolute photo paths — store `/images/uploads/...` classpath-relative paths only.
