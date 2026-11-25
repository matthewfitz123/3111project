package com.hkust.marketplace.controllers.seller;

import com.hkust.marketplace.models.*;
import com.hkust.marketplace.controllers.common.ListingDetailController;
import com.hkust.marketplace.services.ListingService;
import com.hkust.marketplace.services.OfferService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import com.hkust.marketplace.utils.SceneManager;
import com.hkust.marketplace.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SellerDashboardController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label welcomeLabel;
    @FXML private Button btnListings, btnCreate, btnOffers, btnStats, btnReviews, btnNotifications, btnProfile;

    private Button currentActiveBtn;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) welcomeLabel.setText("Hello, " + user.getFullName());
        currentActiveBtn = btnListings;
        showMyListings();
        updateNotificationBadge();
    }

    /**
     * refreshes the unread count shown in the sidebar
     */
    private void updateNotificationBadge() {
        User user = SessionManager.getCurrentUser();
        int unread = user == null ? 0
                : DummyDataService.getInstance().getUnreadNotificationCount(user.getUsername());
        btnNotifications.setText("  Notifications" + (unread > 0 ? " (" + unread + ")" : ""));
    }

    private void setActive(Button btn, String title) {
        if (currentActiveBtn != null) currentActiveBtn.getStyleClass().remove("sidebar-btn-active");
        btn.getStyleClass().add("sidebar-btn-active");
        currentActiveBtn = btn;
        pageTitle.setText(title);
    }

    @FXML
    private void showMyListings() {
        setActive(btnListings, "My Listings");
        contentPane.getChildren().clear();

        VBox root = new VBox(14);
        root.setPadding(new Insets(4));

        User user = SessionManager.getCurrentUser();
        List<Listing> myListings = DummyDataService.getInstance()
                .getListingsBySeller(user != null ? user.getUsername() : "");

        if (myListings.isEmpty()) {
            Label empty = new Label("You have not posted any listings yet.");
            empty.setStyle("-fx-text-fill: #5a5a7a;");
            root.getChildren().add(empty);
        } else {
            for (Listing l : myListings) {
                root.getChildren().add(createListingRow(l));
            }
        }
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        contentPane.getChildren().add(scroll);
    }

    private HBox createListingRow(Listing l) {
        HBox row = new HBox(16);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label title = new Label(l.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label meta = new Label(l.getCategory() + " · " + l.getTypeDisplay() + " · " + l.getMeetupLocation());
        meta.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");
        Label stats = new Label("Views: " + l.getViewCount() + " · Posted: " + formatPostedAt(l));
        stats.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");

        info.getChildren().addAll(title, meta, stats);
        String detail = statusDetail(l);
        if (detail != null) {
            Label detailLabel = new Label(detail);
            detailLabel.setWrapText(true);
            detailLabel.setStyle(l.getStatus() == ListingStatus.REJECTED
                    ? "-fx-text-fill: #c62828; -fx-font-size: 12px;"
                    : "-fx-text-fill: #ed6c02; -fx-font-size: 12px;");
            info.getChildren().add(detailLabel);
        }

        Label status = new Label(l.getStatus().name().replace('_', ' '));
        status.getStyleClass().add("badge");
        switch (l.getStatus()) {
            case LIVE -> status.getStyleClass().add("badge-live");
            case PENDING_REVIEW -> status.getStyleClass().add("badge-pending");
            case REJECTED -> status.getStyleClass().add("badge-rejected");
            default -> status.getStyleClass().add("badge-sold");
        }

        // sellers can open the same detail/chat screen the buyer uses
        Button open = new Button("Open");
        open.getStyleClass().addAll("btn-outline", "btn-sm");
        open.setOnAction(e -> SceneManager.<ListingDetailController>switchScene(
                "common/ListingDetailView.fxml", controller -> controller.setListingId(l.getId())));

        // edit and delete are only allowed when nothing is in flight
        boolean canModify = canModify(l);
        String lockReason = lockReason(l);

        Button edit = new Button("Edit");
        edit.getStyleClass().addAll("btn-outline", "btn-sm");
        edit.setDisable(!canModify);
        edit.setOnAction(e -> editListing(l));
        if (!canModify) {
            edit.setTooltip(new Tooltip(lockReason));
        }

        Button delete = new Button("Delete");
        delete.getStyleClass().addAll("btn-danger", "btn-sm");
        delete.setDisable(!canModify);
        delete.setOnAction(e -> deleteListing(l));
        if (!canModify) {
            delete.setTooltip(new Tooltip(lockReason));
        }

        HBox actions = new HBox(8, open, edit, delete);
        actions.setAlignment(Pos.CENTER_RIGHT);
        row.getChildren().addAll(info, status, actions);
        return row;
    }

    /**
     * true when the status allows edits and no offer is waiting
     */
    private boolean canModify(Listing listing) {
        return ListingService.canModify(listing);
    }

    /**
     * counts only offers that still need an answer
     */
    private long pendingOfferCount(Listing listing) {
        return ListingService.pendingOfferCount(listing);
    }

    /**
     * short reason for the disabled edit and delete buttons
     */
    private String lockReason(Listing listing) {
        if (listing.getStatus() != ListingStatus.LIVE
                && listing.getStatus() != ListingStatus.PENDING_REVIEW
                && listing.getStatus() != ListingStatus.REJECTED) {
            return "A " + listing.getStatus().name().toLowerCase().replace('_', ' ')
                    + " listing cannot be edited or deleted.";
        }
        if (pendingOfferCount(listing) > 0) {
            return "Answer or decline the pending offers before changing this listing.";
        }
        return "";
    }

    /**
     * small dialog for the fields a seller can change after posting
     */
    private void editListing(Listing listing) {
        if (!canModify(listing)) {
            new Alert(Alert.AlertType.WARNING, lockReason(listing)).showAndWait();
            return;
        }

        // past offers do not block an edit, but the seller should know about them
        long historyBuyers = DummyDataService.getInstance().getOffersForListing(listing.getId())
                .stream().map(Offer::getBuyerUsername).distinct().count();
        if (historyBuyers > 0) {
            Alert historyWarning = new Alert(Alert.AlertType.CONFIRMATION);
            historyWarning.setTitle("Edit Listing");
            historyWarning.setHeaderText("Edit: " + listing.getTitle());
            historyWarning.setContentText(historyBuyers
                    + " buyer(s) have offer history for this listing. Edit anyway?");
            if (historyWarning.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Listing");
        dialog.setHeaderText("Edit: " + listing.getTitle());

        TextField titleField = new TextField(listing.getTitle());
        TextArea descriptionField = new TextArea(listing.getDescription() == null
                ? "" : listing.getDescription());
        descriptionField.setPrefRowCount(3);

        ComboBox<String> categoryBox = new ComboBox<>(
                FXCollections.observableArrayList(DummyDataService.CATEGORIES));
        categoryBox.setValue(listing.getCategory());
        ComboBox<String> conditionBox = new ComboBox<>(
                FXCollections.observableArrayList(DummyDataService.CONDITIONS));
        conditionBox.setValue(listing.getCondition());
        ComboBox<String> meetupBox = new ComboBox<>(
                FXCollections.observableArrayList(DummyDataService.HKUST_LOCATIONS));
        meetupBox.setValue(listing.getMeetupLocation());

        TextField priceField = new TextField(listing.getPrice() == null
                ? "" : String.format("%.0f", listing.getPrice()));
        TextField daysField = new TextField(listing.getLendingDays() == null
                ? "" : String.valueOf(listing.getLendingDays()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(9);
        int row = 0;
        grid.add(new Label("Title"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Category"), 0, row);
        grid.add(categoryBox, 1, row++);
        grid.add(new Label("Condition"), 0, row);
        grid.add(conditionBox, 1, row++);
        if (listing.getType() == ListingType.SELL) {
            grid.add(new Label("Price (HK$)"), 0, row);
            grid.add(priceField, 1, row++);
        }
        if (listing.getType() == ListingType.LEND) {
            grid.add(new Label("Lending days"), 0, row);
            grid.add(daysField, 1, row++);
        }
        grid.add(new Label("Meet-up"), 0, row);
        grid.add(meetupBox, 1, row++);
        grid.add(new Label("Description"), 0, row);
        grid.add(descriptionField, 1, row);
        dialog.getDialogPane().setContent(grid);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(button -> {
            if (button != save) {
                return;
            }
            String title = titleField.getText() == null ? "" : titleField.getText().trim();
            if (title.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Title cannot be empty.").showAndWait();
                return;
            }
            if (categoryBox.getValue() == null || conditionBox.getValue() == null
                    || meetupBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Pick a category, condition and meet-up spot.").showAndWait();
                return;
            }

            Double price = listing.getPrice();
            Integer days = listing.getLendingDays();
            if (listing.getType() == ListingType.SELL) {
                price = parsePositiveDouble(priceField.getText());
                if (price == null) {
                    new Alert(Alert.AlertType.WARNING, "Enter a price above zero.").showAndWait();
                    return;
                }
            }
            if (listing.getType() == ListingType.LEND) {
                days = parsePositiveInt(daysField.getText());
                if (days == null) {
                    new Alert(Alert.AlertType.WARNING, "Enter a lending period above zero.").showAndWait();
                    return;
                }
            }

            try {
                ListingService.editListing(listing.getId(), title, descriptionField.getText(),
                        categoryBox.getValue(), conditionBox.getValue(), meetupBox.getValue(),
                        price, days);
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            }
            showMyListings();
        });
    }

    /**
     * deletes a listing after confirmation, warning about offer history
     */
    private void deleteListing(Listing listing) {
        if (!canModify(listing)) {
            new Alert(Alert.AlertType.WARNING, lockReason(listing)).showAndWait();
            return;
        }

        List<Offer> history = DummyDataService.getInstance().getOffersForListing(listing.getId());
        long buyers = history.stream().map(Offer::getBuyerUsername).distinct().count();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Listing");
        confirm.setHeaderText("Delete: " + listing.getTitle());
        confirm.setContentText(buyers == 0
                ? "This listing has no offer history. Delete it?"
                : buyers + " buyer(s) have offer history for this listing. Delete anyway?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            ListingService.deleteListing(listing.getId());
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
        }
        showMyListings();
    }

    // tiny parser so bad input becomes a validation error, not a crash
    private Double parsePositiveDouble(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // same idea for the lending days field
    private Integer parsePositiveInt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String formatPostedAt(Listing listing) {
        return listing.getPostedAt() == null
                ? "Unknown"
                : listing.getPostedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }

    private String statusDetail(Listing listing) {
        return switch (listing.getStatus()) {
            case PENDING_REVIEW -> "Awaiting admin review.";
            case REJECTED -> listing.getRejectionReason() == null || listing.getRejectionReason().isBlank()
                    ? "Rejected by admin."
                    : "Reason: " + listing.getRejectionReason();
            default -> null;
        };
    }

    @FXML
    private void showCreateListing() {
        setActive(btnCreate, "Create New Listing");
        contentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/seller/CreateListingView.fxml"));
            Parent root = loader.load();
            CreateListingController controller = loader.getController();
            // return to My Listings once the listing is submitted
            controller.setOnSubmittedCallback(this::showMyListings);
            contentPane.getChildren().add(root);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not load the create-listing form.\n" + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showOffers() {
        setActive(btnOffers, "Offer Management");
        contentPane.getChildren().clear();

        User user = SessionManager.getCurrentUser();
        List<Offer> pending = DummyDataService.getInstance()
                .getPendingOffersForSeller(user != null ? user.getUsername() : "");

        VBox root = new VBox(12);
        root.setPadding(new Insets(4));

        if (pending.isEmpty()) {
            Label empty = new Label("No pending offers right now.");
            empty.setStyle("-fx-text-fill: #5a5a7a;");
            root.getChildren().add(empty);
        } else {
            for (Offer offer : pending) {
                root.getChildren().add(createOfferCard(offer));
            }
        }
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        contentPane.getChildren().add(scroll);
    }

    private VBox createOfferCard(Offer offer) {
        Listing listing = DummyDataService.getInstance().findListing(offer.getListingId());
        String title = listing != null ? listing.getTitle() : "Listing " + offer.getListingId();

        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));

        HBox heading = new HBox(10);
        heading.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label typeBadge = new Label(offer.getType() == ListingType.GIVEAWAY
                ? "Giveaway" : offer.getType() == ListingType.LEND ? "Lend" : "Sell");
        typeBadge.getStyleClass().addAll("badge",
                offer.getType() == ListingType.SELL ? "badge-sold"
                        : offer.getType() == ListingType.LEND ? "badge-pending" : "badge-live");
        heading.getChildren().addAll(titleLabel, typeBadge);

        User buyer = DummyDataService.getInstance().findUser(offer.getBuyerUsername());
        String buyerName = buyer != null && buyer.getFullName() != null && !buyer.getFullName().isBlank()
                ? buyer.getFullName() : offer.getBuyerUsername();
        Label from = new Label("From " + buyerName + " (@" + offer.getBuyerUsername() + ")");
        from.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");

        Label terms = new Label(offerTerms(offer));
        terms.setStyle("-fx-text-fill: #1a1a2e; -fx-font-size: 13px;");

        String meta = "Sent " + (offer.getCreatedAt() == null ? "recently"
                : offer.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
        if (offer.getMessage() != null && !offer.getMessage().isBlank()) {
            meta += "  ·  \"" + offer.getMessage() + "\"";
        }
        Label metaLabel = new Label(meta);
        metaLabel.setWrapText(true);
        metaLabel.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button accept = new Button("Accept");
        accept.getStyleClass().addAll("btn-success", "btn-sm");
        accept.setOnAction(e -> runOfferAction(() -> OfferService.acceptOffer(offer.getId())));

        Button decline = new Button("Decline");
        decline.getStyleClass().addAll("btn-danger", "btn-sm");
        decline.setOnAction(e -> runOfferAction(() -> OfferService.declineOffer(offer.getId())));

        Button chat = new Button("Open Chat");
        chat.getStyleClass().addAll("btn-outline", "btn-sm");
        chat.setOnAction(e -> SceneManager.<ListingDetailController>switchScene(
                "common/ListingDetailView.fxml", c -> c.setListingId(offer.getListingId())));

        actions.getChildren().addAll(accept, decline, chat);
        if (offer.getType() != ListingType.GIVEAWAY) {
            Button counter = new Button("Counter");
            counter.getStyleClass().addAll("btn-outline", "btn-sm");
            counter.setOnAction(e -> promptCounter(offer, title));
            actions.getChildren().add(1, counter);
        }

        card.getChildren().addAll(heading, from, terms, metaLabel, actions);
        return card;
    }

    private String offerTerms(Offer offer) {
        return switch (offer.getType()) {
            case SELL -> "Offer: HK$" + String.format("%.0f", offer.getOfferPrice());
            case LEND -> "Borrow: " + offer.getBorrowStart() + " to " + offer.getBorrowEnd();
            case GIVEAWAY -> "Giveaway claim";
        };
    }

    // runs a service call, shows the error and refreshes on success
    private void runOfferAction(Runnable action) {
        try {
            action.run();
            showOffers();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
        }
    }

    // counter dialog: price for sell, new return date for lend
    private void promptCounter(Offer offer, String title) {
        if (offer.getType() == ListingType.SELL) {
            TextInputDialog dialog = new TextInputDialog(
                    offer.getOfferPrice() == null ? "" : String.format("%.0f", offer.getOfferPrice()));
            dialog.setTitle("Counter Offer");
            dialog.setHeaderText("Counter for: " + title);
            dialog.setContentText("Your counter price (HK$):");
            dialog.showAndWait().ifPresent(text -> {
                try {
                    double price = Double.parseDouble(text.trim());
                    runOfferAction(() -> OfferService.counterOffer(offer.getId(), price, null));
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.WARNING, "Enter a valid price.").showAndWait();
                }
            });
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Counter Offer");
        dialog.setHeaderText("Counter for: " + title);

        DatePicker endPicker = new DatePicker(
                offer.getCounterEnd() != null ? offer.getCounterEnd()
                        : offer.getBorrowEnd() != null ? offer.getBorrowEnd()
                        : LocalDate.now().plusDays(7));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("New return date"), 0, 0);
        grid.add(endPicker, 1, 0);
        dialog.getDialogPane().setContent(grid);

        ButtonType send = new ButtonType("Send counter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(send, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(button -> {
            if (button == send) {
                runOfferAction(() -> OfferService.counterOffer(offer.getId(), null, endPicker.getValue()));
            }
        });
    }

    @FXML
    private void showStats() {
        setActive(btnStats, "Seller Stats");
        contentPane.getChildren().clear();

        VBox root = new VBox(20);
        root.setPadding(new Insets(4));

        User user = SessionManager.getCurrentUser();
        List<Listing> myListings = DummyDataService.getInstance()
                .getListingsBySeller(user != null ? user.getUsername() : "");

        // Metric cards
        HBox metrics = new HBox(16);
        metrics.getChildren().addAll(
                metricCard("Listings", String.valueOf(myListings.size()), "#003974"),
                metricCard("Avg Rating", String.format("%.1f", user != null ? user.getAverageRating() : 0), "#A68137"),
                metricCard("Reviews", String.valueOf(user != null ? user.getRatingCount() : 0), "#2e7d32"),
                metricCard("Views (demo)", "128", "#1565c0")
        );
        root.getChildren().add(metrics);

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Listings by Category (demo data)");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(280);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Textbooks", 3));
        series.getData().add(new XYChart.Data<>("Electronics", 5));
        series.getData().add(new XYChart.Data<>("Furniture", 2));
        series.getData().add(new XYChart.Data<>("Others", 1));
        barChart.getData().add(series);

        // Pie chart
        PieChart pie = new PieChart();
        pie.setTitle("Listing Status Distribution");
        pie.setPrefHeight(280);
        pie.getData().add(new PieChart.Data("Live", 4));
        pie.getData().add(new PieChart.Data("Pending", 1));
        pie.getData().add(new PieChart.Data("Sold", 2));

        HBox charts = new HBox(20, barChart, pie);
        root.getChildren().add(charts);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        contentPane.getChildren().add(scroll);
    }

    private VBox metricCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setPadding(new Insets(16));
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");
        card.getChildren().addAll(val, lbl);
        return card;
    }

    @FXML
    private void showReviews() {
        // step 23 screen lives in its own controller
        setActive(btnReviews, "Reviews & Feedback");
        contentPane.getChildren().clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/seller/ReviewsView.fxml"));
            contentPane.getChildren().add(root);
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not load the reviews screen.\n" + ex.getMessage()).showAndWait();
        }
    }

    /**
     * loads the shared notification board for the seller
     */
    @FXML
    private void showNotifications() {
        // board handles read state, filters and mark all read
        setActive(btnNotifications, "Notifications");
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

    @FXML
    private void showProfile() {
        setActive(btnProfile, "My Profile");
        contentPane.getChildren().clear();
        User user = SessionManager.getCurrentUser();
        VBox form = new VBox(12);
        form.setMaxWidth(420);
        form.getStyleClass().add("card");
        form.setPadding(new Insets(20));
        form.getChildren().add(new Label("Edit Seller Profile") {{ getStyleClass().add("section-title"); }});

        TextField name = new TextField(user != null ? user.getFullName() : "");
        PasswordField pass = new PasswordField();
        pass.setPromptText("New password (optional)");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");
        TextArea bio = new TextArea(user != null && user.getBio() != null ? user.getBio() : "");
        bio.setPrefRowCount(3);
        ComboBox<String> meetup = new ComboBox<>(FXCollections.observableArrayList(DummyDataService.HKUST_LOCATIONS));
        if (user != null && user.getPreferredMeetup() != null) meetup.setValue(user.getPreferredMeetup());
        meetup.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Save Changes");
        save.getStyleClass().add("btn-gold");
        save.setOnAction(e -> {
            // nothing to update without a session
            if (user == null) {
                new Alert(Alert.AlertType.WARNING, "You are not logged in.").showAndWait();
                return;
            }
            // read the form
            String newName = name.getText().trim();
            String newPassword = pass.getText();
            // name is the one field that has to be there
            if (newName.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Full Name cannot be empty.").showAndWait();
                return;
            }
            // only re-hash when a new password was typed
            if (!newPassword.isEmpty()) {
                String pwdCheck = DummyDataService.checkPasswordStrength(newPassword);
                if (!"OK".equals(pwdCheck)) {
                    new Alert(Alert.AlertType.WARNING, pwdCheck).showAndWait();
                    return;
                }
                // both boxes must match before we save anything
                if (!newPassword.equals(confirmPass.getText())) {
                    new Alert(Alert.AlertType.WARNING, "Passwords do not match.").showAndWait();
                    return;
                }
                // keep the hash, not the password
                user.setPasswordHash(PasswordUtil.hash(newPassword));
            }
            // seller extras are optional so just save what is there
            user.setFullName(newName);
            user.setBio(bio.getText().trim());
            user.setPreferredMeetup(meetup.getValue());
            new Alert(Alert.AlertType.INFORMATION, "Profile saved.").showAndWait();
        });

        Label rating = new Label(user == null || user.getRatingCount() == 0 ? "Rating: no reviews yet"
                : String.format("Rating: %.1f / 5 from %d review(s)",
                user.getAverageRating(), user.getRatingCount()));
        rating.setStyle("-fx-text-fill: #A68137; -fx-font-weight: bold;");

        form.getChildren().addAll(
                rating,
                new Label("Full Name"), name,
                new Label("New Password"), pass,
                new Label("Confirm New Password"), confirmPass,
                new Label("Bio"), bio,
                new Label("Preferred Meet-up"), meetup,
                save);
        contentPane.getChildren().add(form);
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        SceneManager.showWelcome();
    }
}
