package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

// Salted SHA-256 password hashes, stored as "sha256:<salt hex>:<hash hex>"
public final class Passwords {
    private static final String PREFIX = "sha256:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private Passwords() {
    }

    public static String hash(String plain) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return PREFIX + HEX.formatHex(salt) + ":" + HEX.formatHex(digest(salt, plain));
    }

    public static boolean matches(String plain, String stored) {
        if (plain == null || !isHashed(stored)) {
            return false;
        }
        String[] parts = stored.substring(PREFIX.length()).split(":");
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = HEX.parseHex(parts[0]);
            byte[] expected = HEX.parseHex(parts[1]);
            return MessageDigest.isEqual(expected, digest(salt, plain));
        } catch (IllegalArgumentException e) {
            return false; // corrupted hex in the file
        }
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    private static byte[] digest(byte[] salt, String plain) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(salt);
            sha.update(plain.getBytes(StandardCharsets.UTF_8));
            return sha.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is missing from this JDK", e); // every JDK has it
        }
    }
}
