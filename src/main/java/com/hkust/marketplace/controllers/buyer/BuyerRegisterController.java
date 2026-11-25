package com.hkust.marketplace.controllers.buyer;

import com.hkust.marketplace.services.RegistrationService;
import com.hkust.marketplace.utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * buyer registration form for task 1.1
 * collects username, full name, password, confirmation and optional residence,
 * then delegates validation and storage to {@link RegistrationService}
 */
public class BuyerRegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> residenceCombo;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML
    public void initialize() {
        residenceCombo.setItems(FXCollections.observableArrayList(
                "Hall 1", "Hall 2", "Hall 3", "Hall 4", "Hall 5",
                "Hall 6", "Hall 7", "Hall 8", "Hall 9",
                "UG Hall 1", "UG Hall 2", "Off-campus"
        ));
    }

    /**
     * validates and submits the buyer registration form (task 1.1)
     * RegistrationService owns the rules; this method only shows feedback and resets the form
     */
    @FXML
    private void handleRegister() {
        hideMessages();

        try {
            RegistrationService.registerBuyer(
                    usernameField.getText(),
                    fullNameField.getText(),
                    passwordField.getText(),
                    confirmPasswordField.getText(),
                    residenceCombo.getValue());

            showSuccess("Registration successful! You can now login.");
            usernameField.clear();
            fullNameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            residenceCombo.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        successLabel.setText(msg);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    @FXML private void goLogin() { SceneManager.showBuyerLogin(); }
    @FXML private void goWelcome() { SceneManager.showWelcome(); }
}
