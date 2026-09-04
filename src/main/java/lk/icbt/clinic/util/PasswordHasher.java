package lk.icbt.clinic.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Thin wrapper around the BCrypt library.
 * <p>
 * BCrypt rather than a plain hash: it salts every password automatically and
 * is deliberately slow, so a stolen copy of the staff table cannot be
 * attacked with precomputed tables or brute-forced cheaply. This class exists
 * so the rest of the codebase depends on one method, not directly on the
 * third-party library's API.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String plaintext) {
        return BCrypt.withDefaults().hashToString(12, plaintext.toCharArray());
    }

    /** Constant-time comparison against the stored hash; the hash is never reversed. */
    public static boolean matches(String plaintext, String storedHash) {
        return BCrypt.verifyer().verify(plaintext.toCharArray(), storedHash).verified;
    }
}
