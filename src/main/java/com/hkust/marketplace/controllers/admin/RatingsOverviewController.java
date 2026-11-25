package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.Review;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.services.ReviewService;
import com.hkust.marketplace.utils.DummyDataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * admin ratings overview (task 3.8)
 * filter reviews and hide or show inappropriate ones
 */
public class RatingsOverviewController {

    private static final String ALL_SELLERS = "All sellers";
    private static final String ALL_RATINGS = "All ratings";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    @FXML private ComboBox<String> sellerFilter;
    @FXML private ComboBox<String> ratingFilter;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label countLabel;
    @FXML private VBox reviewList;

    @FXML
    public void initialize() {
        sellerFilter.setItems(FXCollections.observableArrayList(sellerChoices()));
        sellerFilter.getSelectionModel().selectFirst();
        ratingFilter.setItems(FXCollections.observableArrayList(
                ALL_RATINGS, "5 stars", "4 stars", "3 stars", "2 stars", "1 star"));
        ratingFilter.getSelectionModel().selectFirst();
        ratingFilter.setCellFactory(list -> starCell());
        ratingFilter.setButtonCell(starCell());

        sellerFilter.setOnAction(e -> refresh());
        ratingFilter.setOnAction(e -> refresh());
        fromDate.valueProperty().addListener((obs, oldDate, newDate) -> refresh());
        toDate.valueProperty().addListener((obs, oldDate, newDate) -> refresh());
        refresh();
    }

    private void refresh() {
        reviewList.getChildren().clear();
        List<Review> reviews = new ArrayList<>(DummyDataService.getInstance().getAllReviews());
        reviews.sort(Comparator.comparing(Review::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        String seller = sellerFilter.getValue();
        String rating = ratingFilter.getValue();
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        reviews.removeIf(review -> !matches(review, seller, rating, from, to));

        countLabel.setText(reviews.size() + " review(s)");
        if (reviews.isEmpty()) {
            reviewList.getChildren().add(new Label("No reviews match these filters."));
            return;
        }
        for (Review review : reviews) {
            reviewList.getChildren().add(buildCard(review));
        }
    }

    private boolean matches(Review review, String seller, String rating, LocalDate from, LocalDate to) {
        if (seller != null && !ALL_SELLERS.equals(seller)
                && (review.getSellerUsername() == null
                || !review.getSellerUsername().equalsIgnoreCase(seller))) {
            return false;
        }
        Integer stars = selectedStars(rating);
        if (stars != null && review.getRating() != stars) {
            return false;
        }
        LocalDate created = review.getCreatedAt() == null ? null : review.getCreatedAt().toLocalDate();
        if (from != null && (created == null || created.isBefore(from))) {
            return false;
        }
        return to == null || (created != null && !created.isAfter(to));
    }

    private VBox buildCard(Review review) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label stars = new Label("★".repeat(Math.max(0, review.getRating()))
                + "☆".repeat(Math.max(0, 5 - review.getRating())));
        stars.setStyle("-fx-text-fill: #A68137; -fx-font-size: 16px;");
        Label who = new Label("seller @" + review.getSellerUsername()
                + "  ·  buyer @" + review.getBuyerUsername()
                + (review.getCreatedAt() == null ? "" : "  ·  " + review.getCreatedAt().format(DATE_FORMAT)));
        who.setStyle("-fx-font-weight: bold;");
        Label comment = new Label(review.getComment() == null || review.getComment().isBlank()
                ? "(no comment)" : review.getComment());
        comment.setWrapText(true);
        Label item = new Label(review.getListingTitle() == null ? "" : review.getListingTitle());
        item.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
        card.getChildren().addAll(stars, who, comment, item);

        if (review.isHidden()) {
            Label hidden = new Label(review.isFlagged()
                    ? "Hidden (flagged by seller)" : "Hidden");
            hidden.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px;");
            card.getChildren().add(hidden);
        }
        if (review.getSellerReply() != null && !review.getSellerReply().isBlank()) {
            Label reply = new Label("Seller reply: " + review.getSellerReply());
            reply.setWrapText(true);
            reply.setStyle("-fx-text-fill: #5a5a7a; -fx-font-style: italic;");
            card.getChildren().add(reply);
        }

        HBox actions = new HBox(8);
        Button toggle = new Button(review.isHidden() ? "Unhide" : "Hide");
        toggle.getStyleClass().addAll(review.isHidden() ? "btn-success" : "btn-outline", "btn-sm");
        toggle.setOnAction(e -> toggleHidden(review));
        actions.getChildren().add(toggle);
        HBox.setHgrow(actions, Priority.ALWAYS);
        card.getChildren().add(actions);
        return card;
    }

    private void toggleHidden(Review review) {
        boolean hide = !review.isHidden();
        try {
            ReviewService.setHidden(review.getId(), hide);
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage() == null ? "Could not update the review." : ex.getMessage()).showAndWait();
        }
        refresh();
    }

    private List<String> sellerChoices() {
        Set<String> names = new LinkedHashSet<>();
        names.add(ALL_SELLERS);
        for (User user : DummyDataService.getInstance().getUsersByRole(UserRole.SELLER)) {
            names.add(user.getUsername());
        }
        for (Review review : DummyDataService.getInstance().getAllReviews()) {
            if (review.getSellerUsername() != null) {
                names.add(review.getSellerUsername());
            }
        }
        return new ArrayList<>(names);
    }

    private Integer selectedStars(String rating) {
        if (rating == null || ALL_RATINGS.equals(rating) || rating.isEmpty()) {
            return null;
        }
        char first = rating.charAt(0);
        return Character.isDigit(first) ? first - '0' : null;
    }

    private ListCell<String> starCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || ALL_RATINGS.equals(value)) {
                    setText(empty || value == null ? null : value);
                    return;
                }
                Integer stars = selectedStars(value);
                setText(stars == null ? value
                        : "★".repeat(stars) + "☆".repeat(5 - stars) + "  (" + stars + ")");
            }
        };
    }
}
