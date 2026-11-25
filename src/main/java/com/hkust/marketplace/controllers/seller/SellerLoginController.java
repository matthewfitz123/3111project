package com.hkust.marketplace.controllers.seller;

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

/**
 * seller login screen with role and active account checks
 */
public class SellerLoginController {

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
            showError("Please enter both username and password.");
            return;
        }
        // find the seller account
        User user = DummyDataService.getInstance().findUser(username);
        if (user == null) {
            showError("User not found.");
            return;
        }
        // make sure this is a seller and not a buyer or admin
        if (user.getRole() != UserRole.SELLER) {
            showError("This account is not a Seller account. Use the correct portal.");
            return;
        }
        // check the password against the stored hash
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            showError("Incorrect password.");
            return;
        }
        // blocked accounts cannot log in
        if (!user.isActive()) {
            showError("Account deactivated.");
            return;
        }
        SessionManager.login(user);
        SceneManager.showSellerDashboard();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML private void goRegister() { SceneManager.showSellerRegister(); }
    @FXML private void goWelcome() { SceneManager.showWelcome(); }
}
