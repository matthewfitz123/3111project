# HKUST Marketplace — submission

Demo accounts, password rules, and limits: [supplementary-notes.md](../../supplementary-notes.md).

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

<img width="1152" height="1394" alt="Screenshot 2026-11-26 164928" src="https://github.com/user-attachments/assets/9b45a605-759c-4963-b6fc-cec53477cdd6" />
<img width="1060" height="1472" alt="Screenshot 2026-11-26 164955" src="https://github.com/user-attachments/assets/29e6e8ab-b08f-4ae5-8a73-539ac1c6cde3" />



The shot is the buyer registration form after a new account is submitted, plus the error when the username is already taken. Input: a fresh username such as `evan`, full name `Evan Lee`, password and confirmation `Password12`, and optionally a residence hall; then the same form again with username `alice`. The marker should see a success message and cleared fields for `evan`, and a duplicate-username warning for `alice` with no second account created.

**1.2 Login**

<img width="904" height="1160" alt="Screenshot 2026-11-26 165101" src="https://github.com/user-attachments/assets/ba6ac9c6-1311-4a20-b974-2773f904812b" />
<img width="1018" height="1236" alt="Screenshot 2026-11-26 165050" src="https://github.com/user-attachments/assets/37e758c7-7971-4335-a48d-5342af755724" />



The shot is the buyer login screen, once for a real buyer and once for the wrong role. Input: `alice` / `Password12`, then `charlie` / `Password12` on the same buyer portal. The marker should see the buyer dashboard for `alice`, and a warning that the account is not a buyer for `charlie`, with no session opened.

**1.3 Browse**

_(paste screenshot)_<img width="2036" height="616" alt="Screenshot 2026-11-26 165140" src="https://github.com/user-attachments/assets/65d3250d-54fd-43e5-9a84-f553fe1d8ca6" />
<img width="2098" height="826" alt="Screenshot 2026-11-26 165207" src="https://github.com/user-attachments/assets/cdd988d6-b693-4bc5-a93f-7fce6b620d55" />
<img width="2880" height="1620" alt="Screenshot 2026-11-26 165251" src="https://github.com/user-attachments/assets/8e0f00e4-6d8c-4f46-8651-6cc246101812" />


The shot is the buyer browse grid after a filter, with the sort control still on screen. Input: log in as `alice`, type a keyword such as `fridge` or pick one category, and set sort to Title A–Z or Price: Low to High. The marker should see only live listings that match, with lend and giveaway items still listed when the filter is not a price filter.

**1.4 Listing detail**

_(paste screenshot)_
<img width="2876" height="1614" alt="Screenshot 2026-11-26 165316" src="https://github.com/user-attachments/assets/7a27e556-b357-4bde-b738-0b85781a62f7" />
<img width="2880" height="1620" alt="Screenshot 2026-11-26 165328" src="https://github.com/user-attachments/assets/b1b7713c-7b4e-417c-9f13-dd7cc519554b" />


The shot is one live listing opened from browse. Input: as `alice`, open a Charlie sell listing such as the seeded live sell item. The marker should see the title, price or type, category, condition, meet-up, photos or the empty-photo state, the seller rating, and a view count that has gone up by one for this open.

**1.5 Offer, borrow, claim, and chat**

_(paste screenshot)_
<img width="2878" height="1618" alt="image" src="https://github.com/user-attachments/assets/ab2265df-cb1c-4a54-be5d-41fa979e82d9" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/feca25a0-fff3-48a0-81a7-4df56307e988" />
<img width="2868" height="1600" alt="image" src="https://github.com/user-attachments/assets/dd216c4e-8cad-4f41-910a-50945b6e14fe" />
<img width="2880" height="1580" alt="image" src="https://github.com/user-attachments/assets/189bd6db-0e78-4f2d-9f0e-4bf145753056" />
<img width="2880" height="1618" alt="image" src="https://github.com/user-attachments/assets/a2ab0e33-f1bc-46f1-93a9-a3923ad8a4c3" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/f9f106bd-b742-4f86-91fb-4e633c9981af" />


The shots cover a price offer, a same-day borrow, a past-date rejection, a giveaway claim, and chat search. Input: as `alice`, offer a positive price such as `80` on a live sell listing; on Diana’s lend listing pick today for both start and end, then try yesterday; claim the live giveaway; send a chat line such as `library pickup` and search for `library`. The marker should see a confirmation that the offer or same-day request was sent, the warning “the start and end dates cannot be in the past” with nothing stored, a claim confirmation and the item no longer available, and the chat hit in the shared thread.

**1.7 My Deals**

_(paste screenshot)_

The shot is My Deals for an accepted borrow, then the review dialog after Return early. Input: as `alice`, after Diana accepts a borrow that starts today and ends in a few days, open My Deals and click Return early, then confirm. The marker should see the due date and Return early on the active card, then a success notice and the Rate Seller dialog. Choosing Later leaves a Leave review button on that returned card.

**1.8 Profile**

_(paste screenshot)_

The shot is the buyer profile form after a valid save and after a bad password. Input: as `alice`, set full name to `Alice Wong` and save with the password boxes empty; then type `short` in the new password and a different confirmation. The marker should see “Profile saved” (or the equivalent success alert) for the name change, and a warning for the weak or mismatched password with the old password still in force.

**1.9 Notifications**

_(paste screenshot)_

The shot is the buyer notification board after the seller acts on an offer. Input: as `alice`, send an offer; log in as the seller (`charlie` or `diana`) and accept or decline it; log back in as `alice` and open Notifications. The marker should see a row with a category such as Offer, a timestamp, and text that the offer was accepted or declined.

**1.10 Deal history**

_(paste screenshot)_

The shot is the completed-deals table with a filter and a review control. Input: as `alice`, open Deal History after a returned borrow or a completed purchase, set the category filter or type the seller name in search. The marker should see the item title, seller, date, type (Bought, Borrowed, or Received), and outcome, with Leave review on a row that is not yet rated. Rows that do not match the filter drop out of the table.

**1.11 Reviews**

_(paste screenshot)_

The shot is a submitted review and the seller rating on the listing page. Input: as `alice`, on a completed purchase or returned borrow, submit 5 stars and a short comment such as `thanks`. The marker should see the “review was posted” alert, the button change to Reviewed, and the seller average on that seller’s listing detail include the new score. A second submit on the same deal is rejected.

### Seller

**2.1 Registration and 2.2 Login**

_(paste screenshot)_

The shots are a new seller registration and a successful seller login. Input: register username `frank`, full name `Frank Ho`, password `Password12`, a short bio, and a meet-up such as the Library; then log in as `charlie` / `Password12` on the seller portal. The marker should see a success message for `frank`, and Charlie’s seller dashboard. Using `alice` on the seller portal shows that the account is not a seller.

**2.3 Create listing**

_(paste screenshot)_

The shot is the create-listing form after submit, and the same item waiting for review. Input: as `charlie`, choose Sell, title `Desk Lamp`, a category, condition, price `40`, and a meet-up, then submit. The marker should see a success alert and the listing under My Listings with status pending review. It does not appear on the buyer browse grid until an admin approves it.

**2.5 My listings**

_(paste screenshot)_

The shot is the confirmation dialog for edit or delete. Input: as `charlie` or `diana`, open a listing that is live, rejected, or pending and has no pending offer, change the title or click delete, and confirm. The marker should see the confirmation text before anything changes, then the updated title or the listing removed from the seller’s list. A listing with a pending offer stays locked and shows a warning instead.

**2.6 Offers**

_(paste screenshot)_

The shot is the seller offer card and the buyer’s follow-up notice. Input: `alice` offers `80` on Charlie’s live sell listing; `charlie` accepts, declines, or counters with `90`. The marker should see the offer leave the pending list, the listing become sold on accept, and `alice` receive a notification that names the decision.

**2.7 Profile and 2.8 Notifications**

_(paste screenshot)_

The shots are the seller profile form and the seller notification list. Input: as `charlie`, set the bio to `Textbooks and electronics` and a preferred meet-up, and save; then open Notifications after an admin approves a listing or a buyer sends an offer. The marker should see “Profile saved” with the new bio, and a notification row with a category and time for the approval, offer, review, or completed deal.

**2.9 Stats**

_(paste screenshot)_

The shot is the seller stats screen. Input: log in as `charlie` or `diana` after at least one listing has been opened and one offer or deal exists. The marker should see numeric views, offers, deals, and rating, plus a bar chart and a pie chart built from those listing rows and status counts.

**2.10 Reviews**

_(paste screenshot)_

The shot is a seller reply or flag on a buyer review. Input: after `alice` posts a review of `charlie`, log in as `charlie`, open Reviews, and reply `Glad it worked` or flag the review. The marker should see the reply stored on the review and a notification for the buyer, or the flagged review hidden from the public average until an admin looks at it.

### Admin

**3.1 Login and 3.2 Listing review**

_(paste screenshot)_

The shots are admin login and the pending-listing queue. Input: `admin` / `Admin12345`, then approve one pending item and reject another with reason `Photo is unclear`. The marker should see the admin panel, the approved listing become live for buyers, and the rejected listing stay off browse with the seller notified of the reason. There is no admin registration screen.

**3.3 Users and 3.4 Profile**

_(paste screenshot)_

The shots are the user list action and the admin profile form. Input: deactivate or soft-delete `bob` and confirm; then change the admin display name and save, leaving the password blank or setting a new one that matches `Admin12345`’s rules. The marker should see a confirmation before the user change, `bob` blocked on the next login, and a success alert for the admin name. Listings and deals for that user stay in the database.

**3.5 All deals and 3.6 Notifications**

_(paste screenshot)_

The shots are the all-deals table and the admin notification board. Input: search for a listing title or seller such as `alice`, or set a date or category filter; open Notifications after a new listing is submitted or a listing is reported. The marker should see deal rows with title, parties, dates, type, and status, only the rows that match the filter, and a notification with a category and timestamp for the submission or report.

**3.7 Manage listings**

_(paste screenshot)_

The shot is an admin edit or a takedown with a reason. Input: on a live listing that `alice` has a pending offer on, take it down with reason `Duplicate post`, or edit the title and save. The marker should see the confirmation or reason field, the listing leave the live browse grid when removed, and a notification to `alice` because she had an active offer.

**3.8 Ratings**

_(paste screenshot)_

The shot is the ratings overview with a filter and a hidden review. Input: filter by seller `charlie` or by a rating, then hide one review. The marker should see only the matching rows, the hidden review drop out of the public seller average, and the row still listed for the admin so it can be shown again.

## 2. Unit testing report

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
