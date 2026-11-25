package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.*;
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
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Label welcomeLabel;
    @FXML private Button btnReview, btnAllListings, btnUsers, btnDeals, btnRatings, btnNotifications, btnProfile;

    private Button currentActiveBtn;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) welcomeLabel.setText("Admin: " + user.getFullName());
        currentActiveBtn = btnReview;
        showReview();
        updateNotificationBadge();
    }

    /**
     * refreshes the unread count shown in the admin sidebar
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
    private void showReview() {
        setActive(btnReview, "Listing Review & Approval");
        contentPane.getChildren().clear();
        try {
            // the review screen lives in its own fxml/controller (step 9)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/ListingReviewView.fxml"));
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Could not load listing review screen.\n" + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showAllListings() {
        // the manage-listings screen lives in its own fxml/controller (step 27)
        setActive(btnAllListings, "Manage All Listings");
        contentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/ManageListingsView.fxml"));
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Could not load manage listings screen.\n" + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showUsers() {
        // the user management screen lives in its own fxml/controller (step 26)
        setActive(btnUsers, "Manage All Users");
        contentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/ManageUsersView.fxml"));
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Could not load user management screen.\n" + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showDeals() {
        setActive(btnDeals, "All Deals Record");
        contentPane.getChildren().clear();

        VBox root = new VBox(12);
        root.setPadding(new Insets(4));

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        TextField search = new TextField();
        search.setPromptText("Search by item / user...");
        ComboBox<String> typeFilter = new ComboBox<>(FXCollections.observableArrayList("All Types", "Sell", "Lend", "Giveaway"));
        typeFilter.getSelectionModel().selectFirst();
        filters.getChildren().addAll(search, typeFilter);
        root.getChildren().add(filters);

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        String[] headers = {"Item", "Buyer", "Seller", "Type", "Status", "Date"};
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[idx]));
            table.getColumns().add(col);
        }
        table.setItems(FXCollections.observableArrayList(
                new String[]{"Calculus Textbook", "alice", "charlie", "Sell", "Accepted", "2026-01-15"},
                new String[]{"USB Hub", "bob", "diana", "Giveaway", "Completed", "2025-12-01"}
        ));
        root.getChildren().add(table);
        contentPane.getChildren().add(root);
    }

    @FXML
    private void showRatings() {
        setActive(btnRatings, "Seller Ratings Overview");
        contentPane.getChildren().clear();

        VBox root = new VBox(12);
        root.setPadding(new Insets(4));

        HBox filters = new HBox(10);
        TextField userFilter = new TextField();
        userFilter.setPromptText("Filter by seller...");
        ComboBox<String> scoreFilter = new ComboBox<>(FXCollections.observableArrayList(
                "All scores", "5 stars", "4 stars", "3 stars", "2 stars", "1 star"));
        scoreFilter.getSelectionModel().selectFirst();
        filters.getChildren().addAll(userFilter, scoreFilter);
        root.getChildren().add(filters);

        for (Review r : DummyDataService.getInstance().getAllReviews()) {
            HBox row = new HBox(12);
            row.getStyleClass().add("card");
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12));

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label stars = new Label("★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating()) +
                    "  @" + r.getSellerUsername() + " by @" + r.getBuyerUsername());
            stars.setStyle("-fx-font-weight: bold;");
            Label comment = new Label(r.getComment());
            comment.setWrapText(true);
            comment.setStyle("-fx-text-fill: #5a5a7a;");
            info.getChildren().addAll(stars, comment);

            Button hide = new Button(r.isHidden() ? "Unhide" : "Hide");
            hide.getStyleClass().addAll("btn-outline", "btn-sm");
            hide.setOnAction(e -> {
                r.setHidden(!r.isHidden());
                DummyDataService.getInstance().recalculateSellerRating(r.getSellerUsername());
                showRatings();
            });
            row.getChildren().addAll(info, hide);
            root.getChildren().add(row);
        }
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        contentPane.getChildren().add(scroll);
    }

    /**
     * loads the shared notification board for the admin
     */
    @FXML
    private void showNotifications() {
        // same component as the buyer and seller portals
        setActive(btnNotifications, "Admin Notifications");
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
        form.setMaxWidth(400);
        form.getStyleClass().add("card");
        form.setPadding(new Insets(20));
        Label titleLbl = new Label("Admin Profile");
        titleLbl.getStyleClass().add("section-title");
        form.getChildren().add(titleLbl);

        TextField name = new TextField(user != null ? user.getFullName() : "");
        PasswordField pass = new PasswordField();
        pass.setPromptText("New password (optional)");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");
        Button save = new Button("Save");
        save.getStyleClass().add("btn-success");
        save.setOnAction(e -> {
            // guard against a missing session
            if (user == null) {
                new Alert(Alert.AlertType.WARNING, "You are not logged in.").showAndWait();
                return;
            }
            // read the form values
            String newName = name.getText().trim();
            String newPassword = pass.getText();
            // keep the admin name valid
            if (newName.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Full Name cannot be empty.").showAndWait();
                return;
            }
            // optional password change
            if (!newPassword.isEmpty()) {
                String pwdCheck = DummyDataService.checkPasswordStrength(newPassword);
                if (!"OK".equals(pwdCheck)) {
                    new Alert(Alert.AlertType.WARNING, pwdCheck).showAndWait();
                    return;
                }
                // do not save a typo in the confirmation box
                if (!newPassword.equals(confirmPass.getText())) {
                    new Alert(Alert.AlertType.WARNING, "Passwords do not match.").showAndWait();
                    return;
                }
                // store the hash and nothing else
                user.setPasswordHash(PasswordUtil.hash(newPassword));
            }
            user.setFullName(newName);
            new Alert(Alert.AlertType.INFORMATION, "Profile updated.").showAndWait();
        });
        form.getChildren().addAll(
                new Label("Full Name"), name,
                new Label("New Password"), pass,
                new Label("Confirm New Password"), confirmPass,
                save);
        contentPane.getChildren().add(form);
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        SceneManager.showWelcome();
    }
}
