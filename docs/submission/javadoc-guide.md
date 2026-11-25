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
