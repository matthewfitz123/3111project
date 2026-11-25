package com.hkust.marketplace.services;

import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;
import com.hkust.marketplace.utils.DummyDataService;
import com.hkust.marketplace.utils.PasswordUtil;

/**
 * registration rules shared by the portal sign-up screens (task 1.1)
 * validates input before a user is stored and keeps passwords hashed
 */
public final class RegistrationService {

    private RegistrationService() {
        // static service
    }

    /**
     * registers a buyer after checking username, name, password and confirmation
     *
     * @param username       desired login name, unique across every user role
     * @param fullName       display name, required but not used for login
     * @param password       raw password, checked then hashed before storage
     * @param confirmPassword second password entry, must match exactly
     * @param residenceHall  optional hall or off-campus value for meet-up suggestions
     * @return the stored buyer account
     * @throws IllegalArgumentException with the user-facing reason when validation fails
     */
    public static User registerBuyer(String username, String fullName, String password,
                                     String confirmPassword, String residenceHall) {
        String name = username == null ? "" : username.trim();
        String full = fullName == null ? "" : fullName.trim();
        DummyDataService data = DummyDataService.getInstance();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        // the store keys users by lowercase name, so this check is case-insensitive
        // and covers buyers, sellers and admins in one map
        if (data.usernameExists(name)) {
            throw new IllegalArgumentException("Username already taken. Please choose another.");
        }
        if (full.isEmpty()) {
            throw new IllegalArgumentException("Full Name cannot be empty.");
        }
        String passwordCheck = DummyDataService.checkPasswordStrength(password);
        if (!"OK".equals(passwordCheck)) {
            throw new IllegalArgumentException(passwordCheck);
        }
        if (password == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = new User(name.toLowerCase(), full, PasswordUtil.hash(password), UserRole.BUYER);
        if (residenceHall != null && !residenceHall.isBlank()) {
            user.setResidenceHall(residenceHall);
        }
        data.registerUser(user);
        return user;
    }
}
