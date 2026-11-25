package com.hkust.marketplace.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// small helper for hashing and checking passwords
// we never store the actual raw password
// each stored value has its own random salt and iteration count
// the format will looks something like
// pbkdf2$120000$salt$hash
/**
 * small helper for hashing and checking passwords with salted pbkdf2
 */
public final class PasswordUtil {

    // 120k rounds is reasonable i reckon but change if u want
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * hashes a password with a fresh random salt (task 1.1 secure credential storage)
     *
     * @return a self-contained {@code pbkdf2$iterations$salt$hash} value safe to store
     * @throws IllegalArgumentException when the password is null
     */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * checks a typed password against a stored {@code pbkdf2$...} value (task 1.1 login verification)
     *
     * @return false for null inputs or malformed stored values instead of throwing
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    // doing the actual pbkdf2 work here
    // sha-256 is available as a class so just use that
    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("PBKDF2 hashing is unavailable.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
