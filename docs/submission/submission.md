# HKUST Marketplace — submission

## How to run

Java 21 and Maven. From the project root:

```bash
./mvnw javafx:run
./mvnw test
./mvnw javadoc:javadoc
```

`javafx:run` starts `com.hkust.marketplace.MarketplaceApp`. The app stores data in a local SQLite file, `data/marketplace.db` (git-ignored). Deleting that file and starting again reseeds the demo. Passwords are salted PBKDF2-HMAC-SHA256 hashes (120,000 iterations, 16-byte salt, form `pbkdf2$iterations$salt$hash`). The raw password is never stored.

Password rules for registration and profile changes: 10–64 characters, one uppercase letter, one lowercase letter, one digit, and the confirmation must match. Usernames are unique across buyers, sellers, and the admin. Full name is required.

| Role | Username | Password |
|---|---|---|
| Admin (seeded, no registration screen) | `admin` | `Admin12345` |
| Buyer | `alice` | `Password12` |
| Buyer | `bob` | `Password12` |
| Seller | `charlie` | `Password12` |
| Seller | `diana` | `Password12` |

## 1. Screenshots of execution

No screenshots are stored in the repo. Paste each capture in the blank under its line. Use the demo accounts above.

### Buyer

**1.1 Registration**

_(paste screenshot)_

Paste the registration form with a new buyer username and the success message, and a second shot of the duplicate-username error.

**1.2 Login**

_(paste screenshot)_

Paste a successful buyer login as `alice`, and the message shown when a seller account is used on the buyer portal.

**1.3 Browse**

_(paste screenshot)_

Paste the live-listings grid after a keyword or category filter, with the sort control visible.

**1.4 Listing detail**

_(paste screenshot)_

Paste one live listing open on the detail screen, showing photos or the empty-photo state, the seller rating, and the view count.

**1.5 Offer, borrow, claim, and chat**

_(paste screenshot)_

Paste a sell offer confirmation, a same-day borrow request, the warning when a start or end date is before today, a giveaway claim confirmation, and the shared chat thread with a search hit.

**1.7 My Deals**

_(paste screenshot)_

Paste My Deals with an active borrow (due date and Return early) and, after Return early, the review dialog.

**1.8 Profile**

_(paste screenshot)_

Paste a successful name change, and the warning for a weak or mismatched password.

**1.9 Notifications**

_(paste screenshot)_

Paste the buyer notification list after an offer is accepted or declined, showing category and time.

**1.10 Deal history**

_(paste screenshot)_

Paste the completed-deals table with a returned or completed row, a filter applied, and the Leave review control on a row that has not been rated.

**1.11 Reviews**

_(paste screenshot)_

Paste a submitted 1–5 review and the updated seller rating on the listing detail page.

### Seller

**2.1 Registration and 2.2 Login**

_(paste screenshot)_

Paste seller registration (bio and meet-up filled in) and a successful login as `charlie`.

**2.3 Create listing**

_(paste screenshot)_

Paste a new sell listing submitted, then the same item sitting in pending review.

**2.5 My listings**

_(paste screenshot)_

Paste edit or delete with the confirmation dialog, on a listing that is still live, rejected, or pending.

**2.6 Offers**

_(paste screenshot)_

Paste accept, decline, or counter on a pending offer, and the buyer notification that follows.

**2.7 Profile and 2.8 Notifications**

_(paste screenshot)_

Paste the seller profile form (name, password, bio, meet-up) and the seller notification list (approval, offer, review, or completion).

**2.9 Stats**

_(paste screenshot)_

Paste the seller stats screen with the view, offer, deal, and rating figures and the bar and pie charts.

**2.10 Reviews**

_(paste screenshot)_

Paste a seller reply or flag on a review.

### Admin

**3.1 Login and 3.2 Listing review**

_(paste screenshot)_

Paste admin login as `admin`, then approve one pending listing and reject another with a reason.

**3.3 Users and 3.4 Profile**

_(paste screenshot)_

Paste the user list with deactivate or soft-delete confirmation, and the admin name/password form.

**3.5 All deals and 3.6 Notifications**

_(paste screenshot)_

Paste the all-deals table with a search or filter, and the admin notification board.

**3.7 Manage listings**

_(paste screenshot)_

Paste an edit or a takedown with a reason, on a listing a buyer has offered on.

**3.8 Ratings**

_(paste screenshot)_

Paste the ratings screen with a filter applied and a review hidden.

## 2. Unit testing report

Full per-task tables: [testing-report.md](testing-report.md).

```bash
./mvnw test
```

Latest run on `main` (2026-11-26): **131 tests, 0 failures, 0 errors, 0 skipped. 100% pass. BUILD SUCCESS.**

JavaFX controllers are not unit tested. They call the services below.

| Test class | Tests |
|---|---:|
| `data.PersistenceServiceTest` | 3 |
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
| **Total** | **131** |

| Task | What the tests cover |
|---|---|
| 1.1 Registration | Unique username, password rules, confirmation, salted hash, optional residence hall. |
| 1.2 Login | Required fields, case-insensitive username, wrong role, wrong password, deactivated account, session. |
| 1.3 Browse | Live listings only, photo path, rating label, verified badge, type and price labels. |
| 1.4 Detail | Photo cap and favourite toggle. View count is incremented when the detail screen opens. |
| 1.5 Offer, borrow, claim, chat | Price offer, same-day borrow, end before start rejected, start or end already in the past rejected and not stored, giveaway claim locks the listing, one shared searchable thread, formal offer message kept. |
| 1.7 My Deals | Early return, mark complete, auto-return after `dueAt`, pending offer is not a deal. |
| 1.8 Profile | Password strength and hash. The form rejects a blank name. |
| 1.9 Notifications | Category, timestamp, read state. `borrowDueSoon` can be stored; the scheduler does not send it. |
| 1.10 Deal history | Completed, returned, and expired rows, with date, category, and seller filters. |
| 1.11 Reviews | 1–5 on a completed purchase or a returned borrow; a second review and a 0 rating are rejected. |
| 2.1–2.3, 2.5–2.10 | Seller login and role guard, edit/delete only while the listing is still modifiable and has no pending offer, accept/decline/counter, stats numbers, reply and flag. Registration and the create-listing form stay in JavaFX. Task 2.4 is H-Group and is not tested. |
| 3.1–3.8 | Admin login, approve/reject/takedown, soft delete, shared deal filters, hide and unhide a review. User-edit and admin profile dialogs are JavaFX. There is no admin registration. |

## 3. Coverage report

`./mvnw test` writes JaCoCo HTML to `target/site/jacoco/index.html`. That folder is generated and is not committed. The plugin counts `services` and `utils` only. Controllers, models, `MarketplaceApp`, and `data` are excluded.

Fresh report from the 2026-11-26 `./mvnw test` run:

| Scope | Instruction | Branch |
|---|---:|---:|
| Overall (`services` + `utils`) | 86.6% (5180 of 5984) | **67.6% (587 of 868)** |
| `services` | | 69.4% (444 of 640) |
| `utils` | | 62.7% (143 of 228) |

Overall branch coverage is **67.6%, which is above 65%**. The `utils` package by itself is 62.7% and is not above 65%. The recorded summary in [coverage-report.md](coverage-report.md) is an earlier run (67.7%, 584 of 862). Use the table in this section.

## 4. Documentation of implemented tasks (JavaDoc)

Comments live in the sources as `/** ... */` blocks. Generate HTML with:

```bash
./mvnw javadoc:javadoc
```

Open `target/site/apidocs/index.html` (not committed). The class-by-class map is [javadoc-guide.md](javadoc-guide.md).

Non-H behaviour, as implemented:

| Task | What it does |
|---|---|
| 1.1 Registration | Validates the password rules, then stores a hash. |
| 1.2 Login | Buyer role only. |
| 1.3 Browse | Live listings, with keyword, category, type, condition, price range, and sort. |
| 1.4 Listing detail | Photos, fields, seller rating, view count, and report. |
| 1.5 Offer / borrow / claim / chat | Sell: price. Lend: start and end, same day allowed, past dates refused with a warning and nothing stored. Giveaway: one claim. Private chat with search and a formal offer. |
| 1.7 My Deals | Reopen listing and chat. Borrows show a due time and can be returned early, which opens the review dialog. Purchases and claims can be marked complete, which also opens the review dialog. |
| 1.8 Profile | Full name and optional password. |
| 1.9 Notifications | Category and timestamp. Offer, chat, and review-reply notices. |
| 1.10 Deal history | Title, seller, date, type, outcome. Filter by date, category, or seller. Leave review on a completed purchase or a returned borrow that is not yet rated. |
| 1.11 Reviews | Comment and 1–5 rating. Seller average updates from visible reviews. |
| 2.1 Registration | Same credential rules, plus bio and preferred meet-up. |
| 2.2 Login | Seller role only. |
| 2.3 Create listing | Type-specific fields. Status starts as pending review. Up to six photos. |
| 2.5 My listings | Edit or delete only when live, rejected, or pending, and only with no pending offer. Confirmation dialog. |
| 2.6 Offers | Accept, decline, or counter. Buyer is notified. |
| 2.7 Profile | Name, password, bio, meet-up. |
| 2.8 Notifications | Approval, offer, review, completion. |
| 2.9 Stats | Views, offers, deals, rating, with bar and pie chart data from `StatsService`. |
| 2.10 Reviews | Reply (buyer notified) or flag (hidden until an admin checks it). |
| 3.1 Login | Seeded admin only. |
| 3.2 Listing review | Approve, or reject with a reason. Seller is notified. |
| 3.3 Users | View, edit, deactivate, or soft-delete. Soft delete blocks login. Listings, deals, and chat stay. |
| 3.4 Profile | Name and password. |
| 3.5 All deals | Title, parties, dates, type, status, search and filters. |
| 3.6 Notifications | Submissions, reports, account changes. |
| 3.7 Listings | Edit any listing, add one for a seller, or take one down with a reason. Buyers with a pending or countered offer, or an active deal, are notified. |
| 3.8 Ratings | Filter reviews and hide or show them. Hidden reviews leave the public average. |

Role checks use `SessionManager.requireRole`. A giveaway claim is synchronized and re-checks that the listing is still live. Chat is one thread per listing and buyer.

## Limits that are still true

- SQLite is a file on this machine, not a shared server.
- JavaFX controllers are not unit tested.
- The borrow form only picks dates. “Minimum borrow is 5 seconds” is a hint. The service rejects an end before the start, rejects a start or end before today, and allows a same-day borrow.
- A pending offer is not a deal until the seller accepts it. My Deals lists deals and shows countered offers separately.
- `borrowDueSoon` can store a reminder, but nothing schedules it. The scheduler only auto-returns a borrow after `dueAt`.
- Photos are copied into `src/main/resources/images/uploads` for a local demo.
