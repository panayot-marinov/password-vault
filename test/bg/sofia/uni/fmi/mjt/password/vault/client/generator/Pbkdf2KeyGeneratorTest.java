package bg.sofia.uni.fmi.mjt.password.vault.client.generator;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.Pbkdf2KeyGenerator;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class Pbkdf2KeyGeneratorTest {

    private static final char[] PASSWORD = "examplePassword".toCharArray();
    private static final byte[] SALT = "exampleSalt".getBytes(StandardCharsets.UTF_8);

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int ITERATIONS_COUNT = 1024;
    private static final int SALT_SIZE = SALT.length;
    private static final int KEY_LENGTH = 64;

    private final KeyGenerator keyGenerator =
            Pbkdf2KeyGenerator.builder()
                    .setIterationsCount(ITERATIONS_COUNT)
                    .setSaltSize(SALT_SIZE)
                    .setKeyLength(KEY_LENGTH)
                    .build();

    @Test
    public void testGenerateKeyOneArgumentThrowsIllegalArgumentExceptionWhenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> keyGenerator.generateKey(null),
                "Method should throw IllegalArgumentException when password is null.");
    }

    @Test
    public void testGenerateKeyTwoArgumentsThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> keyGenerator.generateKey(null, SALT),
                "Method should throw IllegalArgumentException when password is null.");

        assertThrows(IllegalArgumentException.class,
                () -> keyGenerator.generateKey(PASSWORD, null),
                "Method should throw IllegalArgumentException when password is null.");

        assertThrows(IllegalArgumentException.class,
                () -> keyGenerator.generateKey(null, null),
                "Method should throw IllegalArgumentException when password is null.");
    }

    @Test
    public void testGenerateKeyTwoArgumentsThrowsIllegalArgumentExceptionWhenSaltArrayIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> keyGenerator.generateKey(PASSWORD, new byte[]{}),
                "Method should throw IllegalArgumentException when salt array is empty.");
    }

    @Test
    public void testGenerateKeyReturnsCorrectPasswordDerivedKeyWhenArgumentsAreCorrect() {
        PasswordDerivedKey passwordDerivedKey = null;
        try {
            passwordDerivedKey = keyGenerator.generateKey(PASSWORD, SALT);
        } catch (NoSuchAlgorithmException e) {
            fail("Method has thrown an NoSuchAlgorithmException.");
        } catch (InvalidKeySpecException e) {
            fail("Method has thrown an InvalidKeySpecException.");
        }

        byte[] expectedHash = new byte[] {48, 72, 69, 230-256, 56, 37, 227-256, 61};
        SecretKey expectedSecretKey =
                new SecretKeySpec(expectedHash, 0, expectedHash.length, ENCRYPTION_ALGORITHM);
        PasswordDerivedKey expectedPasswordDerivedKey =
                new PasswordDerivedKey(ITERATIONS_COUNT, SALT, expectedSecretKey);

        assertEquals(expectedPasswordDerivedKey, passwordDerivedKey,
                "PasswordDerivedKey does not have a correct value.");
    }

}