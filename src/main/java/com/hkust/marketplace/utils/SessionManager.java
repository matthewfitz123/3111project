package com.hkust.marketplace.utils;

import com.hkust.marketplace.models.User;
import com.hkust.marketplace.models.UserRole;

/**
 * keeps track of who is logged in for this run
 * everything is in memory so it resets when the app closes
 */
public class SessionManager {
    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * gets the username of the person using the app
     * returns null when nobody is logged in
     */
    public static String getCurrentUsername() {
        return currentUser == null ? null : currentUser.getUsername();
    }

    /**
     * checks the current user has one of the allowed roles
     * throws SecurityException when there is no session or the role is wrong
     */
    public static User requireRole(UserRole... allowed) {
        // no session means nothing is allowed
        if (currentUser == null) {
            throw new SecurityException("Not logged in");
        }
        // any one of the roles on the list is enough
        for (UserRole role : allowed) {
            if (currentUser.getRole() == role) {
                return currentUser;
            }
        }
        throw new SecurityException("Role " + currentUser.getRole() + " not permitted");
    }

    public static boolean isBuyer() {
        return currentUser != null && currentUser.getRole() == UserRole.BUYER;
    }

    public static boolean isSeller() {
        return currentUser != null && currentUser.getRole() == UserRole.SELLER;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }
}
