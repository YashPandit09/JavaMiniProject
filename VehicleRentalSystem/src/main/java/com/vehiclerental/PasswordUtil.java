package com.vehiclerental;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2-based password hashing utility.
 * <p>
 * Uses {@code PBKDF2WithHmacSHA256} with a 16-byte random salt and 210 000 iterations.
 * The hash is stored as {@code iterations:base64(salt):base64(hash)} so verification
 * is self-contained and future-proof (the iteration count is embedded in the stored value).
 * <p>
 * This class uses only the standard JDK — no external dependency is required.
 */
public class PasswordUtil {

    private static final String ALGORITHM  = "PBKDF2WithHmacSHA256";
    private static final int    ITERATIONS = 210_000;
    private static final int    SALT_BYTES = 16;
    private static final int    KEY_LENGTH = 256;   // bits

    /**
     * Hashes a plaintext password and returns a storable string.
     *
     * @param password the plaintext password (never {@code null})
     * @return a string in the format {@code iterations:base64(salt):base64(hash)}
     */
    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        return ITERATIONS
                + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plaintext password against a stored hash.
     *
     * @param password   the plaintext password to check
     * @param storedHash the previously stored hash string
     * @return {@code true} if the password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 3) return false;

        int    iterations = Integer.parseInt(parts[0]);
        byte[] salt       = Base64.getDecoder().decode(parts[1]);
        byte[] expected   = Base64.getDecoder().decode(parts[2]);

        byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);

        // Constant-time comparison to resist timing attacks
        return slowEquals(expected, actual);
    }

    // ---- internal helpers -------------------------------------------------

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 hashing failed", e);
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    // Prevent instantiation
    private PasswordUtil() { }
}
