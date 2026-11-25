package com.hkust.marketplace.controllers.common;

import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Report;
import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SceneManager;
import com.hkust.marketplace.utils.SessionManager;
import com.hkust.marketplace.services.BrowseService;
import com.hkust.marketplace.services.FavouriteService;
import com.hkust.marketplace.services.NotificationService;
import com.hkust.marketplace.services.OfferService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * listing detail screen for task 1.4 with the task 1.5 buyer actions
 * shows one listing with up to six photos, all fields, seller info,
 * type actions, chat, favourite and report controls
 * opening it also bumps the view counter
 */
public class ListingDetailController {

    private static final DateTimeFormatter POSTED_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML private Label viewerLabel;
    @FXML private Label typeBadge;
    @FXML private Label statusBadge;
    @FXML private Label viewCountLabel;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label metaLabel;
    @FXML private Label actionHintLabel;
    @FXML private Label descriptionLabel;

    @FXML private javafx.scene.control.Button actionButton;
    @FXML private javafx.scene.control.Button chatButton;
    @FXML private javafx.scene.control.Button reportButton;
    @FXML private javafx.scene.control.Button favouriteButton;

    @FXML private FlowPane photoGallery;

    @FXML private Label detailCategory;
    @FXML private Label detailCondition;
    @FXML private Label detailType;
    @FXML private Label detailMeetup;
    @FXML private Label detailStatus;
    @FXML private Label detailPosted;
    @FXML private Label detailViews;
    @FXML private Label lendingLabel;
    @FXML private Label lendingValue;

    @FXML private Label sellerAvatar;
    @FXML private Label sellerNameLabel;
    @FXML private Label sellerUsernameLabel;
    @FXML private Label sellerVerifiedBadge;
    @FXML private Label sellerRatingStars;
    @FXML private Label sellerRatingCountLabel;
    @FXML private VBox sellerReviewsBox;
    @FXML private Label sellerBioLabel;
    @FXML private ChatPanelController chatPanelController;

    private Listing listing;
    private boolean actionSent;

    /**
     * called by scenemanager after the fxml has loaded
     * loads the listing, counts the view and draws the screen
     */
    public void setListingId(String listingId) {
        User viewer = SessionManager.getCurrentUser();
        if (viewer != null) {
            viewerLabel.setText(viewer.getFullName() + "  ·  " + viewer.getRole());
        }

        listing = DummyDataService.getInstance().findListing(listingId);
        if (listing == null) {
            showMissingListing();
            return;
        }

        // opening the detail screen is what counts as a view
        DummyDataService.getInstance().incrementViewCount(listingId);
        render();
    }

    private void render() {
        // basic header info
        typeBadge.setText(listing.getTypeDisplay());
        typeBadge.getStyleClass().setAll("badge", typeStyle(listing.getType()));
        statusBadge.setText(statusText(listing.getStatus()));
        statusBadge.getStyleClass().setAll("badge", statusStyle(listing.getStatus()));
        viewCountLabel.setText("Views: " + listing.getViewCount());
        titleLabel.setText(listing.getTitle());
        priceLabel.setText(listing.getPriceDisplay());
        metaLabel.setText(listing.getCategory() + "  ·  " + listing.getCondition()
                + "  ·  " + listing.getMeetupLocation());

        // the details grid
        detailCategory.setText(listing.getCategory());
        detailCondition.setText(listing.getCondition());
        detailType.setText(listing.getTypeDisplay());
        detailMeetup.setText(listing.getMeetupLocation());
        detailStatus.setText(statusText(listing.getStatus()));
        detailPosted.setText(listing.getPostedAt() == null
                ? "unknown" : POSTED_FORMAT.format(listing.getPostedAt()));
        detailViews.setText(String.valueOf(listing.getViewCount()));

        // only lend listings have a duration worth showing
        boolean isLend = listing.getType() == ListingType.LEND;
        lendingLabel.setVisible(isLend);
        lendingLabel.setManaged(isLend);
        lendingValue.setVisible(isLend);
        lendingValue.setManaged(isLend);
        lendingValue.setText(isLend && listing.getLendingDays() != null
                ? listing.getLendingDays() + " days" : "-");

        descriptionLabel.setText(listing.getDescription() == null
                || listing.getDescription().isBlank()
                ? "No description provided." : listing.getDescription());

        renderPhotos();
        renderSeller();
        renderActions();
        if (chatPanelController != null) {
            chatPanelController.setListing(listing);
        }
    }

    private void renderPhotos() {
        photoGallery.getChildren().clear();
        List<String> paths = BrowseService.photoPaths(listing);
        if (paths == null || paths.isEmpty()) {
            Label none = new Label("No photos uploaded for this listing.");
            none.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
            photoGallery.getChildren().add(none);
            return;
        }
        for (String path : paths) {
            Image image = loadPhoto(path);
            if (image == null) {
                continue;
            }
            ImageView view = new ImageView(image);
            view.setFitWidth(190);
            view.setFitHeight(140);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            // simple wrapper so the images have a little frame
            javafx.scene.layout.StackPane tile = new javafx.scene.layout.StackPane(view);
            tile.setStyle("-fx-background-color: white; -fx-padding: 5;"
                    + " -fx-border-color: #e8f0f8; -fx-border-radius: 6; -fx-background-radius: 6;");
            photoGallery.getChildren().add(tile);
        }
        if (photoGallery.getChildren().isEmpty()) {
            Label broken = new Label("The photos for this listing could not be loaded.");
            broken.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
            photoGallery.getChildren().add(broken);
        }
    }

    private void renderSeller() {
        User seller = DummyDataService.getInstance().findUser(listing.getSellerUsername());
        if (seller == null) {
            sellerAvatar.setText("?");
            sellerNameLabel.setText("Unknown seller");
            sellerUsernameLabel.setText("");
            sellerRatingStars.setText("");
            sellerRatingCountLabel.setText("");
            sellerBioLabel.setText("");
            sellerVerifiedBadge.setVisible(false);
            sellerVerifiedBadge.setManaged(false);
            sellerReviewsBox.getChildren().clear();
            return;
        }
        String name = seller.getFullName() == null ? seller.getUsername() : seller.getFullName();
        sellerAvatar.setText(name.isBlank() ? "?" : name.substring(0, 1).toUpperCase());
        sellerNameLabel.setText(name);
        sellerUsernameLabel.setText("@" + seller.getUsername());
        boolean verified = BrowseService.showVerifiedBadge(listing);
        sellerVerifiedBadge.setText("HKUST Verified");
        sellerVerifiedBadge.setVisible(verified);
        sellerVerifiedBadge.setManaged(verified);
        sellerRatingStars.setText(stars(seller.getAverageRating()));
        sellerRatingCountLabel.setText(String.format("%.1f / 5  ·  %d ratings",
                seller.getAverageRating(), seller.getRatingCount()));
        sellerBioLabel.setText(seller.getBio() == null || seller.getBio().isBlank()
                ? "This seller has not added a bio." : seller.getBio());
        renderSellerReviews(seller);
    }

    // newest visible reviews first, hidden ones are filtered out by the store
    private void renderSellerReviews(User seller) {
        sellerReviewsBox.getChildren().clear();
        List<Review> reviews = new java.util.ArrayList<>(
                DummyDataService.getInstance().getReviewsForSeller(seller.getUsername()));
        reviews.sort(java.util.Comparator.comparing(Review::getCreatedAt,
                java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));

        Label heading = new Label("Reviews (" + reviews.size() + ")");
        heading.setStyle("-fx-font-weight: bold; -fx-text-fill: #003974;");
        sellerReviewsBox.getChildren().add(heading);
        if (reviews.isEmpty()) {
            Label none = new Label("No reviews yet.");
            none.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
            sellerReviewsBox.getChildren().add(none);
            return;
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMM yyyy");
        for (Review review : reviews) {
            Label top = new Label(stars(review.getRating()) + "  @" + review.getBuyerUsername()
                    + (review.getListingTitle() == null ? "" : "  ·  " + review.getListingTitle())
                    + (review.getCreatedAt() == null ? "" : "  ·  " + review.getCreatedAt().format(format)));
            top.setStyle("-fx-text-fill: #A68137; -fx-font-size: 12px;");
            VBox row = new VBox(2, top);
            if (review.getComment() != null && !review.getComment().isBlank()) {
                Label comment = new Label(review.getComment());
                comment.setWrapText(true);
                comment.setStyle("-fx-text-fill: #1a1a2e; -fx-font-size: 13px;");
                row.getChildren().add(comment);
            }
            if (review.getSellerReply() != null && !review.getSellerReply().isBlank()) {
                Label reply = new Label("Seller reply: " + review.getSellerReply());
                reply.setWrapText(true);
                reply.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px; -fx-font-style: italic;");
                row.getChildren().add(reply);
            }
            sellerReviewsBox.getChildren().add(row);
        }
    }

    /**
     * shows the type-specific action label and enables it only for a buyer
     * while the listing is still live and no action has been sent yet (task 1.5)
     */
    private void renderActions() {
        boolean buyer = SessionManager.isBuyer();
        boolean live = listing.getStatus() == ListingStatus.LIVE;

        switch (listing.getType()) {
            case SELL -> actionButton.setText("Make Offer");
            case LEND -> actionButton.setText("Request Borrow");
            case GIVEAWAY -> actionButton.setText("Claim Item");
        }
        if (actionSent) {
            actionButton.setText("Offer sent");
        }

        // buyers can act only while the listing is still live
        actionButton.setDisable(!buyer || !live || actionSent);
        chatButton.setDisable(chatPanelController == null);
        reportButton.setDisable(!buyer || !live);
        favouriteButton.setText(FavouriteService.isFavourite(SessionManager.getCurrentUser(), listing)
                ? "♥ Favourited" : "♥ Favourite");
        favouriteButton.setDisable(!buyer);

        if (!live) {
            actionHintLabel.setText("This listing is no longer live, so offers and claims are disabled.");
        } else if (!buyer) {
            actionHintLabel.setText("Only buyers can make offers or claims. You can still read the chat.");
        } else if (actionSent) {
            actionHintLabel.setText("Your offer is with the seller. Watch the chat for a reply.");
        } else {
            actionHintLabel.setText("Pick an action, or say hello in the chat below.");
        }
    }

    /**
     * toggles the current listing in the buyer's favourites (task 1.4)
     */
    @FXML
    private void toggleFavourite() {
        User buyer;
        try {
            buyer = SessionManager.requireRole(UserRole.BUYER);
        } catch (SecurityException ex) {
            showWarning(ex.getMessage());
            return;
        }
        if (listing == null) {
            return;
        }
        boolean saved = FavouriteService.toggle(buyer, listing);
        favouriteButton.setText(saved ? "♥ Favourited" : "♥ Favourite");
    }

    /**
     * lets a buyer report a listing and pings the admin
     */
    @FXML
    private void reportListing() {
        User buyer;
        try {
            buyer = SessionManager.requireRole(UserRole.BUYER);
        } catch (SecurityException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            return;
        }
        if (listing == null) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Report Listing");
        dialog.setHeaderText("Report: " + listing.getTitle());
        dialog.setContentText("Reason:");
        dialog.showAndWait().ifPresent(reason -> {
            if (reason == null || reason.isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Please give a reason.").showAndWait();
                return;
            }
            // step 3 gives us addReport and the admin notification list
            Report report = new Report();
            report.setListingId(listing.getId());
            report.setReporterUsername(buyer.getUsername());
            report.setReason(reason.trim());
            DummyDataService.getInstance().addReport(report);
            NotificationService.notify("admin", "Listing Reported",
                    buyer.getFullName() + " reported '" + listing.getTitle() + "': " + reason.trim(),
                    "Report");
            new Alert(Alert.AlertType.INFORMATION,
                    "Report sent to the admin. Thanks for flagging it.").showAndWait();
        });
    }

    /**
     * runs the task 1.5 action for the current listing type
     * SELL asks for a price, LEND asks for borrow dates and GIVEAWAY asks for one claim;
     * OfferService re-checks that the listing is still live before anything is stored
     */
    @FXML
    private void handlePrimaryAction() {
        if (listing == null) {
            return;
        }
        User buyer;
        try {
            buyer = SessionManager.requireRole(UserRole.BUYER);
        } catch (SecurityException ex) {
            showWarning(ex.getMessage());
            return;
        }

        try {
            switch (listing.getType()) {
                case SELL -> askForOffer(buyer);
                case LEND -> askForBorrow(buyer);
                case GIVEAWAY -> askForClaim(buyer);
            }
        } catch (RuntimeException ex) {
            showWarning(ex.getMessage());
        }
    }

    /**
     * asks for a SELL price and submits it through OfferService (task 1.5)
     * then shows the confirmation alert and reloads the listing state
     */
    private void askForOffer(User buyer) {
        TextInputDialog dialog = new TextInputDialog(
                listing.getPrice() == null ? "" : String.format("%.0f", listing.getPrice()));
        dialog.setTitle("Make an Offer");
        dialog.setHeaderText("Offer for: " + listing.getTitle());
        dialog.setContentText("Your price (HK$):");
        dialog.showAndWait().ifPresent(text -> {
            try {
                double price = Double.parseDouble(text.trim());
                OfferService.submitOffer(listing, buyer, price, null, null, null);
                actionSent = true;
                new Alert(Alert.AlertType.INFORMATION,
                        "Offer sent to " + listing.getSellerFullName() + ".").showAndWait();
                reloadAfterAction();
            } catch (NumberFormatException ex) {
                showWarning("Enter a valid price.");
            } catch (RuntimeException ex) {
                showWarning(ex.getMessage());
            }
        });
    }

    /**
     * asks for LEND start/end dates and submits the borrow request (task 1.5)
     * same-day is allowed because the demo minimum borrow time is 5 seconds
     */
    private void askForBorrow(User buyer) {
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
            if (button != send) {
                return;
            }
            try {
                OfferService.submitOffer(listing, buyer, null,
                        startPicker.getValue(), endPicker.getValue(), null);
                actionSent = true;
                new Alert(Alert.AlertType.INFORMATION,
                        "Borrow request sent to " + listing.getSellerFullName() + ".").showAndWait();
                reloadAfterAction();
            } catch (RuntimeException ex) {
                showWarning(ex.getMessage());
            }
        });
    }

    /**
     * asks for one confirmation then claims the giveaway through OfferService (task 1.5)
     */
    private void askForClaim(User buyer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Claim Giveaway");
        confirm.setHeaderText("Claim: " + listing.getTitle());
        confirm.setContentText("The first buyer to claim gets the item. Continue?");
        confirm.showAndWait().ifPresent(button -> {
            if (button != ButtonType.OK) {
                return;
            }
            try {
                OfferService.claimGiveaway(listing, buyer);
                actionSent = true;
                new Alert(Alert.AlertType.INFORMATION,
                        "You claimed this item. The seller has been notified.").showAndWait();
                reloadAfterAction();
            } catch (RuntimeException ex) {
                showWarning(ex.getMessage());
            }
        });
    }

    /**
     * reloads the listing after an offer or claim and redraws without counting another view
     */
    private void reloadAfterAction() {
        listing = DummyDataService.getInstance().findListing(listing.getId());
        if (listing == null) {
            showMissingListing();
            return;
        }
        render();
    }

    // chat button jumps to the message box
    @FXML
    private void openChat() {
        if (chatPanelController != null) {
            chatPanelController.focusInput();
        }
    }

    /**
     * lets another screen land on the chat box, used by my deals "open chat"
     * runs later because the scene is not showing yet when the controller is set up
     */
    public void focusChat() {
        javafx.application.Platform.runLater(this::openChat);
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING,
                message == null || message.isBlank() ? "Something went wrong." : message).showAndWait();
    }

    private Image loadPhoto(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            // uploaded photos live on the classpath, seed photos may be plain files
            if (path.startsWith("/")) {
                InputStream stream = getClass().getResourceAsStream(path);
                return stream == null ? null : new Image(stream);
            }
            return new Image(path);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String typeStyle(ListingType type) {
        return switch (type) {
            case SELL -> "badge-sold";
            case LEND -> "badge-pending";
            case GIVEAWAY -> "badge-live";
        };
    }

    private String statusStyle(ListingStatus status) {
        return switch (status) {
            case LIVE -> "badge-live";
            case PENDING_REVIEW -> "badge-pending";
            case REJECTED, REMOVED -> "badge-rejected";
            default -> "badge-sold";
        };
    }

    private String statusText(ListingStatus status) {
        return switch (status) {
            case PENDING_REVIEW -> "Pending review";
            case LIVE -> "Live";
            case REJECTED -> "Rejected";
            case RESERVED -> "Reserved";
            case SOLD -> "Sold";
            case LENT -> "Lent";
            case GIVEN -> "Given away";
            case REMOVED -> "Removed";
        };
    }

    private String stars(double rating) {
        int full = (int) Math.round(Math.max(0, Math.min(5, rating)));
        return "★".repeat(full) + "☆".repeat(5 - full);
    }

    private void showMissingListing() {
        titleLabel.setText("Listing not found");
        descriptionLabel.setText("This listing may have been removed.");
        actionButton.setDisable(true);
        chatButton.setDisable(true);
        reportButton.setDisable(true);
        favouriteButton.setDisable(true);
        photoGallery.getChildren().clear();
        new Alert(Alert.AlertType.WARNING, "This listing could not be found.").showAndWait();
    }

    @FXML
    private void goBack() {
        // sellers and admins can reach this screen too, so send them home properly
        if (SessionManager.isSeller()) {
            SceneManager.showSellerDashboard();
        } else if (SessionManager.isAdmin()) {
            SceneManager.showAdminDashboard();
        } else {
            SceneManager.showBuyerDashboard();
        }
    }
}
