# Testing report

## How to run

```bash
./mvnw test
```

JUnit 5 tests run through Maven Surefire. JaCoCo is attached for coverage.

## Latest result

- Date: 2026-11-25
- Command: `./mvnw test`
- Tests run: **128**
- Failures: **0**
- Errors: **0**
- Skipped: **0**
- Result: **BUILD SUCCESS**

## Full suite

| Test class | Tests |
|---|---:|
| `services.AdminUserTest` | 1 |
| `services.AuthServiceTest` | 8 |
| `services.BorrowSchedulerTest` | 2 |
| `services.BrowseServiceTest` | 7 |
| `services.BuyerDealHistoryTest` | 3 |
| `services.BuyerMyDealsTest` | 5 |
| `services.BuyerNotificationBoardTest` | 4 |
| `services.BuyerReviewTest` | 3 |
| `services.DealHistoryServiceTest` | 3 |
| `services.DealServiceTest` | 4 |
| `services.FavouriteServiceTest` | 3 |
| `services.ListingDealEdgeTest` | 6 |
| `services.ListingModerationTest` | 2 |
| `services.ListingOfferChatTest` | 6 |
| `services.ListingServiceTest` | 6 |
| `services.NotificationEdgeTest` | 3 |
| `services.NotificationServiceTest` | 4 |
| `services.OfferServiceEdgeTest` | 7 |
| `services.OfferServiceTest` | 9 |
| `services.PortalLoginTest` | 3 |
| `services.RegistrationServiceTest` | 5 |
| `services.RoleGuardTest` | 2 |
| `services.SellerReviewHandlingTest` | 1 |
| `services.SellerStatsTest` | 1 |
| `utils.BuyerProfileRulesTest` | 3 |
| `utils.ChatStoreTest` | 5 |
| `utils.DummyDataServiceTest` | 8 |
| `utils.PasswordRulesTest` | 5 |
| `utils.PasswordUtilTest` | 5 |
| `utils.SessionManagerTest` | 4 |
| **Total** | **128** |

## Task 1.1 Buyer Registration

Added `services/RegistrationServiceTest` for the registration rules, and reused the existing password/store tests for the shared pieces.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.RegistrationServiceTest` | 5 | Username/full-name validation, password strength and confirmation match, duplicate username rejection across buyer/seller/admin, salted hash storage, optional residence hall. |
| `utils.PasswordRulesTest` | 5 | Shared password rules used by registration: length limits and required character mix. |
| `utils.PasswordUtilTest` | 5 | Salted PBKDF2 hash/verify behavior used to store credentials. |
| `utils.DummyDataServiceTest` | 8 | In-memory user store basics and seeded account roles used by registration/login checks. |

The JavaFX form itself is thin: `BuyerRegisterController#handleRegister` delegates to `RegistrationService` and only displays success/error labels. Screen behavior is verified manually and by screenshots.

## Task 1.2 Buyer Login

Added `services/AuthServiceTest` for the buyer login rules, and reused the session/password tests for the shared pieces.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.AuthServiceTest` | 8 | Required fields, case-insensitive username lookup, buyer-role rejection for seller/admin credentials, wrong password, deactivated account, session creation, and login for a newly registered buyer. |
| `utils.PasswordUtilTest` | 5 | Salted PBKDF2 verify behavior used during login. |
| `utils.SessionManagerTest` | 4 | Session login/logout and role guard helpers used after authentication. |

The JavaFX login screen is thin: `BuyerLoginController#handleLogin` delegates to `AuthService#loginBuyer` and only displays errors or navigates to the buyer dashboard. Screen behavior is verified manually and by screenshots.

## Task 1.3 Browse Marketplace

Added `services/BrowseServiceTest` for the browse-card data rules, and reused the store test for seeded live listings.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.BrowseServiceTest` | 7 | Live-only listing selection, primary photo path choice, detail photo cap, seller rating label, HKUST Verified badge rule, availability label, and sell/lend/giveaway display labels. |
| `utils.DummyDataServiceTest` | 8 | In-memory listing/user store basics and seeded data used by the browse screen. |

The JavaFX card is thin: `BuyerDashboardController#createListingCard` renders thumbnail, badges, seller rating and actions, while `BrowseService` owns the testable card data rules. Screen behavior is verified manually and by screenshots.

## Task 1.4 Listing Detail

Added `services/FavouriteServiceTest` for the like/favourite rules and extended `BrowseServiceTest` for the detail photo cap.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.FavouriteServiceTest` | 3 | Favourite toggle add/remove, per-user favourites list, null/missing input handling. |
| `services.BrowseServiceTest` | 7 | Detail gallery photo cap plus the browse-card rules reused by the detail screen. |
| `utils.DummyDataServiceTest` | 8 | In-memory listing/user store basics used by detail rendering. |

The JavaFX detail screen is thin: `ListingDetailController` renders photos/fields/seller/actions and delegates favourite state to `FavouriteService` and badge/photo rules to `BrowseService`. Screen behavior is verified manually and by screenshots.

## Task 1.5 Offer / borrow / claim + chat

Added `services/ListingOfferChatTest` for the buyer actions on a listing and the private chat thread. Older offer and chat tests still cover the same store and the atomic giveaway claim.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.ListingOfferChatTest` | 6 | Pending price offer on a sell listing with a seller notification, same-day borrow request (end date cannot be before the start; the 5-second demo minimum is a hint because the form only picks dates), giveaway claim confirmation and a second claim rejected once the listing is no longer live, rejection when the listing is not live, one retained thread used by buyer and seller with case-insensitive search, formal Make Offer message kept on that thread and searchable. |
| `services.OfferServiceTest` | 9 | Shared offer rules, including two threads claiming the same giveaway and only one succeeding. |
| `utils.ChatStoreTest` | 5 | Thread reuse, message order, and blank-search behavior used by the chat panel. |

Confirmation dialogs on the listing detail screen and in the chat panel are JavaFX alerts. They call `OfferService` and only display the result, so they are checked manually rather than in JUnit.

## Task 1.7 My Deals

Added `services/BuyerMyDealsTest` for return, completion and auto-return. Older deal and scheduler tests still cover the same methods.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.BuyerMyDealsTest` | 5 | Completed deals stay on the buyer list and the listing thread can be reopened, early return records a due time and sets the listing LIVE, auto-return expires a passed `dueAt` and sets the listing LIVE, mark complete leaves the deal unrated and the listing SOLD, a pending offer is not a deal yet. |
| `services.DealServiceTest` | 4 | Return versus complete, and a different buyer cannot close the deal. |
| `services.BorrowSchedulerTest` | 2 | Overdue borrow expires; a future due time is left active. |

View listing and Open chat are JavaFX navigation. A pending offer is only a deal after the seller accepts; countered offers are drawn on the screen but are not deals.

## Task 1.8 Manage Profile

Added `utils/BuyerProfileRulesTest` for the checks the profile form runs before it writes the user.

| Test class | Tests | What it covers |
|---|---:|---|
| `utils.BuyerProfileRulesTest` | 3 | A strong new password is hashed and verifies, weak passwords fail the shared rules, and the user model itself accepts a blank name. |
| `utils.PasswordRulesTest` | 5 | Length and character mix used by the profile form. |
| `utils.PasswordUtilTest` | 5 | Hash and verify used when the password changes. |

Success and failure text are JavaFX alerts in `BuyerDashboardController#showProfile`. The form also edits residence hall, which the task text does not require. Empty full name is rejected only in that form, not in `User`.

## Task 1.9 Notification Board

Added `services/BuyerNotificationBoardTest` for buyer-facing categories, order and read state.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.BuyerNotificationBoardTest` | 4 | Offer accepted/declined/countered, new chat, review reply and a plain announcement, each with a category and a timestamp, newest first. Due-soon text can be stored. `listingRemoved` notifies the seller. Mark-read and mark-all-read clear the unread count. |
| `services.NotificationServiceTest` | 4 | Shared helpers for listing, offer, chat, review and borrow reminders. |

`borrowDueSoon` is not called by `BorrowScheduler`. `NotificationService.listingRemoved` does not notify buyers; the admin remove screen sends those buyer notices itself. The board layout is JavaFX.

## Task 1.10 Deal History

Added `services/BuyerDealHistoryTest`. `DealHistoryServiceTest` already covers labels and the completed/returned filter.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.BuyerDealHistoryTest` | 3 | Mark complete makes the deal appear with title, seller, Bought and Completed, date/category/seller filters, and an auto-expired borrow appears with outcome Expired. |
| `services.DealHistoryServiceTest` | 3 | Completed and returned only, Bought/Borrowed/Received labels, date and search matching. |

The table is filled by `DealHistoryController` from `buyerHistory` on each refresh. Expired borrows are included.

## Task 1.11 Review / Rate Sellers

Added `services/BuyerReviewTest`.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.BuyerReviewTest` | 3 | One 1–5 review on a completed sale, seller visible-review list and average, the buyer's stored review, a second review rejected, rating 0 rejected, and a returned borrow can be reviewed. |

There is no separate buyer-review query; the record is the review's buyer username in the store. Listing detail reads `getReviewsForSeller`. The star dialog is JavaFX.

## Task 2 Seller (2.4 skipped)

H-group task 2.4 is not covered. Seller registration and create-listing screens stay in the controllers. Tests cover login, listing edit/delete guards, offer decisions, stats data, and review reply/flag.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.PortalLoginTest` | 3 | Hashed passwords, seller login, buyer rejected from the seller portal. |
| `services.RoleGuardTest` | 2 | A buyer session cannot accept offers, delete listings, approve listings, hide reviews or read seller stats. |
| `services.ListingServiceTest` | 6 | Edit and delete only when the listing is still modifiable and has no pending offer. |
| `services.OfferServiceTest` | 9 | Accept, decline and counter, with buyer notifications. |
| `services.SellerStatsTest` | 1 | View count, offer total, listing rows and status counts used by the charts. |
| `services.SellerReviewHandlingTest` | 1 | Reply notifies the buyer; flag hides the review. |

Password rules are written in `documentation.md` (10–64 characters, upper, lower, digit). Chart widgets are JavaFX. Create-listing photo copy and the seller registration form are not unit tested.

## Task 3 Admin

No admin registration. The seeded account is `admin`.

| Test class | Tests | What it covers |
|---|---:|---|
| `services.PortalLoginTest` | 3 | Admin login, wrong role rejected, deactivated admin rejected. |
| `services.ListingModerationTest` | 2 | Approve and reject with a reason, and takedown notifies the seller and a buyer with a pending offer. |
| `services.AdminUserTest` | 1 | Soft delete deactivates login and leaves the listing in the store. |
| `services.DealHistoryServiceTest` | 3 | Shared history filters used by the all-deals table. |
| `services.SellerReviewHandlingTest` | 1 | Admin unhide puts a flagged review back in the public list. |

Adding a listing for a seller, editing a user, and the admin profile form are JavaFX dialogs. `borrowDueSoon` is still not scheduled. Favourites (buyer 1.6) and seller 2.4 were not added. |
