package bg.sofia.uni.fmi.mjt.password.vault.client.hasher;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DefaultPasswordHasher implements PasswordHasher {

    private static final int HASHED_PASSWORDS_NUMBER_SYSTEM = 16;
    private static final int HASH_LENGTH = 32;

    private MessageDigest messageDigest;

    @Override
    public String hashPassword(char[] password, HashingAlgorithm algorithm) {
        if (password == null || algorithm == null) {
            throw new IllegalArgumentException("Password and algorithm should have non-null values.");
        }

        try {
            messageDigest = switch (algorithm) {
                case MD5 -> MessageDigest.getInstance("MD5");
                case SHA1 -> MessageDigest.getInstance("SHA-1");
                case SHA256 -> MessageDigest.getInstance("SHA-256");
            };
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while choosing hashing function.", e);
        }

        byte[] passwordBytes = toByteArray(password);
        messageDigest.update(passwordBytes);
        byte[] hashedPasswordBytes = messageDigest.digest();

        // Convert byte array into signum representation
        BigInteger bigInt = new BigInteger(1, hashedPasswordBytes);
        String hexString = toHexString(bigInt, HASH_LENGTH);

        return hexString;
    }

    private byte[] toByteArray(char[] input) {
        byte[] output = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) input[i];
        }

        return output;
    }

    private String toHexString(BigInteger bigInteger, int length) {
        StringBuilder hexBuilder = new StringBuilder(bigInteger.toString(HASHED_PASSWORDS_NUMBER_SYSTEM));

        while (hexBuilder.length() < length) {
            hexBuilder.insert(0, "0");
        }

        return hexBuilder.toString();
    }

}