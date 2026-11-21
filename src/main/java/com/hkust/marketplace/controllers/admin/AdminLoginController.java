package com.hkust.marketplace.controllers.admin;

import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import com.hkust.marketplace.utils.SceneManager;
import com.hkust.marketplace.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AdminLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // both fields are needed
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter credentials.");
            return;
        }
        // admins are seeded so there is no registration screen
        User user = DummyDataService.getInstance().findUser(username);
        if (user == null || user.getRole() != UserRole.ADMIN) {
            showError("Invalid admin credentials.");
            return;
        }
        // verify the password hash before letting them in
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            showError("Incorrect password.");
            return;
        }
        // deactivated admins should not get past the login screen
        if (!user.isActive()) {
            showError("Account deactivated. Contact admin.");
            return;
        }
        SessionManager.login(user);
        SceneManager.showAdminDashboard();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML private void goWelcome() { SceneManager.showWelcome(); }
}
