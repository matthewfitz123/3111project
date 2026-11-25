# Javadoc guide

Javadoc comments live inside the Java sources as `/** ... */` blocks. They are not a separate document; tools such as `javadoc` or the IDE can render them.

To generate HTML docs locally:

```bash
./mvnw javadoc:javadoc
```

Output is written under `target/site/apidocs` (not committed).

## Task map

| Task | Main classes / methods | What the comments explain |
|---|---|---|
| 1.1 Buyer Registration | `services/RegistrationService#registerBuyer`<br>`controllers/buyer/BuyerRegisterController#handleRegister`<br>`utils/DummyDataService#usernameExists`, `#findUser`, `#registerUser`, `#checkPasswordStrength`<br>`utils/PasswordUtil#hash`, `#verify` | Validation order, unique username across all roles, password rules, hashed credential storage, optional residence hall. |
| 1.2 Buyer Login | `services/AuthService#loginBuyer`<br>`controllers/buyer/BuyerLoginController#handleLogin`<br>`utils/SessionManager#login`<br>`utils/PasswordUtil#verify` | Required fields, case-insensitive username lookup, buyer-role check, PBKDF2 password verification, active-account check and session creation. |
| 1.3 Browse Marketplace | `services/BrowseService#liveListings`, `#primaryPhotoPath`, `#photoPaths`, `#sellerRatingLabel`, `#showVerifiedBadge`, `#availabilityLabel`<br>`controllers/buyer/BuyerDashboardController#showBrowse`, `#applyBrowseFilters`, `#createListingCard`<br>`utils/DummyDataService#getLiveListings`<br>`models/Listing#getPhotoPaths`, `#getPriceDisplay`, `#getTypeDisplay`<br>`models/User#isVerified` | Live-only listings, filters/sort, thumbnail choice, detail photo cap, seller rating, HKUST Verified badge, availability label and type/price display labels. |
| 1.4 Listing Detail | `controllers/common/ListingDetailController#setListingId`, `#render`, `#renderPhotos`, `#renderSeller`, `#renderActions`, `#toggleFavourite`, `#handlePrimaryAction`, `#reportListing`<br>`services/FavouriteService#toggle`, `#isFavourite`, `#favouritesFor`<br>`services/BrowseService#photoPaths`, `#showVerifiedBadge`<br>`utils/DummyDataService#toggleFavourite`, `#isFavourite`, `#getFavourites` | Up to six photos, all listing fields, seller rating/verified badge, availability status, type actions, chat, favourite toggle and report button. |
| 1.5 Offer / borrow / claim + chat | `services/OfferService#submitOffer`, `#claimGiveaway`<br>`controllers/common/ListingDetailController#renderActions`, `#handlePrimaryAction`<br>`controllers/common/ChatPanelController#setListing`, `#sendMessage`, `#makeOffer`, `#submitChatOffer`, `#handleSearch`<br>`utils/DummyDataService#getOrCreateThread`, `#addChatMessage`, `#getMessagesForThread`, `#searchMessages`, `#getThreadsForUser` | Live re-check before a price offer, borrow request or claim; seller notification and claim confirmation; same-day borrow (5-second demo minimum); one shared thread for buyer and seller; retained searchable history; formal Make Offer from chat. |
| 1.6 My Deals | `controllers/buyer/BuyerDashboardController#showMyDeals`<br>`services/DealService#returnEarly`, `#markComplete`<br>`services/BorrowScheduler#checkExpired` | Current and past deals, reopen listing/chat, due date and early return, auto-return when `dueAt` passes, mark complete with the review prompt left open, listing status back to LIVE or left SOLD/GIVEN. |
| 1.7 Manage Profile | `controllers/buyer/BuyerDashboardController#showProfile`<br>`utils/DummyDataService#checkPasswordStrength`<br>`utils/PasswordUtil#hash`, `#verify` | Full name and optional new password, shared password rules and confirmation match, hashed storage, success/failure alerts. |
| 1.8 Notification Board | `controllers/common/NotificationBoardController`<br>`controllers/buyer/BuyerDashboardController#showNotifications`<br>`services/NotificationService#offerAccepted`, `#offerDeclined`, `#offerCountered`, `#newChatMessage`, `#borrowDueSoon`, `#listingRemoved`, `#reviewReplied`, `#notify`<br>`utils/DummyDataService#getNotificationsFor`, `#markNotificationRead`, `#markAllNotificationsRead` | Category, timestamp and read state. Offer, chat, review-reply and announcement helpers. Due-soon text exists; removal of a listing notifies the seller from the service. |
| 1.9 Deal History | `controllers/buyer/DealHistoryController`<br>`services/DealHistoryService#buyerHistory`, `#typeLabel`, `#statusLabel`, `#sellerDisplay`, `#historyDate`, `#matches` | Completed and returned deals with title, seller, date, Bought/Borrowed/Received and outcome. Filter by date, category and seller search. A finished deal shows up on the next query. |
| 1.10 Review / Rate Sellers | `services/ReviewService#submitReview`<br>`utils/DummyDataService#addReview`, `#getReviewsForSeller`, `#recalculateSellerRating`<br>`controllers/common/ListingDetailController#renderSellerReviews` | One 1–5 rating and comment per completed deal, seller average from visible reviews, reviews on the listing page, stored with the buyer username. |
