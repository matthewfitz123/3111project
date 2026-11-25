package com.hkust.marketplace.controllers.seller;

import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.services.ReviewService;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * seller reviews (task 2.10)
 * reply or flag; a reply notifies the buyer
 */
public class ReviewsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    @FXML private VBox reviewList;
    @FXML private Label summaryLabel;

    @FXML
    public void initialize() {
        refresh();
    }

    private void refresh() {
        reviewList.getChildren().clear();
        User me = SessionManager.getCurrentUser();
        if (me == null) {
            return;
        }
        summaryLabel.setText(me.getRatingCount() == 0 ? "No visible reviews"
                : String.format("%.1f / 5 from %d review(s)", me.getAverageRating(), me.getRatingCount()));

        List<Review> reviews = new ArrayList<>(
                DummyDataService.getInstance().getAllReviewsForSeller(me.getUsername()));
        reviews.sort(Comparator.comparing(Review::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (reviews.isEmpty()) {
            reviewList.getChildren().add(new Label("No reviews yet."));
            return;
        }
        for (Review review : reviews) {
            reviewList.getChildren().add(buildCard(review));
        }
    }

    private VBox buildCard(Review review) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label stars = new Label("★".repeat(review.getRating()) + "☆".repeat(5 - review.getRating()));
        stars.setStyle("-fx-text-fill: #A68137; -fx-font-size: 16px;");
        Label comment = new Label(review.getComment() == null || review.getComment().isBlank()
                ? "(no comment)" : review.getComment());
        comment.setWrapText(true);
        Label meta = new Label("by @" + review.getBuyerUsername() + " · " + review.getListingTitle()
                + (review.getCreatedAt() == null ? "" : " · " + review.getCreatedAt().format(DATE_FORMAT)));
        meta.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
        card.getChildren().addAll(stars, comment, meta);

        if (review.isHidden()) {
            Label hidden = new Label(review.isFlagged()
                    ? "Flagged, hidden until an admin reviews it" : "Hidden by an admin");
            hidden.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
            card.getChildren().add(hidden);
        }
        if (review.getSellerReply() != null && !review.getSellerReply().isBlank()) {
            Label reply = new Label("Your reply: " + review.getSellerReply());
            reply.setWrapText(true);
            reply.setStyle("-fx-text-fill: #5a5a7a; -fx-font-style: italic;");
            card.getChildren().add(reply);
        }

        HBox actions = new HBox(8);
        Button reply = new Button(review.getSellerReply() == null ? "Reply" : "Edit reply");
        reply.getStyleClass().addAll("btn-outline", "btn-sm");
        reply.setOnAction(e -> reply(review));
        actions.getChildren().add(reply);
        if (!review.isHidden()) {
            Button flag = new Button("Flag");
            flag.getStyleClass().addAll("btn-danger", "btn-sm");
            flag.setOnAction(e -> flag(review));
            actions.getChildren().add(flag);
        }
        card.getChildren().add(actions);
        return card;
    }

    private void reply(Review review) {
        TextInputDialog dialog = new TextInputDialog(
                review.getSellerReply() == null ? "" : review.getSellerReply());
        dialog.setTitle("Reply to Review");
        dialog.setHeaderText("Reply to @" + review.getBuyerUsername() + " about '"
                + review.getListingTitle() + "'");
        dialog.setContentText("Reply:");
        Optional<String> text = dialog.showAndWait();
        if (text.isEmpty()) {
            return;
        }
        run(() -> ReviewService.replyToReview(review.getId(), text.get()),
                "Reply posted. @" + review.getBuyerUsername() + " has been notified.");
    }

    private void flag(Review review) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Flag Review");
        dialog.setHeaderText("Flag this review for an admin? It is hidden until they check it.");
        dialog.setContentText("Reason:");
        Optional<String> reason = dialog.showAndWait();
        if (reason.isEmpty()) {
            return;
        }
        run(() -> ReviewService.flagReview(review.getId(), reason.get()),
                "Review flagged and hidden until an admin checks it.");
    }

    private void run(Runnable action, String doneMessage) {
        try {
            action.run();
            new Alert(Alert.AlertType.INFORMATION, doneMessage).showAndWait();
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Something went wrong." : ex.getMessage()).showAndWait();
        }
        refresh();
    }
}
