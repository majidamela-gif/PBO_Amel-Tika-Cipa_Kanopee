package latihan.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            byte[] hash = pbkdf2(password, salt);

            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Gagal melakukan hashing password.",
                    e
            );
        }
    }

    public static boolean verifyPassword(
            String password,
            String storedPassword
    ) {
        try {
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt =
                    Base64.getDecoder().decode(parts[0]);
            byte[] storedHash =
                    Base64.getDecoder().decode(parts[1]);
            byte[] enteredHash =
                    pbkdf2(password, salt);

            return MessageDigest.isEqual(
                    storedHash,
                    enteredHash
            );

        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(
            String password,
            byte[] salt
    ) throws Exception {
        javax.crypto.spec.PBEKeySpec spec =
                new javax.crypto.spec.PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        ITERATIONS,
                        KEY_LENGTH
                );

        javax.crypto.SecretKeyFactory factory =
                javax.crypto.SecretKeyFactory.getInstance(
                        "PBKDF2WithHmacSHA256"
                );

        return factory.generateSecret(spec).getEncoded();
    }
}