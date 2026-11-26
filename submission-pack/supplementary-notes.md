HKUST Marketplace - supplementary notes
======================================

What is in scope
----------------
Non-H buyer tasks 1.1-1.5 and 1.7-1.11, seller tasks 2.1-2.3 and 2.5-2.10,
and admin tasks 3.1-3.8 are implemented in the service and store layer.

  1.1 Buyer registration (unique username, password rules, hashed storage)
  1.2 Buyer login (role check, deactivated account)
  1.3 Browse live listings (search, filter, sort)
  1.4 Listing detail (photos, seller rating, view count, report)
  1.5 Offer, borrow request, giveaway claim, and private searchable chat
  1.7 My Deals (reopen listing and chat, due date, early return, mark complete)
  1.8 Buyer profile (full name and password)
  1.9 Notification board (category, timestamp, read state)
  1.10 Deal history (title, seller, date, type, outcome, filters)
  1.11 Review a seller after a completed purchase or a returned borrow
  2.1 Seller registration (bio and preferred meet-up)
  2.2 Seller login
  2.3 Create listing (pending review)
  2.5 List, edit, and delete own listings, with a confirmation dialog
  2.6 Accept, decline, or counter an offer, and notify the buyer
  2.7 Seller profile (name, password, bio, meet-up)
  2.8 Seller notifications (approval, offer, review, completion)
  2.9 Stats: views, offers, deals, rating, with bar and pie chart data
  3.1 Admin login for the seeded admin only (no admin registration)
  3.2 Approve or reject a pending listing, with a reason and a seller notice
  3.3 View, edit, deactivate, or soft-delete a buyer or seller
  3.4 Admin name and password
  3.5 All deals, with the listed fields and search/filter
  3.6 Admin notifications (submissions, reports, account changes)
  3.7 Edit any listing, add a listing for a seller, take one down with a reason
      and notify buyers who had an active offer
  3.8 Filter ratings and hide inappropriate reviews

JavaFX screens call those services. The controllers themselves are not unit tested.
All of the above is backed by a local SQLite database at data/marketplace.db.

Where to look
-------------
  submission-pack/submission.md       combined TA document, including the JUnit results
  docs/submission/javadoc-guide.md    task-to-method map
  docs/submission/coverage-report.md  earlier JaCoCo snapshot
  target/site/jacoco/index.html       JaCoCo HTML (generated, not committed)

How to run
----------
  ./mvnw test

Latest result
-------------
131 tests, 0 failures, 0 errors, 0 skipped.

JaCoCo from ./mvnw test on 2026-11-26 (services and utils only):
  overall   86.6% instruction (5180 of 5984), 67.6% branch (587 of 868)
  services  69.4% branch (444 of 640)
  utils     62.7% branch (143 of 228)
Overall branch coverage is above 65%. The utils package alone is not.

Two behaviour fixes in af1cc09
------------------------------
- An expired borrow now appears in the buyer's deal history.
- A returned borrow (status RETURNED) can be reviewed. Previously only
  COMPLETED purchases could be reviewed.

Password rules
--------------
10 to 64 characters, at least one uppercase letter, one lowercase letter,
and one digit. The confirmation password must match. The same rules are used
for registration and for profile password changes. They are also written in
submission-pack/submission.md.

Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes. The raw password is
never stored. Login checks the hash with PasswordUtil.verify.

Database
--------
Local SQLite is used because the TA only requires a database, not a server.
The schema is in data/Database.java and has tables for users, listings,
listing photos, offers, chat threads/messages, deals, notifications, reviews,
reports, favourites and the audit log. PersistenceService loads and saves the
whole store in one transaction. Tests disable persistence and use the in-memory
reset hook, so the real data file is never touched by JUnit.

Design decisions
----------------
- Access control is in the service layer. SessionManager.requireRole is used
  by offer, deal, listing, review, and stats methods, so a buyer session cannot
  run seller-only or admin-only actions.
- Giveaway claims are synchronized and re-check that the listing is still live
  inside the lock. Two buyers cannot both win the same giveaway.
- View count increases when a listing detail screen is opened.
- Chat is one thread per listing and buyer. Messages are stored in SQLite and
  searched case-insensitively. The seller joins the thread the buyer started.
- The store is still the fast in-memory working copy, but SQLite is the storage
  layer. It is loaded on startup and saved after each store change and on close.
- A soft-deleted user is deactivated and cannot log in. Their listings, deals,
  and chat stay in the store.
- Seller edit/delete is allowed only while the listing is live, rejected, or
  pending, and only when there is no pending offer.

Still true limitations
----------------------
- SQLite is local to the machine. The file is data/marketplace.db; delete it if
  you want to reset the demo to the seeded data. It is git-ignored.
- The JavaFX controllers are not unit tested. They were checked by loading
  screens and by clicking through the flows.
- The borrow form only picks dates. The "minimum borrow is 5 seconds" line is
  a demo hint. The service rejects an end date that is before the start date,
  rejects a start or end date that is already in the past, and allows a
  same-day borrow. A past date shows a warning and is not stored.
- After Return early, My Deals opens the review dialog, and both My Deals and
  Deal History keep a Leave review action until the buyer submits one.
- Photos are copied into src/main/resources/images/uploads and the classpath
  copy. That is enough for a local demo.
- A pending offer is not a deal until the seller accepts it. My Deals lists
  deals, and shows countered offers separately so the buyer can accept them.
- NotificationService.borrowDueSoon can store a reminder, but nothing schedules
  it. The scheduler only auto-returns a borrow after dueAt.

Demo accounts
-------------
admin    / Admin12345     admin (seeded, no registration screen)
alice    / Password12     buyer
bob      / Password12     buyer
charlie  / Password12     seller
diana    / Password12     seller
