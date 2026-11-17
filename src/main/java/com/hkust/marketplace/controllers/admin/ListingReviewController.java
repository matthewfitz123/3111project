package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.Listing;
import com.hkust.marketplace.models.ListingStatus;
import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * admin listing review screen (step 9)
 * embedded inside the admin dashboard content pane
 */
public class ListingReviewController {

    @FXML private VBox reviewContainer;

    @FXML
    public void initialize() {
        render();
    }

    private void render() {
        reviewContainer.getChildren().clear();
        List<Listing> pending = DummyDataService.getInstance().getPendingListings();
        if (pending.isEmpty()) {
            Label empty = new Label("No listings awaiting review.");
            empty.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 15px;");
            reviewContainer.getChildren().add(empty);
            return;
        }
        for (Listing l : pending) {
            reviewContainer.getChildren().add(createPendingCard(l));
        }
    }

    private VBox createPendingCard(Listing l) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16));

        Label title = new Label(l.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label statusBadge = new Label("Pending Review");
        statusBadge.getStyleClass().addAll("badge", "badge-pending");

        Label meta = new Label(String.format("Seller: %s (@%s)  ·  %s  ·  %s  ·  %s  ·  %s  ·  %s",
                l.getSellerFullName(), l.getSellerUsername(),
                l.getCategory(), l.getTypeDisplay(), l.getCondition(), l.getPriceDisplay(),
                l.getMeetupLocation()));
        meta.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");

        Label submitted = new Label("Submitted: " +
                l.getPostedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        submitted.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");

        // photo thumbnails so admin can see what they are approving
        HBox photos = new HBox(8);
        photos.setAlignment(Pos.CENTER_LEFT);
        int shown = 0;
        for (String path : l.getPhotoPaths()) {
            if (shown >= 3 || path == null) break;
            try {
                java.io.InputStream stream = getClass().getResourceAsStream(path);
                if (stream == null) continue;
                ImageView iv = new ImageView(new Image(stream));
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setPreserveRatio(true);
                photos.getChildren().add(iv);
                shown++;
            } catch (Exception ignored) {
                // skip broken images rather than crash the review screen
            }
        }
        if (photos.getChildren().isEmpty()) {
            photos.setVisible(false);
            photos.setManaged(false);
        }

        Label description = new Label(l.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #3a3a55; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        Button approve = new Button("Approve");
        approve.getStyleClass().addAll("btn-success", "btn-sm");
        approve.setOnAction(e -> approveListing(l));

        Button reject = new Button("Reject");
        reject.getStyleClass().addAll("btn-danger", "btn-sm");
        reject.setOnAction(e -> rejectListing(l));

        actions.getChildren().addAll(approve, reject);
        card.getChildren().addAll(title, statusBadge, meta, photos, description, submitted, actions);
        return card;
    }

    private void approveListing(Listing l) {
        try {
            SessionManager.requireRole(UserRole.ADMIN);
        } catch (SecurityException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Approve this listing?");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                l.setStatus(ListingStatus.LIVE);
                l.setReviewedAt(LocalDateTime.now());
                DummyDataService.getInstance().updateListing(l);
                DummyDataService.getInstance().addNotification(new Notification(
                        l.getSellerUsername(), "Listing Approved",
                        "Your listing '" + l.getTitle() + "' is now live!", "System"));
                render();
            }
        });
    }

    private void rejectListing(Listing l) {
        try {
            SessionManager.requireRole(UserRole.ADMIN);
        } catch (SecurityException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            return;
        }
        // reason is required so keep asking until the admin gives one or cancels
        Optional<String> reason = promptForReason();
        while (reason.isPresent() && reason.get().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Reason is required.").showAndWait();
            reason = promptForReason();
        }
        if (reason.isEmpty()) {
            return;
        }
        l.setStatus(ListingStatus.REJECTED);
        l.setRejectionReason(reason.get());
        l.setReviewedAt(LocalDateTime.now());
        DummyDataService.getInstance().updateListing(l);
        DummyDataService.getInstance().addNotification(new Notification(
                l.getSellerUsername(), "Listing Rejected",
                "Your listing '" + l.getTitle() + "' was rejected. Reason: " + reason.get(), "System"));
        render();
    }

    private Optional<String> promptForReason() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejection Reason");
        dialog.setHeaderText("Please provide a reason for rejection");
        dialog.setContentText("Reason:");
        return dialog.showAndWait();
    }
}
