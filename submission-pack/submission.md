# HKUST Marketplace — submission

Demo accounts, password rules, and limits: [supplementary-notes.md](supplementary-notes.md).

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

The shot is the buyer registration form after a new account is submitted, plus the error when the username is already taken.

**1.2 Login**

<img width="904" height="1160" alt="Screenshot 2026-11-26 165101" src="https://github.com/user-attachments/assets/ba6ac9c6-1311-4a20-b974-2773f904812b" />
<img width="1018" height="1236" alt="Screenshot 2026-11-26 165050" src="https://github.com/user-attachments/assets/37e758c7-7971-4335-a48d-5342af755724" />



The shot is the buyer login screen, once for a real buyer and once for the wrong role. 

**1.3 Browse**

_(paste screenshot)_<img width="2036" height="616" alt="Screenshot 2026-11-26 165140" src="https://github.com/user-attachments/assets/65d3250d-54fd-43e5-9a84-f553fe1d8ca6" />
<img width="2098" height="826" alt="Screenshot 2026-11-26 165207" src="https://github.com/user-attachments/assets/cdd988d6-b693-4bc5-a93f-7fce6b620d55" />
<img width="2880" height="1620" alt="Screenshot 2026-11-26 165251" src="https://github.com/user-attachments/assets/8e0f00e4-6d8c-4f46-8651-6cc246101812" />


The shot is the buyer browse grid after a filter, with the sort control still on screen, with a filter that filters everything away, and then a filter that keeps a portion of the entries. The third screenshot is of all the entries sorted from A-Z as specified in the sort section of the filter.

**1.4 Listing detail**

_(paste screenshot)_
<img width="2876" height="1614" alt="Screenshot 2026-11-26 165316" src="https://github.com/user-attachments/assets/7a27e556-b357-4bde-b738-0b85781a62f7" />
<img width="2880" height="1620" alt="Screenshot 2026-11-26 165328" src="https://github.com/user-attachments/assets/b1b7713c-7b4e-417c-9f13-dd7cc519554b" />


The shot is one live listing opened from browse.

**1.5 Offer, borrow, claim, and chat**

_(paste screenshot)_
<img width="2878" height="1618" alt="image" src="https://github.com/user-attachments/assets/ab2265df-cb1c-4a54-be5d-41fa979e82d9" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/feca25a0-fff3-48a0-81a7-4df56307e988" />
<img width="2868" height="1600" alt="image" src="https://github.com/user-attachments/assets/dd216c4e-8cad-4f41-910a-50945b6e14fe" />
<img width="2880" height="1580" alt="image" src="https://github.com/user-attachments/assets/189bd6db-0e78-4f2d-9f0e-4bf145753056" />
<img width="2880" height="1618" alt="image" src="https://github.com/user-attachments/assets/a2ab0e33-f1bc-46f1-93a9-a3923ad8a4c3" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/f9f106bd-b742-4f86-91fb-4e633c9981af" />


The shots cover a price offer and then requested borrow dates being inputted and then a free item that had been claimed and then the chat function for the listings along with the chat functions search capability.

**1.7 My Deals**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/d0011e1d-23d6-4901-804b-44df9da28018" />
<img width="2880" height="1618" alt="Screenshot 2026-11-26 173549" src="https://github.com/user-attachments/assets/3138325e-39f8-409e-907f-71cfa70b9227" />


1. On the welcome screen choose the seller portal.
2. Open Offers. If Graphing Calculator TI-84 has no pending borrow yet, log out, log in on the buyer portal, open that lend listing from Browse Marketplace, click the borrow action, set start to today and end to a later day, and send it. Log back in and accept that offer.
3. Log in Click My Deals. Take the shot while the calculator card shows the due date and the Return early button.
4. Click Return early and confirm. Take a second shot of the Rate Seller dialog. If you click Later, the same card must still show Leave review.

**1.8 Profile**

_(paste screenshot)_
<img width="2880" height="1618" alt="Screenshot 2026-11-26 173653" src="https://github.com/user-attachments/assets/4e9f234c-cbed-42b2-9a78-ea08786f9fc6" />
<img width="2878" height="1620" alt="image" src="https://github.com/user-attachments/assets/15a3c939-1aab-466e-9c99-eee7ae9016a8" />
<img width="2876" height="1620" alt="image" src="https://github.com/user-attachments/assets/7e89606e-4345-406c-a582-85c8d1c93035" />

1. Log in on the buyer portal as `alice` / `Password12`. Click My Profile.
2. Change Full Name to `Alice Wong`. Leave both password boxes empty. Click Save Changes. The shot must show the success alert.
3. On the same form type `short` in New Password and `different` in Confirm New Password. Click Save Changes. The shot must show the warning that the password is too weak or that the two boxes do not match. The account must still log in with `Password12`.

**1.9 Notifications**

_(paste screenshot)_
<img width="2878" height="1616" alt="image" src="https://github.com/user-attachments/assets/440ed545-211f-46d4-b4ff-b030d1fdd786" />

1. Log in on the buyer portal as `alice` / `Password12`. On Browse Marketplace open Calculus Textbook (Stewart), which is Charlie’s live sell listing. Submit an offer of `160`.
2. Log out. Log in on the seller portal as `charlie` / `Password12`. Open Offers and decline that offer.
3. Log in again as `alice`. Click Notifications. The shot must show a row whose text says the offer was declined, with a category and a time.

**1.10 Deal history**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/91624fdc-d5b0-466d-959a-0820d85e1d40" />


1. Finish the Return early steps in 1.7 so Alice has a returned borrow, or mark a completed purchase from My Deals.
2. Still as `alice`, click Deal History.
3. In the search box type `Diana` or set Category to Electronics. The shot must show the table columns Item Title, Seller, Category, Deal Date, Type, and Outcome, only the rows that match, and Leave review on a returned or completed row that has not been rated yet.

**1.11 Reviews**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/2ed7285e-3caa-4e5e-b11b-71ad19280e0c" />
<img width="2880" height="1616" alt="image" src="https://github.com/user-attachments/assets/ebcea3a2-7631-4598-9f5a-fea654de7752" />

1. As `alice`, open My Deals or Deal History on the returned calculator or a completed purchase. Click Leave review.
2. Leave the rating at 5 stars. Type `thanks` in the comment. Click Submit review. The shot must show the alert that the review was posted, and the button replaced by Reviewed.
3. Open Browse Marketplace, then Diana’s or Charlie’s listing. The shot must show that seller’s rating on the detail page. Clicking Leave review again on the same deal must show a warning that it was already reviewed.

### Seller

**2.1 Registration and 2.2 Login**

_(paste screenshot)_
<img width="1702" height="1610" alt="image" src="https://github.com/user-attachments/assets/ccf037f5-009d-4519-9fb7-c4b802d7510f" />
<img width="2182" height="1620" alt="image" src="https://github.com/user-attachments/assets/aaef2a08-d9aa-4720-b179-1377ea279582" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/77f828d1-7151-4215-a78a-fc2bda56ba3b" />

1. From the welcome screen open seller registration. Type username `frank`, full name `Frank Ho`, password `Password12`, the same password again, a bio such as `Selling textbooks`, and a meet-up location. Submit. The shot must show the success message.
2. Open the seller login screen. Type `charlie` / `Password12`. The shot must show the seller dashboard titled My Listings.
3. Log out and try `alice` / `Password12` on the seller login. The shot must show the warning that this account is not a seller, and the dashboard must not open.

**2.3 Create listing**

_(paste screenshot)_
<img width="2860" height="1620" alt="image" src="https://github.com/user-attachments/assets/5cf7ab22-1f63-45f6-8243-ef1ee16554f8" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/a9c898e9-e8e3-42b1-b656-5b51e116dc09" />


1. Log in on the seller portal as `charlie` / `Password12`. Click Create Listing.
2. Set Listing Type to Sell, Item Name to `USB Fan`, Category to Electronics, Condition to Used, Price to `40`, and a meet-up location. Click submit.
3. The shot must show the success alert, then My Listings with USB Fan marked pending review. Log in as `alice` and confirm USB Fan is not on Browse Marketplace.

**2.5 My listings**

_(paste screenshot)_
<img width="2876" height="1608" alt="image" src="https://github.com/user-attachments/assets/405ffa1d-38c3-4b32-ae90-acebc8cf8a1a" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/2f3febe5-205d-4314-b4a0-c3b0f9cb1593" />
<img width="2864" height="1614" alt="image" src="https://github.com/user-attachments/assets/0bdc8fde-1dbc-4187-9978-1c8e22d94f98" />

1. Log in as `charlie` / `Password12`. On My Listings pick a live listing with no pending offer, such as MacBook Pro Charger 61W. Click Edit.
2. If a history warning appears, confirm it. Clear the title and click Save. The shot must show “Title cannot be empty.”
3. Type `MacBook Charger` and save. The shot must show that new title on the card. Click Delete on a pending listing you created yourself and capture the confirmation dialog before you confirm.

**2.6 Offers**

_(paste screenshot)_
<img width="2878" height="1616" alt="image" src="https://github.com/user-attachments/assets/3a016daa-dd39-4c6e-8932-79858302dc89" />


1. Log in as `alice` / `Password12`. Offer `200` on MacBook Pro Charger 61W.
2. Log in as `charlie` / `Password12`. Open Offers. The shot must show Alice’s offer of HK$200 with Accept, Decline, and Counter.
3. Click Counter, type `220`, and send it. The offer must leave the pending list. Log in as `alice`, open Notifications, and the shot must show a counter notice for that listing.

**2.7 Profile and 2.8 Notifications**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/67b0c6da-d6b1-41ac-bd8c-98d3c23786be" />
<img width="2874" height="1620" alt="image" src="https://github.com/user-attachments/assets/d72c41e0-81cc-4fa3-aaaa-ee012fb41708" />

1. Log in as `charlie` / `Password12`. Click My Profile. Set Bio to `Textbooks and electronics`, pick a preferred meet-up, and leave the password boxes empty. Click Save Changes. The shot must show the “Profile saved” alert and the new bio still in the box.
2. Have `alice` send any offer to one of Charlie’s live listings, or ask `admin` to approve Charlie’s USB Fan. Click Notifications as `charlie`. The shot must show a row with a category and a time for the new offer or the approval.

**2.9 Stats**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/95dfd814-a975-4e26-b23f-1d73d046175b" />

1. Log in as `alice` and open Calculus Textbook (Stewart) once, so the view count moves.
2. Log out and log in as `charlie` / `Password12`. Click Seller Stats.
3. The shot must show the numbers for views, offers, deals, and rating, and both the bar chart and the pie chart on that same screen.

**2.10 Reviews**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/47d5aca8-5c94-47ad-9e6c-44cacecdd6f1" />
<img width="2878" height="1620" alt="image" src="https://github.com/user-attachments/assets/507b0784-11eb-4af9-86cb-0d4c007fe10d" />

1. Complete 1.11 so `alice` has posted a review of Charlie or Diana.
2. Log in as that seller (`charlie` or `diana`, password `Password12`). Click Reviews & Feedback.
3. On Alice’s review, type `Glad it worked` and send the reply, or click flag. The shot must show the reply under the review, or the flagged state. Log in as `alice` and open Notifications if you replied: the shot must include her reply notice.

### Admin

**3.1 Login and 3.2 Listing review**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/10ba1d4c-02ae-4b66-9788-4b1301773760" />
<img width="2790" height="1620" alt="image" src="https://github.com/user-attachments/assets/eef101f2-3144-4809-8e7e-d7d6cb7b26dd" />
<img width="2880" height="1618" alt="image" src="https://github.com/user-attachments/assets/4421fd65-137d-4659-b2bb-df7f30cdeaa0" />

1. From the welcome screen open the admin login. There is no admin registration. Type `admin` / `Admin12345`. The shot must show the admin panel on Listing Review.
2. Mini Fridge (Diana) is already pending. Create USB Fan as Charlie in 2.3 so a second pending item exists. On Listing Review click Approve on USB Fan.
3. Click Reject on Mini Fridge, type `Photo is unclear`, and confirm. The shot must show the reason dialog. USB Fan must then appear on Alice’s Browse Marketplace. Mini Fridge must not. Diana’s Notifications must mention the rejection reason.

**3.3 Users and 3.4 Profile**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/b73b5b6c-54dc-4691-b7f4-c8c3eb2fc97f" />
<img width="2866" height="1600" alt="image" src="https://github.com/user-attachments/assets/7988546f-9815-4770-80a8-d6d324639b05" />
<img width="2866" height="1600" alt="image" src="https://github.com/user-attachments/assets/81a9e5aa-4272-4111-b4d8-7c870f747c2a" />

1. Stay logged in as `admin` / `Admin12345`. Click Manage Users. Find `bob` and choose deactivate or soft-delete. The shot must show the confirmation dialog before you confirm.
2. After confirming, log out and try the buyer portal with `bob` / `Password12`. The shot must show that the account is deactivated.
3. Log back in as `admin`. Click My Profile, change the display name to `HKUST Admin`, leave the password blank, and save. The shot must show the success alert. Bob’s old listings and deals must still be listed under All Deals or All Listings.

**3.5 All deals and 3.6 Notifications**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/335ecdcb-59a5-47c7-ba4b-668870200ec3" />
<img width="2880" height="1614" alt="image" src="https://github.com/user-attachments/assets/0e71ab3b-eb5f-49d5-9042-c13edf07aaaa" />

1. As `admin`, click All Deals. In the search box type `alice` or a listing title from a deal you already completed. The shot must show columns for the item, the buyer and seller, dates, type, and status, and only the rows that match.
2. As `alice`, open any live listing and submit a report with a short reason. Log back in as `admin` and click Notifications. The shot must show that report, or Diana’s Mini Fridge submission, with a category and a timestamp.

**3.7 Manage listings**

_(paste screenshot)_
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/678012d6-786a-4348-92ec-102525d9f41c" />
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/0ed5997c-7a80-4268-a430-227e884c1b6b" />
<img width="1344" height="722" alt="image" src="https://github.com/user-attachments/assets/46a73e81-403e-41fe-a3a4-11b4946454e5" />

1. As `alice`, offer `150` on Calculus Textbook (Stewart) and leave that offer pending.
2. Log in as `admin`. Click All Listings. Open that textbook and click the remove action. Type `Duplicate post`. The shot must show the reason field before you confirm.
3. Confirm. Log in as `alice`. The textbook must be gone from Browse Marketplace, and Notifications must tell her the listing was removed.

**3.8 Ratings**

_(paste screenshot)_
<img width="2874" height="1620" alt="image" src="https://github.com/user-attachments/assets/9ab178d6-4ca6-46da-b4e5-4a3956c3b936" />
<img width="2876" height="1620" alt="image" src="https://github.com/user-attachments/assets/51467b9d-0e2a-410f-a65c-4d572debbb19" />

1. Post Alice’s 5-star review of Charlie in 1.11 first.
2. Log in as `admin` / `Admin12345`. Click Seller Ratings. Filter by seller `charlie` or by 5 stars. The shot must show only the matching rows.
3. Hide Alice’s review. The shot must still list that row for the admin, marked hidden. Open Charlie’s listing as `alice`: the public rating must no longer include that hidden score.

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

Overall branch coverage is **67.6%, which is above 65%**. The `utils` package by itself is 62.7% and is not above 65%. The recorded summary in [coverage-report.md](../docs/submission/coverage-report.md) is an earlier run (67.7%, 584 of 862). Use the table in this section.

## 4. Documentation of implemented tasks (JavaDoc)

Comments live in the sources as `/** ... */` blocks. Generate HTML with:

```bash
./mvnw javadoc:javadoc
```

Open `target/site/apidocs/index.html` (not committed). The class-by-class map is [javadoc-guide.md](../docs/submission/javadoc-guide.md).

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
