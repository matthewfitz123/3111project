# Testing report

## How to run

```bash
./mvnw test
```

JUnit 5 tests run through Maven Surefire. JaCoCo is attached for coverage.

## Latest result

- Date: 2026-11-25
- Command: `./mvnw test`
- Tests run: **100**
- Failures: **0**
- Errors: **0**
- Skipped: **0**
- Result: **BUILD SUCCESS**

## Full suite

| Test class | Tests |
|---|---:|
| `services.AuthServiceTest` | 8 |
| `services.BorrowSchedulerTest` | 2 |
| `services.BrowseServiceTest` | 7 |
| `services.DealHistoryServiceTest` | 3 |
| `services.DealServiceTest` | 4 |
| `services.FavouriteServiceTest` | 3 |
| `services.ListingDealEdgeTest` | 6 |
| `services.ListingOfferChatTest` | 6 |
| `services.ListingServiceTest` | 6 |
| `services.NotificationEdgeTest` | 3 |
| `services.NotificationServiceTest` | 4 |
| `services.OfferServiceEdgeTest` | 7 |
| `services.OfferServiceTest` | 9 |
| `services.RegistrationServiceTest` | 5 |
| `utils.ChatStoreTest` | 5 |
| `utils.DummyDataServiceTest` | 8 |
| `utils.PasswordRulesTest` | 5 |
| `utils.PasswordUtilTest` | 5 |
| `utils.SessionManagerTest` | 4 |
| **Total** | **100** |

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

Confirmation dialogs on the listing detail screen and in the chat panel are JavaFX alerts. They call `OfferService` and only display the result, so they are checked manually rather than in JUnit. |
