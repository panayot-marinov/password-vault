package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class Pbkdf2KeyGenerator implements KeyGenerator {

    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String SALT_ALGORITHM = "SHA1PRNG";

    private final int iterationsCount;
    private final int saltSize;
    private final int keyLength;

    public static Pbkdf2KeyGeneratorBuilder builder() {
        return new Pbkdf2KeyGeneratorBuilder();
    }

    @Override
    public PasswordDerivedKey generateKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }

        byte[] salt = generateSalt();
        return generateKey(password, salt);
    }

    @Override
    public PasswordDerivedKey generateKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Neither password nor salt could be null.");
        }
        if (salt.length == 0) {
            throw new IllegalArgumentException("Salt length cannot be zero.");
        }

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterationsCount, keyLength);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        byte[] hash = secretKeyFactory.generateSecret(spec).getEncoded();

        SecretKey secretKey = new SecretKeySpec(hash, 0, hash.length, ENCRYPTION_ALGORITHM);
        PasswordDerivedKey passwordDerivedKey = new PasswordDerivedKey(iterationsCount, salt, secretKey);

        return passwordDerivedKey;
    }

    private Pbkdf2KeyGenerator(Pbkdf2KeyGeneratorBuilder builder) {
        iterationsCount = builder.iterationsCount;
        saltSize = builder.saltSize;
        keyLength = builder.keyLength;
    }

    private byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance(SALT_ALGORITHM);
        byte[] salt = new byte[saltSize];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public static class Pbkdf2KeyGeneratorBuilder {

        private static final int DEFAULT_ITERATIONS_COUNT = 1024;
        private static final int DEFAULT_SALT_SIZE = 16;
        private static final int DEFAULT_KEY_LENGTH = 32 * 8;

        private int iterationsCount = DEFAULT_ITERATIONS_COUNT;
        private int saltSize = DEFAULT_SALT_SIZE;
        private int keyLength = DEFAULT_KEY_LENGTH;

        public Pbkdf2KeyGeneratorBuilder setIterationsCount(int iterationsCount) {
            if (iterationsCount <= 0) {
                throw new IllegalArgumentException("iterationsCount must be a positive number.");
            }

            this.iterationsCount = iterationsCount;
            return this;
        }

        public Pbkdf2KeyGeneratorBuilder setSaltSize(int saltSize) {
            if (saltSize <= 0) {
                throw new IllegalArgumentException("saltSize must be a positive number.");
            }

            this.saltSize = saltSize;
            return this;
        }

        public Pbkdf2KeyGeneratorBuilder setKeyLength(int keyLength) {
            if (keyLength <= 0) {
                throw new IllegalArgumentException("keyLength must be a positive number.");
            }

            this.keyLength = keyLength;
            return this;
        }

        public Pbkdf2KeyGenerator build() {
            return new Pbkdf2KeyGenerator(this);
        }

    }

}