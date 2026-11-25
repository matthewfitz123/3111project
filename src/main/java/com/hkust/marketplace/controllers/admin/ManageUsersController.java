package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * admin user management (task 3.3)
 * view, edit, deactivate or soft-delete a buyer or seller
 * listings, deals and chat stay in the store
 */
public class ManageUsersController {

    @FXML private VBox usersContainer;

    @FXML
    public void initialize() {
        render();
    }

    private void render() {
        usersContainer.getChildren().clear();

        Button auditBtn = new Button("View Audit Log");
        auditBtn.getStyleClass().addAll("btn-outline", "btn-sm");
        auditBtn.setOnAction(e -> showAuditLog());
        usersContainer.getChildren().add(auditBtn);

        DummyDataService data = DummyDataService.getInstance();
        int shown = 0;
        for (User u : data.getAllUsers()) {
            // admins manage buyers and sellers, not other admins
            if (u.getRole() == UserRole.ADMIN) {
                continue;
            }
            shown++;
            usersContainer.getChildren().add(createUserCard(u));
        }
        if (shown == 0) {
            Label empty = new Label("No users to manage.");
            empty.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 14px;");
            usersContainer.getChildren().add(empty);
        }
    }

    private HBox createUserCard(User u) {
        HBox row = new HBox(12);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(u.getFullName() + " (@" + u.getUsername() + ")");
        name.setStyle("-fx-font-weight: bold;");

        String rating = u.getRole() == UserRole.SELLER
                ? String.format("★ %.1f (%d reviews)", u.getAverageRating(), u.getRatingCount())
                : "—";
        Label meta = new Label(u.getRole() + " · " + (u.isActive() ? "Active" : "Deactivated")
                + " · Rating " + rating);
        meta.setStyle("-fx-text-fill: #5a5a7a; -fx-font-size: 12px;");
        info.getChildren().addAll(name, meta);

        Button edit = new Button("Edit");
        edit.getStyleClass().addAll("btn-outline", "btn-sm");
        edit.setOnAction(e -> editUser(u));

        Button toggle = new Button(u.isActive() ? "Deactivate" : "Reactivate");
        toggle.getStyleClass().addAll(u.isActive() ? "btn-danger" : "btn-success", "btn-sm");
        toggle.setOnAction(e -> toggleActive(u));

        Button delete = new Button("Delete");
        delete.getStyleClass().addAll("btn-danger", "btn-sm");
        delete.setDisable(!u.isActive());
        delete.setOnAction(e -> deleteUser(u));

        row.getChildren().addAll(info, edit, toggle, delete);
        return row;
    }

    private boolean requireAdmin() {
        try {
            SessionManager.requireRole(UserRole.ADMIN);
            return true;
        } catch (SecurityException ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            return false;
        }
    }

    private String actor() {
        User admin = SessionManager.getCurrentUser();
        return admin == null ? "admin" : admin.getUsername();
    }

    private void toggleActive(User u) {
        if (!requireAdmin()) {
            return;
        }
        boolean activating = !u.isActive();
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                (activating ? "Reactivate" : "Deactivate") + " @" + u.getUsername() + "?");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                DummyDataService data = DummyDataService.getInstance();
                if (activating) {
                    data.reactivateUser(u.getUsername());
                    data.logAudit(actor(), "USER_REACTIVATED", "@" + u.getUsername());
                } else {
                    data.deactivateUser(u.getUsername());
                    data.logAudit(actor(), "USER_DEACTIVATED", "@" + u.getUsername());
                }
                render();
            }
        });
    }

    private void deleteUser(User u) {
        if (!requireAdmin()) {
            return;
        }
        // soft delete only: listings, deals and chat are kept for integrity
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete @" + u.getUsername() + "?\n\nThis is a soft delete: the account is deactivated "
                        + "and cannot log in, but their listings, deals and chat history are retained.");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                DummyDataService data = DummyDataService.getInstance();
                data.deactivateUser(u.getUsername());
                data.logAudit(actor(), "USER_DELETED",
                        "@" + u.getUsername() + " (soft delete; listings/deals/chat retained)");
                render();
            }
        });
    }

    private void editUser(User u) {
        if (!requireAdmin()) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit @" + u.getUsername());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(u.getFullName());
        nameField.setPromptText("Full name");
        // admins are never manageable here, so don't allow promoting anyone to ADMIN
        ComboBox<UserRole> roleBox = new ComboBox<>(FXCollections.observableArrayList(
                UserRole.BUYER, UserRole.SELLER));
        roleBox.setValue(u.getRole() == UserRole.SELLER ? UserRole.SELLER : UserRole.BUYER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Role:"), 0, 1);
        grid.add(roleBox, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Full name cannot be empty.").showAndWait();
            return;
        }
        DummyDataService data = DummyDataService.getInstance();
        u.setFullName(newName);
        u.setRole(roleBox.getValue());
        data.logAudit(actor(), "USER_EDITED",
                "@" + u.getUsername() + " name='" + newName + "' role=" + u.getRole());
        render();
    }

    private void showAuditLog() {
        if (!requireAdmin()) {
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Audit Log");
        dialog.setHeaderText("Admin actions, oldest first");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TextArea area = new TextArea(String.join("\n",
                DummyDataService.getInstance().getAuditLog()));
        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefSize(700, 400);
        dialog.getDialogPane().setContent(area);
        dialog.showAndWait();
    }
}
