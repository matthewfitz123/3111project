package com.hkust.marketplace.controllers.common;

import com.hkust.marketplace.models.Notification;
import com.hkust.marketplace.models.User;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * notification board (task 1.8)
 * newest first, with category, timestamp and read state, plus mark-all-read
 */
public class NotificationBoardController {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML private ComboBox<String> categoryFilter;
    @FXML private VBox notificationList;
    @FXML private Label unreadLabel;

    private String selectedCategory = "All";
    private Runnable onChanged = () -> {};
    private boolean updatingCategories;

    /**
     * lets the dashboard refresh its sidebar unread count
     */
    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged == null ? () -> {} : onChanged;
    }

    @FXML
    public void initialize() {
        categoryFilter.setOnAction(e -> {
            if (!updatingCategories && categoryFilter.getValue() != null) {
                selectedCategory = categoryFilter.getValue();
                refresh();
            }
        });
        refreshCategories();
        refresh();
    }

    /**
     * marks every notification for the current user as read
     */
    @FXML
    private void markAllRead() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        DummyDataService.getInstance().markAllNotificationsRead(user.getUsername());
        refresh();
        onChanged.run();
    }

    /**
     * rebuilds the list from the store using the selected category
     */
    private void refresh() {
        User user = SessionManager.getCurrentUser();
        notificationList.getChildren().clear();
        if (user == null) {
            unreadLabel.setText("");
            notificationList.getChildren().add(emptyLabel("Sign in to see notifications."));
            return;
        }

        List<Notification> all = DummyDataService.getInstance()
                .getNotificationsFor(user.getUsername());
        // count unread before filtering so the header stays accurate
        int unread = DummyDataService.getInstance()
                .getUnreadNotificationCount(user.getUsername());
        unreadLabel.setText(unread + (unread == 1 ? " unread" : " unread"));

        // show everything or just the chosen category
        List<Notification> shown = new ArrayList<>();
        for (Notification notification : all) {
            if ("All".equals(selectedCategory)
                    || selectedCategory.equals(notification.getCategory())) {
                shown.add(notification);
            }
        }

        if (shown.isEmpty()) {
            notificationList.getChildren().add(emptyLabel("No notifications here yet."));
            return;
        }
        for (Notification notification : shown) {
            notificationList.getChildren().add(buildRow(notification));
        }
    }

    /**
     * builds one notification row with its category, time and read button
     */
    private HBox buildRow(Notification notification) {
        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox heading = new HBox(8);
        heading.setAlignment(Pos.CENTER_LEFT);
        Label category = new Label(notification.getCategory() == null
                ? "General" : notification.getCategory());
        category.getStyleClass().addAll("badge", "badge-pending");
        Label title = new Label(notification.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a1a2e;"
                + (notification.isRead() ? "" : " -fx-underline: true;"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label time = new Label(notification.getTimestamp() == null
                ? "" : TIME_FORMAT.format(notification.getTimestamp()));
        time.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 11px;");
        heading.getChildren().addAll(category, title, spacer, time);

        Label message = new Label(notification.getMessage());
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 13px;");
        body.getChildren().addAll(heading, message);

        HBox row = new HBox(10, body);
        row.getStyleClass().add("notif-item");
        row.setAlignment(Pos.CENTER_LEFT);
        // only unread rows need the extra button
        if (!notification.isRead()) {
            Button read = new Button("Mark read");
            read.getStyleClass().addAll("btn-outline", "btn-sm");
            read.setOnAction(e -> {
                DummyDataService.getInstance().markNotificationRead(notification.getId());
                refresh();
                onChanged.run();
            });
            row.getChildren().add(read);
        }
        return row;
    }

    /**
     * keeps the category dropdown in sync with what is actually stored
     */
    private void refreshCategories() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }
        Set<String> options = new LinkedHashSet<>();
        options.add("All");
        for (Notification notification : DummyDataService.getInstance()
                .getNotificationsFor(user.getUsername())) {
            if (notification.getCategory() != null && !notification.getCategory().isBlank()) {
                options.add(notification.getCategory());
            }
        }

        // guard the listener while the dropdown is being rebuilt
        updatingCategories = true;
        categoryFilter.setItems(FXCollections.observableArrayList(options));
        if (!options.contains(selectedCategory)) {
            selectedCategory = "All";
        }
        categoryFilter.setValue(selectedCategory);
        updatingCategories = false;
    }

    // tiny helper so every empty state looks the same
    private Label emptyLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #9aa0b0; -fx-font-size: 12px;");
        return label;
    }
}
