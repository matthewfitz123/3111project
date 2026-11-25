# HKUST Marketplace

Campus marketplace for buyers, sellers, and one seeded admin. Matthew is not in the H group, so H-Group items are out of scope and are not described here as delivered. Buyer favourites (1.6) and seller AI descriptions (2.4) are skipped entirely.

The behaviour below is implemented in the service layer, with a local SQLite database behind it. JavaFX controllers call those methods and are not unit tested.

## Setup

Requires Java 21 and Maven. From the project root:

```bash
./mvnw javafx:run
./mvnw test
```

`javafx:run` starts `com.hkust.marketplace.MarketplaceApp`. `./mvnw test` runs JUnit 5 and writes JaCoCo HTML to `target/site/jacoco/index.html` (not committed). Coverage is measured on `services` and `utils`. Controllers and model classes are excluded.

## Database

The app uses a local SQLite database at `data/marketplace.db`. The JDBC URL can be overridden with `-Dmarketplace.db.url=...`. `Database` owns the schema; `PersistenceService` loads the store on startup and saves it after each store change and on close. Deleting `data/marketplace.db` resets the demo to the seed data. The runtime file is git-ignored.

Tables: `users`, `listings`, `listing_photos`, `offers`, `chat_threads`, `chat_messages`, `deals`, `notifications`, `reviews`, `reports`, `favourites`, `audit_log`. Passwords in `users` are PBKDF2 hashes, never plaintext.

Further grading notes:

- `docs/submission/testing-report.md`
- `docs/submission/javadoc-guide.md`
- `docs/submission/coverage-report.md`
- `supplementary-notes.txt`

The suite is 130 tests, 0 failures. JaCoCo on the latest run: overall 86.5% instruction and 67.7% branch (584 of 862); services 69.6% branch; utils 62.7% branch.

## Password rules

Used for buyer and seller registration and for profile password changes:

- 10–64 characters
- at least one uppercase letter
- at least one lowercase letter
- at least one digit
- the confirmation password must match exactly

Usernames are unique across buyers, sellers, and admins. Full name is required. Buyer residence hall is optional.

## Password storage

Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes: 120,000 iterations and a 16-byte salt. The stored form is `pbkdf2$iterations$salt$hash`. `PasswordUtil.hash` runs on registration and on a password change. `PasswordUtil.verify` runs on login. The raw password is never stored.

Accounts, listings, deals, and chat are cached in `DummyDataService` and persisted in `data/marketplace.db`. Closing and reopening the app keeps them.

## Shared behaviour

- Role checks use `SessionManager.requireRole` from offer, deal, listing, review, and stats methods. A buyer session cannot perform seller-only or admin-only actions. Each portal login rejects the wrong role and a deactivated account.
- Opening a listing detail screen calls `incrementViewCount`.
- Chat is one thread per listing and buyer. Messages are stored in SQLite and `searchMessages` matches case-insensitively. The seller joins the thread the buyer already started. A formal Make Offer reuses `OfferService`.
- A giveaway claim is `synchronized` and re-checks that the listing is still live inside the lock. Only the first claim succeeds. The listing becomes `GIVEN`.

## Buyer

| Task | What it does |
|---|---|
| 1.1 Registration | Validates the rules above, then stores a hashed password. |
| 1.2 Login | Buyer role only. Opens the buyer session. |
| 1.3 Browse | Live listings only, with keyword, category, type, condition, price range, and sort. |
| 1.4 Listing detail | Photos, fields, seller rating, view count, and report. |
| 1.5 Offer / borrow / claim / chat | Sell: price offer. Lend: start and end date. Giveaway: one claim. Private chat with search and a formal offer. |
| 1.7 My Deals | Current and past deals. Reopen the listing and chat. Borrows show a due time and can be returned early. Purchases and claims can be marked complete, which opens the review prompt. Return sets the listing back to live. Completion leaves it sold or given. |
| 1.8 Profile | Edit full name and, optionally, password. Success and failure are shown on the form. |
| 1.9 Notifications | Offer accepted, declined, or countered; new chat; review reply; other notices. Each row has a category and a timestamp. |
| 1.10 Deal history | Completed, returned, and expired deals: title, seller, date, Bought / Borrowed / Received, and outcome. Filter by date range, category, or seller. Completing a deal puts it in this list. |
| 1.11 Reviews | After a completed purchase or a returned borrow: a comment and a rating from 1 to 5. The seller average updates. Reviews show on the seller profile and on listing detail. The buyer's reviews stay in the store. |

## Seller

| Task | What it does |
|---|---|
| 2.1 Registration | Same credential rules as buyers, plus bio and a preferred meet-up location. |
| 2.2 Login | Seller role only. |
| 2.3 Create listing | Type-specific fields. New listings start as pending review. Up to six photos are copied under `src/main/resources/images/uploads`. |
| 2.5 My Listings | List own listings. Edit or delete only when the listing is live, rejected, or pending and has no pending offer. The screen asks for confirmation. |
| 2.6 Offers | Accept, decline, or counter. The listing status changes with the decision and the buyer is notified. |
| 2.7 Profile | Full name, password, bio, and preferred meet-up. |
| 2.8 Notifications | Listing approved or rejected, new offer, review received, deal completed. |
| 2.9 Stats | Views, offers, deals, and rating. `StatsService` supplies the rows and status counts behind the bar and pie charts. |
| 2.10 Reviews | The seller sees reviews, can reply (the buyer is notified), or flag a review (it is hidden until an admin checks it). |

## Admin

There is no admin registration. Only the seeded admin can log in.

| Task | What it does |
|---|---|
| 3.1 Login | Admin role and password check. |
| 3.2 Listing review | Pending queue. Approve, or reject with a required reason. The seller is notified. |
| 3.3 Users | View, edit, deactivate, or soft-delete a buyer or seller, with confirmation. A soft delete deactivates login. Listings, deals, and chat stay in the store. |
| 3.4 Profile | Full name and password. |
| 3.5 All deals | Title, parties, dates, type, and status, with search and filters. |
| 3.6 Notifications | New submissions, reported listings, and account-change notices on the shared board. |
| 3.7 Listings | Edit any listing, add one for a seller, or take a live listing down with a reason. Buyers with a pending or countered offer, or an active deal, are notified. |
| 3.8 Ratings | Filter reviews and hide or show inappropriate ones. Hidden reviews leave the public average. |

## Limits

- The database is a local SQLite file, so it is per-machine and not shared between users.
- JavaFX controllers are not unit tested.
- The borrow form only picks dates. The “minimum borrow is 5 seconds” line is a demo hint. The service rejects an end date before the start and allows a same-day borrow.
- A pending offer is not a deal until the seller accepts it. My Deals lists deals, and shows countered offers separately.
- `borrowDueSoon` can store a reminder, but nothing schedules it. The scheduler only auto-returns a borrow after `dueAt`.
- Photos are copied into the resources tree for a local demo.

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin12345` |
| Buyer | `alice` | `Password12` |
| Buyer | `bob` | `Password12` |
| Seller | `charlie` | `Password12` |
| Seller | `diana` | `Password12` |
