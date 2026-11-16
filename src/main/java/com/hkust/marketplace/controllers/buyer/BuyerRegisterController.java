package com.hkust.marketplace.controllers.buyer;

import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.PasswordUtil;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    @FXML
    private void handleRegister() {
        hideMessages();

        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String residence = residenceCombo.getValue();

        // every field except residence is required
        // catch an empty username first
        if (username.isEmpty()) {
            showError("Username is required.");
            return;
        }
        // usernames have to be unique across buyers sellers and admins
        if (DummyDataService.getInstance().usernameExists(username)) {
            showError("Username already taken. Please choose another.");
            return;
        }
        // full name is just for display but it cannot be blank
        if (fullName.isEmpty()) {
            showError("Full Name cannot be empty.");
            return;
        }
        // password rules live in dummydataservice so every portal uses the same ones
        // min 10 chars upper lower digit and max 64
        String pwdCheck = DummyDataService.checkPasswordStrength(password);
        if (!"OK".equals(pwdCheck)) {
            showError(pwdCheck);
            return;
        }
        // make sure both password boxes match
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        // hash before saving so the raw password never touches the user object
        User user = new User(username.toLowerCase(), fullName, PasswordUtil.hash(password), UserRole.BUYER);
        // residence is optional
        if (residence != null) {
            user.setResidenceHall(residence);
        }
        // save the account in memory for now
        DummyDataService.getInstance().registerUser(user);

        showSuccess("Registration successful! You can now login.");
        // clear the form so it is ready for another account
        usernameField.clear();
        fullNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        residenceCombo.getSelectionModel().clearSelection();
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
