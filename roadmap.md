# HKUST Marketplace — Build Plan (Non-Honors Track)

JavaFX desktop application · Tasks 1–3 · Build order is **dependency-driven**, not document order.

---

## 1. Key decisions & constraints

| Decision | Rationale |
|---|---|
| **JavaFX + FXML** (existing skeleton) | No HTTP layer exists, so "RBAC at the API layer" is implemented as **service-layer enforcement**: every service method calls `SessionManager.requireRole(...)`. Controllers never enforce access by hiding buttons alone. Document this interpretation for the TA. |
| **Add Maven (or Gradle)** | Skeleton currently has only `.idea/` + `.iml`. External jars needed (SQLite driver, BCrypt, JUnit) and the TA needs a one-command run. |
| **SQLite persistence, not `DummyDataService`** | Requirements demand secure storage + retained/searchable chat history. `DummyDataService` is repurposed as `DataSeeder`. |
| **`Offer` ≠ `Deal`** | `Offer` = proposal (pending / accepted / declined / countered, price or date range). `Deal` = accepted transaction (due date, completion, outcome). Both models are required. |
| **Shared FXML components** | One implementation of cards, chat, notifications, profile form, listing detail — reused by all three roles (consistency NFR). |

> ⚠️ **Do not build Task 1 → Task 2 → Task 3 in order.** Buyer flows depend on seller listing creation (2.3), admin approval (3.2), seller offer acceptance (2.5), and deal completion before reviews (1.10).

---

## 2. Target project structure

```
src/main/java/com/hkust/marketplace/
├── MarketplaceApp.java
├── controllers/
│   ├── WelcomeController.java
│   ├── admin/      (login, dashboard, listing review, users, listings, deals, ratings)
│   ├── buyer/      (login, register, dashboard/browse, deals, history)
│   ├── seller/     (login, register, dashboard, create listing, offers, reviews, stats)
│   └── common/     (listing detail, chat panel, notification board, profile form)
├── models/
│   ├── User, UserRole
│   ├── Listing, ListingStatus, ListingType, Condition, Category
│   ├── Offer, OfferStatus
│   ├── Deal
│   ├── ChatThread, ChatMessage
│   ├── Review
│   ├── Notification
│   └── Report
├── services/                       # business logic + RBAC checks
│   ├── AuthService, UserService, ListingService, OfferService, DealService
│   ├── ChatService, ReviewService, NotificationService, StatsService
│   └── BorrowScheduler
├── data/                           # persistence
│   ├── Database (connection + schema init)
│   └── UserDAO, ListingDAO, OfferDAO, DealDAO, ChatDAO, ReviewDAO, NotificationDAO
└── utils/
    ├── SceneManager, SessionManager
    ├── PasswordUtil, Validator
    └── DataSeeder (was DummyDataService)

src/main/resources/
├── css/styles.css
├── fxml/{admin,buyer,seller,common}/
└── images/

src/test/java/                      # JUnit
data/                               # runtime: marketplace.db + uploaded images
```

---

## 3. Build order

### Phase 0 — Foundations
- [ ] **1.** Maven-ify: `src/main/java`, `src/main/resources`, `src/test/java`; add `javafx-maven-plugin`, `sqlite-jdbc`, `jbcrypt`, JUnit. Exclude `.idea/` and `*.iml` from the ZIP.
- [ ] **2.** Create `services/`, `data/`, `controllers/common/`, `utils/PasswordUtil`, `utils/Validator`.
- [ ] **3.** Add missing models: `Offer`, `OfferStatus`, `ChatThread`, `ChatMessage`, `Report`, `Condition`, `Category`, photo paths on `Listing`.
- [ ] **4.** Complete `ListingStatus`: `PENDING_REVIEW, LIVE, REJECTED, RESERVED, SOLD, LENT, GIVEN, REMOVED` + allowed transitions defined in one place.
- [ ] **5.** `Database` + full schema + `DataSeeder` (hardcoded admin, demo buyers/sellers, HKUST locations, categories).
- [ ] **6.** `PasswordUtil` (BCrypt, or PBKDF2WithHmacSHA256 + salt for zero deps) and `Validator`. **Write exact password rules into `documentation.md` now.**
- [ ] **7.** Upgrade `SessionManager` (current user, role, `requireRole`) and `SceneManager` (parameterised loading, e.g. `load("ListingDetailView", c -> c.setListingId(id))`).
- [ ] **8.** `fxml/common/`: `ListingCardView`, `ChatPanelView`, `NotificationBoardView`, `ProfileFormView`, `ListingDetailView`, `ConfirmDialog`; extend `styles.css`.

### Phase 1 — Auth & profiles
- [ ] **9.** `AuthService` + `UserDAO` → `BuyerRegisterController` / `BuyerRegisterView` — **1.1**
- [ ] **10.** Reuse for `SellerRegisterController` (+ bio, meet-up locations) — **2.1**
- [ ] **11.** Login + role check: `BuyerLoginController` **1.2**, `SellerLoginController` **2.2**, `AdminLoginController` **3.1**
- [ ] **12.** `ProfileFormView` wired into all three dashboards — **1.7, 2.6, 3.4**

### Phase 2 — Listing lifecycle
- [ ] **13.** `seller/CreateListingController` + FXML: type-driven validation, `FileChooser` ≤6 photos copied to `data/images/`, status `PENDING_REVIEW` — **2.3**
- [ ] **14.** `admin/ListingReviewController`: approve / reject-with-reason. Build `NotificationService` here (first real event) — **3.2**
- [ ] **15.** My Listings list (read-only first) in `SellerDashboardController` — **2.4**
- [ ] **16.** Browse + `ListingCardView`, search / filter / sort, Live listings only — **1.3**
- [ ] **17.** `common/ListingDetailView`: all photos, seller rating placeholder, **view-count increment**, report button — **1.4**

### Phase 3 — Interaction core
- [ ] **18.** `ChatDAO` / `ChatService` / `ChatPanelView`: thread per (listing, buyer), retained + searchable — **1.5 (chat)**
- [ ] **19.** `OfferService` + buyer action buttons: availability check + **atomic claim** (`synchronized` service method *and* DB transaction/uniqueness guard) — **1.5**
- [ ] **20.** `seller/OfferManagementController`: accept / decline / counter, status transitions, notify buyer — **2.5**
- [ ] **21.** Formal "Make Offer" inside `ChatPanelView`, reusing `OfferService` — **1.5**
- [ ] **22.** `buyer/MyDealsController`: reopen listing/chat, self-return, mark complete — **1.6**
- [ ] **23.** `BorrowScheduler`: `ScheduledExecutorService`, UI updates via `Platform.runLater`, shutdown in `MarketplaceApp.stop()`; 5s minimum borrow — **1.6 / 1.8**
- [ ] **24.** Finish My Listings edit/delete restrictions + notify affected buyers — **2.4**

### Phase 4 — Notifications
- [ ] **25.** `common/NotificationBoardView` in all three dashboards: timestamps, categories, read state — **1.8, 2.7, 3.6**

### Phase 5 — Completion & reputation
- [ ] **26.** `ReviewService` + rate-seller dialog on completion → recalc average → back-fill `ListingDetailView` + seller profile — **1.10**
- [ ] **27.** `seller/ReviewsController`: reply (→ buyer notification) + flag — **2.9**
- [ ] **28.** `admin/RatingsOverviewController`: filter by user/score/date + hide — **3.8**
- [ ] **29.** `buyer/DealHistoryController` **1.9** + `admin/AllDealsController` **3.5** (same DAO query, different scope)

### Phase 6 — Admin management & stats
- [ ] **30.** `admin/ManageUsersController`: soft-delete + audit log, confirmation dialogs — **3.3**
- [ ] **31.** `admin/ManageListingsController`: edit, add on behalf of seller, take down with reason → notify buyers with active offers — **3.7**
- [ ] **32.** `seller/StatsController`: views / offers / completed deals / avg rating; **built-in JavaFX `BarChart` + `PieChart`** — **2.8**

### Phase 7 — Hardening & submission
- [ ] **33.** JUnit tests: concurrent claim, role-guard rejection, password rules, chat search, auto-return timing.
- [ ] **34.** Javadoc/comments, `documentation.md` → README (setup, run command, seeded accounts, password rules), demo data, ZIP.

---

## 4. Task coverage map

### Task 1 — Buyer
| Task | Step | Primary files |
|---|---|---|
| 1.1 Registration | 9 | `BuyerRegisterController`, `AuthService`, `Validator` |
| 1.2 Login | 11 | `BuyerLoginController`, `SessionManager` |
| 1.3 Browse | 16 | `BuyerDashboardController`, `ListingCardView` |
| 1.4 Listing detail | 17 | `common/ListingDetailView`, `ListingService` |
| 1.5 Offer / borrow / claim + chat | 18–21 | `OfferService`, `ChatService`, `ChatPanelView` |
| 1.6 My Deals | 22–23 | `MyDealsController`, `BorrowScheduler` |
| 1.7 Profile | 12 | `ProfileFormView` |
| 1.8 Notifications | 25 | `NotificationBoardView` |
| 1.9 Deal history | 29 | `DealHistoryController`, `DealDAO` |
| 1.10 Review / rate | 26 | `ReviewService`, `Review` |

### Task 2 — Seller
| Task | Step | Primary files |
|---|---|---|
| 2.1 Registration | 10 | `SellerRegisterController` |
| 2.2 Login | 11 | `SellerLoginController` |
| 2.3 Create listing | 13 | `CreateListingController`, `ListingService` |
| 2.4 My Listings | 15, 24 | `SellerDashboardController` |
| 2.5 Offer management | 20 | `OfferManagementController`, `OfferService` |
| 2.6 Profile | 12 | `ProfileFormView` |
| 2.7 Notifications | 25 | `NotificationBoardView` |
| 2.8 Stats | 32 | `StatsController`, `StatsService` |
| 2.9 Review handling | 27 | `ReviewsController`, `ReviewService` |

### Task 3 — Admin
| Task | Step | Primary files |
|---|---|---|
| 3.1 Login | 11 | `AdminLoginController`, `DataSeeder` |
| 3.2 Listing review | 14 | `ListingReviewController` |
| 3.3 Manage users | 30 | `ManageUsersController`, `UserService` |
| 3.4 Own profile | 12 | `ProfileFormView` |
| 3.5 All deals | 29 | `AllDealsController` |
| 3.6 Notifications | 25 | `NotificationBoardView` |
| 3.7 Manage all listings | 31 | `ManageListingsController` |
| 3.8 Ratings overview | 28 | `RatingsOverviewController` |

---

## 5. JavaFX gotchas

- Never run DB queries on the FX Application Thread — use `Task` / `Service`.
- All background → UI updates go through `Platform.runLater`.
- `<fx:include>` needs `fx:id` plus an `@FXML private XxxController` field to reach the nested controller.
- `TableView` columns render empty without cell value factories.
- Stop `BorrowScheduler` in `MarketplaceApp.stop()` or the JVM won't exit.
- Copy selected photos into `data/images/`; never store absolute paths to the user's original files.

## 6. Do not defer (common mark-losers)

Password hashing · service-layer RBAC · atomic giveaway claim · view counting · searchable chat history · auto-return job · documented password rules.

## 7. Suggested split (group of 3)

Phases 0–1 together, then:
- **A:** seller + admin listing pipeline (13–15, 30–31)
- **B:** buyer browse / deals / history (16–17, 22, 29)
- **C:** chat / offers / notifications / reviews (18–21, 25–28)

Reconvene at Phase 5. Stats (32) and hardening (33–34) are shared.