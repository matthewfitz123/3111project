package com.hkust.marketplace.controllers.buyer;

import com.hkust.marketplace.models.Deal;
import com.hkust.marketplace.models.DealStatus;
import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.Offer;
import com.hkust.marketplace.models.OfferStatus;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.controllers.common.ListingDetailController;
import com.hkust.marketplace.services.BrowseService;
import com.hkust.marketplace.services.DealService;
import com.hkust.marketplace.services.OfferService;
import com.hkust.marketplace.services.ReviewService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import com.hkust.marketplace.utils.SceneManager;
import com.hkust.marketplace.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * buyer dashboard with browse my deals profile and notifications
 */
public class BuyerDashboardController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label welcomeLabel;
    @FXML private Button btnBrowse, btnDeals, btnHistory, btnFavourites, btnNotifications, btnProfile;

    private Button currentActiveBtn;

    // browse filter state, kept as fields so re-rendering keeps what the user picked
    private String browseCategory = "All Categories";
    private String browseType = "All Types";
    private String browseCondition = "All Conditions";
    private String browseSort = "Newest";
    private String browseSearch = "";
    private String browseMinPrice = "";
    private String browseMaxPrice = "";

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Hello, " + user.getFullName());
        }
        currentActiveBtn = btnBrowse;
        showBrowse();
        updateNotificationBadge();
    }

    /**
     * keeps the sidebar count in sync with the shared board
     */
    private void updateNotificationBadge() {
        User user = SessionManager.getCurrentUser();
        int unread = user == null ? 0
                : DummyDataService.getInstance().getUnreadNotificationCount(user.getUsername());
        btnNotifications.setText("  Notifications" + (unread > 0 ? " (" + unread + ")" : ""));
    }

    private void setActive(Button btn, String title) {
        if (currentActiveBtn != null) {
            currentActiveBtn.getStyleClass().remove("sidebar-btn-active");
        }
        btn.getStyleClass().add("sidebar-btn-active");
        currentActiveBtn = btn;
        pageTitle.setText(title);
    }

    /**
     * renders the buyer browse marketplace screen (task 1.3)
     * shows only live listings and keeps filter state between re-renders
     */
    @FXML
    private void showBrowse() {
        setActive(btnBrowse, "Browse Marketplace");
        contentPane.getChildren().clear();

        VBox root = new VBox(16);
        root.setPadding(new Insets(4));

        // Filter controls live in groups so they wrap instead of shrinking to "..."
        ComboBox<String> categoryFilter = new ComboBox<>(FXCollections.observableArrayList("All Categories"));
        categoryFilter.getItems().addAll(DummyDataService.CATEGORIES);
        categoryFilter.setPrefWidth(190);
        categoryFilter.getSelectionModel().select(browseCategory);
        categoryFilter.setOnAction(e -> {
            browseCategory = categoryFilter.getValue();
            showBrowse();
        });

        ComboBox<String> typeFilter = new ComboBox<>(FXCollections.observableArrayList("All Types", "Sell", "Lend", "Giveaway"));
        typeFilter.setPrefWidth(150);
        typeFilter.getSelectionModel().select(browseType);
        typeFilter.setOnAction(e -> {
            browseType = typeFilter.getValue();
            showBrowse();
        });

        ComboBox<String> conditionFilter = new ComboBox<>(FXCollections.observableArrayList("All Conditions"));
        conditionFilter.getItems().addAll(DummyDataService.CONDITIONS);
        conditionFilter.setPrefWidth(185);
        conditionFilter.getSelectionModel().select(browseCondition);
        conditionFilter.setOnAction(e -> {
            browseCondition = conditionFilter.getValue();
            showBrowse();
        });

        ComboBox<String> sortFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Newest", "Price: Low to High", "Price: High to Low", "Title A-Z"));
        sortFilter.setPrefWidth(170);
        sortFilter.getSelectionModel().select(browseSort);
        sortFilter.setOnAction(e -> {
            browseSort = sortFilter.getValue();
            showBrowse();
        });

        TextField searchField = new TextField(browseSearch);
        searchField.setPromptText("Search listings...");
        searchField.setPrefWidth(200);
        searchField.setOnAction(e -> {
            browseSearch = searchField.getText();
            showBrowse();
        });

        TextField minPrice = new TextField(browseMinPrice);
        minPrice.setPromptText("Min HK$");
        minPrice.setPrefWidth(90);
        TextField maxPrice = new TextField(browseMaxPrice);
        maxPrice.setPromptText("Max HK$");
        maxPrice.setPrefWidth(90);
        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().addAll("btn-primary", "btn-sm");
        applyBtn.setOnAction(e -> {
            browseSearch = searchField.getText();
            browseMinPrice = minPrice.getText();
            browseMaxPrice = maxPrice.getText();
            showBrowse();
        });

        // keep each label next to its control when the bar wraps
        HBox categoryGroup = new HBox(6, new Label("Category:"), categoryFilter);
        categoryGroup.setAlignment(Pos.CENTER_LEFT);
        HBox typeGroup = new HBox(6, new Label("Type:"), typeFilter);
        typeGroup.setAlignment(Pos.CENTER_LEFT);
        HBox conditionGroup = new HBox(6, new Label("Condition:"), conditionFilter);
        conditionGroup.setAlignment(Pos.CENTER_LEFT);
        HBox sortGroup = new HBox(6, new Label("Sort:"), sortFilter);
        sortGroup.setAlignment(Pos.CENTER_LEFT);
        HBox priceGroup = new HBox(6, new Label("Price:"), minPrice, new Label("-"), maxPrice);
        priceGroup.setAlignment(Pos.CENTER_LEFT);

        FlowPane filterBar = new FlowPane(12, 8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getChildren().addAll(categoryGroup, typeGroup, conditionGroup, sortGroup,
                priceGroup, searchField, applyBtn);
        root.getChildren().add(filterBar);

        // Listing grid built from the filtered + sorted list
        FlowPane grid = new FlowPane(16, 16);
        grid.setPrefWrapLength(900);

        List<Listing> filtered = applyBrowseFilters();
        if (filtered.isEmpty()) {
            Label empty = new Label(DummyDataService.getInstance().getLiveListings().isEmpty()
                    ? "No live listings at the moment."
                    : "No listings match your filters.");
            empty.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 15px;");
            root.getChildren().add(empty);
        } else {
            for (Listing listing : filtered) {
                grid.getChildren().add(createListingCard(listing));
            }
            root.getChildren().add(grid);
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        contentPane.getChildren().add(scroll);
    }

    /**
     * filters and sorts live listings using the browse* fields (task 1.3)
     */
    private List<Listing> applyBrowseFilters() {
        String keyword = browseSearch == null ? "" : browseSearch.trim().toLowerCase();
        Double min = parsePrice(browseMinPrice);
        Double max = parsePrice(browseMaxPrice);

        List<Listing> result = new ArrayList<>();
        for (Listing l : DummyDataService.getInstance().getLiveListings()) {
            if (!keyword.isEmpty()) {
                String haystack = (l.getTitle() + " " + (l.getDescription() == null ? "" : l.getDescription()))
                        .toLowerCase();
                if (!haystack.contains(keyword)) continue;
            }
            if (!"All Categories".equals(browseCategory) && !Objects.equals(l.getCategory(), browseCategory)) continue;
            if (!"All Types".equals(browseType) && !typeMatches(l, browseType)) continue;
            if (!"All Conditions".equals(browseCondition) && !Objects.equals(l.getCondition(), browseCondition)) continue;
            if (l.getType() == ListingType.SELL && l.getPrice() != null) {
                if (min != null && l.getPrice() < min) continue;
                if (max != null && l.getPrice() > max) continue;
            }
            result.add(l);
        }

        switch (browseSort) {
            case "Price: Low to High" -> result.sort(Comparator.comparing(
                    l -> l.getPrice() != null ? l.getPrice() : Double.MAX_VALUE));
            case "Price: High to Low" -> result.sort(Comparator.comparing(
                    (Listing l) -> l.getPrice() != null ? l.getPrice() : -1.0).reversed());
            case "Title A-Z" -> result.sort(Comparator.comparing(Listing::getTitle,
                    String.CASE_INSENSITIVE_ORDER));
            default -> result.sort(Comparator.comparing(Listing::getPostedAt).reversed());
        }
        return result;
    }

    // price fields are optional, bad input just means no bound
    private Double parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean typeMatches(Listing l, String typeLabel) {
        return switch (typeLabel) {
            case "Sell" -> l.getType() == ListingType.SELL;
            case "Lend" -> l.getType() == ListingType.LEND;
            case "Giveaway" -> l.getType() == ListingType.GIVEAWAY;
            default -> true;
        };
    }

    /**
     * builds one browse card with thumbnail, badges, seller rating and actions (task 1.3)
     */
    private VBox createListingCard(Listing listing) {
        VBox card = new VBox(8);
        card.getStyleClass().add("listing-card");
        card.setPrefWidth(260);
        card.setMinHeight(200);

        // primary photo thumbnail, or a small placeholder when the listing has none
        String photoPath = BrowseService.primaryPhotoPath(listing);
        Image thumb = photoPath == null ? null : loadThumbnail(photoPath);
        if (thumb != null) {
            ImageView thumbView = new ImageView(thumb);
            thumbView.setFitWidth(240);
            thumbView.setFitHeight(110);
            thumbView.setPreserveRatio(true);
            thumbView.setSmooth(true);
            card.getChildren().add(thumbView);
        } else {
            Label noPhoto = new Label("No photo");
            noPhoto.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
            card.getChildren().add(noPhoto);
        }

        // Type badge + price
        HBox top = new HBox(6);
        top.setAlignment(Pos.CENTER_LEFT);
        Label typeBadge = new Label(listing.getTypeDisplay());
        typeBadge.getStyleClass().add("badge");
        if (listing.getType() == ListingType.SELL) typeBadge.getStyleClass().add("badge-sold");
        else if (listing.getType() == ListingType.LEND) typeBadge.getStyleClass().add("badge-pending");
        else typeBadge.getStyleClass().add("badge-live");

        Label availability = new Label(BrowseService.availabilityLabel(listing));
        availability.getStyleClass().add("badge");
        availability.getStyleClass().add("badge-live");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label price = new Label(listing.getPriceDisplay());
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: #003974; -fx-font-size: 16px;");
        top.getChildren().addAll(typeBadge, availability, spacer, price);

        Label title = new Label(listing.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1a1a2e;");
        title.setWrapText(true);

        Label meta = new Label(listing.getCategory() + " · " + listing.getCondition());
        meta.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");

        HBox sellerRow = new HBox(6);
        sellerRow.setAlignment(Pos.CENTER_LEFT);
        Label seller = new Label("Seller: " + listing.getSellerFullName());
        seller.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");
        Label rating = new Label(BrowseService.sellerRatingLabel(listing));
        rating.setStyle("-fx-text-fill: #A68137; -fx-font-size: 12px;");
        sellerRow.getChildren().addAll(seller, rating);
        if (BrowseService.showVerifiedBadge(listing)) {
            Label verified = new Label("HKUST Verified");
            verified.getStyleClass().add("badge");
            verified.getStyleClass().add("badge-live");
            sellerRow.getChildren().add(verified);
        }

        Label location = new Label("📍 " + listing.getMeetupLocation());
        location.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");

        Label date = new Label("Posted: " + listing.getPostedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");

        Button actionBtn = new Button();
        actionBtn.getStyleClass().addAll("btn-primary", "btn-sm");
        switch (listing.getType()) {
            case SELL -> {
                actionBtn.setText("Make Offer");
                actionBtn.setOnAction(e -> showOfferDialog(listing));
            }
            case LEND -> {
                actionBtn.setText("Request Borrow");
                actionBtn.setOnAction(e -> showBorrowDialog(listing));
            }
            case GIVEAWAY -> {
                actionBtn.setText("Claim Item");
                actionBtn.setOnAction(e -> claimItem(listing));
            }
        }

        // a separate button keeps the action demo and the detail screen apart
        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().addAll("btn-outline", "btn-sm");
        detailsBtn.setOnAction(e -> openDetail(listing));

        HBox cardActions = new HBox(8, actionBtn, detailsBtn);
        cardActions.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(top, title, meta, sellerRow, location, date, cardActions);
        return card;
    }

    private Image loadThumbnail(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            // uploaded photos live on the classpath, seed photos may be plain files
            if (path.startsWith("/")) {
                java.io.InputStream stream = getClass().getResourceAsStream(path);
                return stream == null ? null : new Image(stream);
            }
            return new Image(path);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    // opens the step 12 detail screen and passes it the listing id
    private void openDetail(Listing listing) {
        SceneManager.<ListingDetailController>switchScene("common/ListingDetailView.fxml",
                controller -> controller.setListingId(listing.getId()));
    }

    // all three card actions go through OfferService so they really create offers
    private User currentBuyer() {
        try {
            return SessionManager.requireRole(UserRole.BUYER);
        } catch (SecurityException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            return null;
        }
    }

    private void showOfferDialog(Listing listing) {
        User buyer = currentBuyer();
        if (buyer == null) return;
        TextInputDialog dialog = new TextInputDialog(String.valueOf(listing.getPrice() != null ? listing.getPrice() : 0));
        dialog.setTitle("Make an Offer");
        dialog.setHeaderText("Offer for: " + listing.getTitle());
        dialog.setContentText("Your offer price (HK$):");
        dialog.showAndWait().ifPresent(priceStr -> {
            try {
                double price = Double.parseDouble(priceStr);
                OfferService.submitOffer(listing, buyer, price, null, null, null);
                new Alert(Alert.AlertType.INFORMATION,
                        "Your offer has been sent to " + listing.getSellerFullName() + ".").showAndWait();
                showBrowse();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid price.").showAndWait();
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.WARNING,
                        ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
            }
        });
    }

    private void showBorrowDialog(Listing listing) {
        User buyer = currentBuyer();
        if (buyer == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Borrow Request");
        dialog.setHeaderText("Request to borrow: " + listing.getTitle());

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(
                listing.getLendingDays() == null ? 7 : listing.getLendingDays()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Start date"), 0, 0);
        grid.add(startPicker, 1, 0);
        grid.add(new Label("End date"), 0, 1);
        grid.add(endPicker, 1, 1);
        Label hint = new Label("minimum borrow is 5 seconds for the demo, same day is fine");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");
        grid.add(hint, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType send = new ButtonType("Send request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(send, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(button -> {
            if (button != send) return;
            try {
                OfferService.submitOffer(listing, buyer, null,
                        startPicker.getValue(), endPicker.getValue(), null);
                new Alert(Alert.AlertType.INFORMATION,
                        "Borrow request sent to " + listing.getSellerFullName() + ".").showAndWait();
                showBrowse();
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.WARNING,
                        ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
            }
        });
    }

    private void claimItem(Listing listing) {
        User buyer = currentBuyer();
        if (buyer == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Claim Item");
        alert.setHeaderText("Claim: " + listing.getTitle());
        alert.setContentText("Are you sure you want to claim this giveaway item?");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    OfferService.claimGiveaway(listing, buyer);
                    new Alert(Alert.AlertType.INFORMATION,
                            "Item claimed successfully!").showAndWait();
                    showBrowse();
                } catch (RuntimeException ex) {
                    new Alert(Alert.AlertType.WARNING,
                            ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
                }
            }
        });
    }

    /**
     * My Deals (task 1.7): accepted offers, borrows and claims, plus past deals
     * each card can reopen the listing and chat; borrows show a due date and
     * Return early, and a completed purchase or claim opens the review dialog
     */
    @FXML
    private void showMyDeals() {
        setActive(btnDeals, "My Deals");
        contentPane.getChildren().clear();

        VBox root = new VBox(12);
        root.setPadding(new Insets(4));

        Label info = new Label("Your accepted offers, borrowed items and claimed giveaways");
        info.setStyle("-fx-text-fill: #5a5a7a;");
        root.getChildren().add(info);

        User me = SessionManager.getCurrentUser();
        List<Deal> deals = me == null ? new ArrayList<>()
                : new ArrayList<>(DummyDataService.getInstance().getDealsForBuyer(me.getUsername()));

        // countered offers are not deals yet, show them first so the buyer can accept
        List<Offer> countered = me == null ? new ArrayList<>()
                : DummyDataService.getInstance().getOffersForBuyer(me.getUsername()).stream()
                        .filter(o -> o.getStatus() == OfferStatus.COUNTERED)
                        .toList();
        if (!countered.isEmpty()) {
            Label counterHeading = new Label("Counter offers waiting for you");
            counterHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #003974;");
            root.getChildren().add(counterHeading);
            for (Offer offer : countered) {
                root.getChildren().add(createCounterCard(offer));
            }
        }
        // active deals first since they still need something from the buyer, then newest
        deals.sort(Comparator
                .comparing((Deal d) -> d.getStatus() != DealStatus.ACTIVE)
                .thenComparing(Deal::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        if (deals.isEmpty() && countered.isEmpty()) {
            Label empty = new Label("No deals yet. Make an offer, borrow or claim an item from Browse.");
            empty.setStyle("-fx-text-fill: #5a5a7a;");
            root.getChildren().add(empty);
        } else {
            for (Deal deal : deals) {
                root.getChildren().add(createDealCard(deal));
            }
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        contentPane.getChildren().add(scroll);
    }

    /**
     * shows a seller counter offer with a button to accept it
     */
    private VBox createCounterCard(Offer offer) {
        Listing listing = DummyDataService.getInstance().findListing(offer.getListingId());
        String title = listing != null ? listing.getTitle() : "Listing " + offer.getListingId();

        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label terms = new Label(offer.getType() == ListingType.SELL
                ? "Counter price: HK$" + String.format("%.0f", offer.getCounterPrice())
                : "New return date: " + offer.getCounterEnd());
        terms.setStyle("-fx-text-fill: #1a1a2e; -fx-font-size: 13px;");

        Button accept = new Button("Accept counter");
        accept.getStyleClass().addAll("btn-success", "btn-sm");
        accept.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Accept the seller's counter for '" + title + "'?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
            try {
                OfferService.acceptCounter(offer.getId());
                new Alert(Alert.AlertType.INFORMATION, "Counter accepted. Deal created.").showAndWait();
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.WARNING,
                        ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
            }
            showMyDeals();
        });

        card.getChildren().addAll(titleLabel, terms, accept);
        return card;
    }

    private VBox createDealCard(Deal deal) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd MMM HH:mm");
        ListingType type = deal.getDealType();
        DealStatus status = deal.getStatus();
        boolean active = status == DealStatus.ACTIVE;

        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));
        // deals that fell through stay in the list but greyed out
        if (status == DealStatus.CANCELLED || status == DealStatus.EXPIRED) {
            card.setOpacity(0.6);
        }

        HBox heading = new HBox(10);
        heading.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(deal.getListingTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label typeBadge = new Label(type == ListingType.GIVEAWAY
                ? "Giveaway" : type == ListingType.LEND ? "Borrow" : "Buy");
        typeBadge.getStyleClass().addAll("badge",
                type == ListingType.SELL ? "badge-sold"
                        : type == ListingType.LEND ? "badge-pending" : "badge-live");
        String statusText = status == null ? "Unknown"
                : status.name().charAt(0) + status.name().substring(1).toLowerCase();
        Label statusBadge = new Label(statusText);
        statusBadge.getStyleClass().addAll("badge",
                active || status == DealStatus.PENDING ? "badge-pending"
                        : status == DealStatus.COMPLETED || status == DealStatus.RETURNED
                        ? "badge-live" : "badge-rejected");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        heading.getChildren().addAll(titleLabel, typeBadge, spacer, statusBadge);

        User seller = DummyDataService.getInstance().findUser(deal.getSellerUsername());
        String sellerName = seller != null && seller.getFullName() != null && !seller.getFullName().isBlank()
                ? seller.getFullName() : deal.getSellerUsername();
        Label from = new Label("Seller: " + sellerName + " (@" + deal.getSellerUsername() + ")");
        from.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");

        String termsText;
        if (type == ListingType.LEND) {
            termsText = deal.getBorrowStart() == null ? "Borrowed item"
                    : "Borrowed from " + deal.getBorrowStart().format(dateFormat);
            if (deal.getDueAt() != null) {
                termsText += "  ·  Due back " + deal.getDueAt().format(dateFormat);
            } else if (deal.getBorrowEnd() != null) {
                termsText += "  ·  Due back " + deal.getBorrowEnd().format(dateFormat);
            }
        } else if (type == ListingType.GIVEAWAY) {
            termsText = "Free giveaway";
        } else {
            termsText = deal.getOfferPrice() == null ? "Purchase"
                    : "Agreed price: HK$" + String.format("%.0f", deal.getOfferPrice());
        }
        Label terms = new Label(termsText);
        terms.setStyle("-fx-text-fill: #1a1a2e; -fx-font-size: 13px;");

        String meta = "Started " + (deal.getCreatedAt() == null ? "recently"
                : deal.getCreatedAt().format(timeFormat));
        if (deal.getCompletedAt() != null) {
            meta += "  ·  " + (status == DealStatus.RETURNED ? "Returned " : "Closed ")
                    + deal.getCompletedAt().format(timeFormat);
        } else if (active && type == ListingType.LEND && deal.getDueAt() != null
                && deal.getDueAt().isBefore(LocalDateTime.now())) {
            meta += "  ·  Overdue, please return it";
        }
        Label metaLabel = new Label(meta);
        metaLabel.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        // a removed listing has no detail screen or chat left to open
        boolean listingExists = DummyDataService.getInstance().findListing(deal.getListingId()) != null;

        Button view = new Button("View listing");
        view.getStyleClass().addAll("btn-outline", "btn-sm");
        view.setDisable(!listingExists);
        view.setOnAction(e -> SceneManager.<ListingDetailController>switchScene(
                "common/ListingDetailView.fxml", c -> c.setListingId(deal.getListingId())));

        Button chat = new Button("Open chat");
        chat.getStyleClass().addAll("btn-outline", "btn-sm");
        chat.setDisable(!listingExists);
        chat.setOnAction(e -> SceneManager.<ListingDetailController>switchScene(
                "common/ListingDetailView.fxml", c -> {
                    c.setListingId(deal.getListingId());
                    c.focusChat();
                }));

        actions.getChildren().addAll(view, chat);

        if (active && type == ListingType.LEND) {
            Button returnEarly = new Button("Return early");
            returnEarly.getStyleClass().addAll("btn-gold", "btn-sm");
            returnEarly.setOnAction(e -> runDealAction(
                    "Return '" + deal.getListingTitle() + "' now? The listing goes live again.",
                    "Item returned. " + sellerName + " has been notified.",
                    () -> DealService.returnEarly(deal.getId())));
            actions.getChildren().add(returnEarly);
        } else if (active) {
            Button complete = new Button("Mark complete");
            complete.getStyleClass().addAll("btn-success", "btn-sm");
            complete.setOnAction(e -> {
                boolean done = runDealAction(
                        "Confirm you received '" + deal.getListingTitle() + "'? This closes the deal.",
                        "Deal completed. You can now rate " + sellerName + ".",
                        () -> DealService.markComplete(deal.getId()));
                if (done) {
                    showReviewDialog(deal);
                }
            });
            actions.getChildren().add(complete);
        } else if (status == DealStatus.COMPLETED && !deal.isRated()) {
            Button review = new Button("Leave review");
            review.getStyleClass().addAll("btn-gold", "btn-sm");
            review.setOnAction(e -> showReviewDialog(deal));
            actions.getChildren().add(review);
        } else if (status == DealStatus.COMPLETED) {
            Label reviewed = new Label("Reviewed");
            reviewed.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px;");
            actions.getChildren().add(reviewed);
        }

        card.getChildren().addAll(heading, from, terms, metaLabel, actions);
        return card;
    }

    // asks first, runs the service call, then redraws so the new status shows
    private boolean runDealAction(String question, String doneMessage, Runnable action) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, question);
        confirm.setTitle("My Deals");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return false;
        }
        boolean done = false;
        try {
            action.run();
            done = true;
            new Alert(Alert.AlertType.INFORMATION, doneMessage).showAndWait();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
        }
        showMyDeals();
        return done;
    }

    /**
     * star rating and comment for a completed deal (step 22)
     */
    private void showReviewDialog(Deal deal) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rate Seller");
        dialog.setHeaderText("How was your deal for '" + deal.getListingTitle() + "'?");

        ComboBox<Integer> ratingBox = new ComboBox<>(FXCollections.observableArrayList(5, 4, 3, 2, 1));
        ratingBox.setValue(5);
        ratingBox.setButtonCell(starCell());
        ratingBox.setCellFactory(list -> starCell());

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Optional comment for the seller");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);

        VBox content = new VBox(8, new Label("Rating"), ratingBox, new Label("Comment"), commentArea);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType submit = new ButtonType("Submit review", ButtonBar.ButtonData.OK_DONE);
        ButtonType later = new ButtonType("Later", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submit, later);

        if (dialog.showAndWait().orElse(later) != submit) {
            return;
        }
        try {
            ReviewService.submitReview(deal, SessionManager.getCurrentUser(),
                    ratingBox.getValue() == null ? 0 : ratingBox.getValue(), commentArea.getText());
            new Alert(Alert.AlertType.INFORMATION, "Thanks, your review was posted.").showAndWait();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Could not post the review." : ex.getMessage()).showAndWait();
        }
        showMyDeals();
    }

    private ListCell<Integer> starCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null
                        : "★".repeat(value) + "☆".repeat(5 - value) + "  (" + value + ")");
            }
        };
    }

    /**
     * loads the dedicated deal history screen (step 25)
     */
    @FXML
    private void showHistory() {
        setActive(btnHistory, "Deal History");
        contentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/buyer/DealHistoryView.fxml"));
            contentPane.getChildren().add(loader.load());
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not load deal history.\n" + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showFavourites() {
        setActive(btnFavourites, "Favourites & Following");
        contentPane.getChildren().clear();
        VBox root = new VBox(16);
        root.setPadding(new Insets(4));
        root.setAlignment(Pos.CENTER);
        Label icon = new Label("♥");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #A68137;");
        Label title = new Label("Favourites, Bookmarks & Following");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #003974;");
        Label desc = new Label("Save listings you like and follow favourite sellers.\nThis section is a UI placeholder for students to implement.");
        desc.setStyle("-fx-text-fill: #5a5a7a; -fx-text-alignment: center;");
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        root.getChildren().addAll(icon, title, desc);
        contentPane.getChildren().add(root);
    }

    /**
     * opens the shared notification board (task 1.9)
     */
    @FXML
    private void showNotifications() {
        // the board handles read state and the category filter itself
        setActive(btnNotifications, "Notification Board");
        contentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/common/NotificationBoardView.fxml"));
            javafx.scene.Parent root = loader.load();
            com.hkust.marketplace.controllers.common.NotificationBoardController board =
                    loader.getController();
            if (board != null) {
                board.setOnChanged(this::updateNotificationBadge);
            }
            contentPane.getChildren().add(root);
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not load the notification board.\n" + ex.getMessage()).showAndWait();
        }
    }

    /**
     * Manage Profile (task 1.8)
     * edits full name and, when typed, the password using the registration rules
     * success and failure are alert dialogs; residence hall can be changed too
     */
    @FXML
    private void showProfile() {
        setActive(btnProfile, "Manage Profile");
        contentPane.getChildren().clear();

        User user = SessionManager.getCurrentUser();
        VBox form = new VBox(14);
        form.setPadding(new Insets(8));
        form.setMaxWidth(420);
        form.getStyleClass().add("card");

        Label title = new Label("Edit your profile");
        title.getStyleClass().add("section-title");

        TextField nameField = new TextField(user != null ? user.getFullName() : "");
        nameField.setPromptText("Full Name");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password (leave blank to keep)");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");
        ComboBox<String> residence = new ComboBox<>(FXCollections.observableArrayList(
                "Hall 1", "Hall 2", "Hall 3", "Hall 4", "Hall 5",
                "Hall 6", "Hall 7", "Hall 8", "Hall 9", "UG Hall 1", "UG Hall 2", "Off-campus"));
        if (user != null && user.getResidenceHall() != null) {
            residence.setValue(user.getResidenceHall());
        }

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            // someone has to be logged in for this to make sense
            if (user == null) {
                new Alert(Alert.AlertType.WARNING, "You are not logged in.").showAndWait();
                return;
            }
            // grab the values from the form
            String newName = nameField.getText().trim();
            String newPassword = newPass.getText();
            // the full name still cannot be blank
            if (newName.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Full Name cannot be empty.").showAndWait();
                return;
            }
            // only touch the password if the user actually typed one
            if (!newPassword.isEmpty()) {
                // same rules as registration
                String pwdCheck = DummyDataService.checkPasswordStrength(newPassword);
                if (!"OK".equals(pwdCheck)) {
                    new Alert(Alert.AlertType.WARNING, pwdCheck).showAndWait();
                    return;
                }
                // both password boxes have to match
                if (!newPassword.equals(confirmPass.getText())) {
                    new Alert(Alert.AlertType.WARNING, "Passwords do not match.").showAndWait();
                    return;
                }
                // hash before saving, never keep the raw one
                user.setPasswordHash(PasswordUtil.hash(newPassword));
            }
            // these fields can be updated straight away
            user.setFullName(newName);
            user.setResidenceHall(residence.getValue());
            new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!").showAndWait();
        });

        form.getChildren().addAll(title,
                new Label("Full Name"), nameField,
                new Label("New Password"), newPass,
                new Label("Confirm Password"), confirmPass,
                new Label("Residence"), residence,
                saveBtn);

        contentPane.getChildren().add(form);
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        SceneManager.showWelcome();
    }
}
