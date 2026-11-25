package com.hkust.marketplace.services;

import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;
import com.hkust.marketplace.utils.SessionManager;

/**
 * credential checks for the portal login screens (task 1.2)
 * validates username/password against the in-memory user store,
 * enforces the account role for the portal, then starts the session
 */
public final class AuthService {

    private AuthService() {
        // static service
    }

    /**
     * logs in a buyer and makes that user the current session
     *
     * @param username login name, matched case-insensitively
     * @param password raw password, verified against the stored PBKDF2 hash
     * @return the authenticated buyer account
     * @throws IllegalArgumentException with the user-facing reason when login fails
     */
    public static User loginBuyer(String username, String password) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Please enter both username and password.");
        }

        User user = DummyDataService.getInstance().findUser(name);
        if (user == null) {
            throw new IllegalArgumentException("User not found. Please check your username.");
        }
        // buyer credentials must not open the seller or admin portals
        if (user.getRole() != UserRole.BUYER) {
            throw new IllegalArgumentException("This account is not a Buyer account. Please use the correct portal.");
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password. Please try again.");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("This account has been deactivated. Contact admin.");
        }

        SessionManager.login(user);
        return user;
    }

    /**
     * logs in a seller (task 2.2) using the same hash and active-account checks as buyers
     */
    public static User loginSeller(String username, String password) {
        User user = requireCredentials(username, password, UserRole.SELLER,
                "This account is not a Seller account. Use the correct portal.",
                "User not found.",
                "Incorrect password.",
                "Account deactivated.");
        SessionManager.login(user);
        return user;
    }

    /**
     * logs in the seeded admin (task 3.1). there is no admin registration
     */
    public static User loginAdmin(String username, String password) {
        User user = requireCredentials(username, password, UserRole.ADMIN,
                "Invalid admin credentials.",
                "Invalid admin credentials.",
                "Incorrect password.",
                "Account deactivated. Contact admin.");
        SessionManager.login(user);
        return user;
    }

    private static User requireCredentials(String username, String password, UserRole role,
                                           String wrongRole, String missing, String badPassword,
                                           String inactive) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException(role == UserRole.ADMIN
                    ? "Please enter credentials."
                    : "Please enter both username and password.");
        }
        User user = DummyDataService.getInstance().findUser(name);
        if (user == null || (role == UserRole.ADMIN && user.getRole() != UserRole.ADMIN)) {
            throw new IllegalArgumentException(user == null && role != UserRole.ADMIN ? missing : wrongRole);
        }
        if (user.getRole() != role) {
            throw new IllegalArgumentException(wrongRole);
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException(badPassword);
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException(inactive);
        }
        return user;
    }
}
