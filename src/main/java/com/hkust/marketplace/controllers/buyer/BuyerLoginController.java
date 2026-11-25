package com.hkust.marketplace.controllers.buyer;

import com.hkust.marketplace.services.AuthService;
import com.hkust.marketplace.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * buyer login screen for task 1.2
 * collects username/password and delegates credential checks to {@link AuthService}
 */
public class BuyerLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    /**
     * validates credentials and opens the buyer dashboard (task 1.2)
     * AuthService owns the checks; this method only shows feedback and navigates
     */
    @FXML
    private void handleLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        try {
            AuthService.loginBuyer(usernameField.getText(), passwordField.getText());
            SceneManager.showBuyerDashboard();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void goRegister() {
        SceneManager.showBuyerRegister();
    }

    @FXML
    private void goWelcome() {
        SceneManager.showWelcome();
    }
}
