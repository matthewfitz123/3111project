package com.hkust.marketplace.controllers.common;

import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.ListingType;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.Report;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SceneManager;
import com.hkust.marketplace.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * listing detail screen (step 12)
 * shows one listing with all photos, fields and seller info
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
    @FXML private Label sellerRatingStars;
    @FXML private Label sellerRatingCountLabel;
    @FXML private Label sellerBioLabel;

    private Listing listing;

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
    }

    private void renderPhotos() {
        photoGallery.getChildren().clear();
        List<String> paths = listing.getPhotoPaths();
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
            return;
        }
        String name = seller.getFullName() == null ? seller.getUsername() : seller.getFullName();
        sellerAvatar.setText(name.isBlank() ? "?" : name.substring(0, 1).toUpperCase());
        sellerNameLabel.setText(name);
        sellerUsernameLabel.setText("@" + seller.getUsername());
        sellerRatingStars.setText(stars(seller.getAverageRating()));
        sellerRatingCountLabel.setText(String.format("%.1f / 5  ·  %d ratings",
                seller.getAverageRating(), seller.getRatingCount()));
        sellerBioLabel.setText(seller.getBio() == null || seller.getBio().isBlank()
                ? "This seller has not added a bio." : seller.getBio());
    }

    private void renderActions() {
        // offer and chat buttons are placeholders until steps 15 to 17
        switch (listing.getType()) {
            case SELL -> actionButton.setText("Make Offer");
            case LEND -> actionButton.setText("Request Borrow");
            case GIVEAWAY -> actionButton.setText("Claim Item");
        }
        actionButton.setDisable(true);
        chatButton.setDisable(true);
        reportButton.setDisable(SessionManager.getCurrentUser() == null);
        actionHintLabel.setText("Offers and chat are coming in the next steps. "
                + "You can report this listing if something looks wrong.");
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
            DummyDataService.getInstance().addNotification(new Notification(
                    "admin", "Listing Reported",
                    buyer.getFullName() + " reported '" + listing.getTitle() + "': " + reason.trim(),
                    "Report"));
            new Alert(Alert.AlertType.INFORMATION,
                    "Report sent to the admin. Thanks for flagging it.").showAndWait();
        });
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
        photoGallery.getChildren().clear();
        new Alert(Alert.AlertType.WARNING, "This listing could not be found.").showAndWait();
    }

    @FXML
    private void goBack() {
        SceneManager.showBuyerDashboard();
    }
}
