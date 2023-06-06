package bg.sofia.uni.fmi.mjt.password.vault.client.hasher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultPasswordHasherTest {

    private static final char[] TEST_PASSWORD = "testpassword".toCharArray();
    private static final String TEST_PASSWORD_MD5 = "e16b2ab8d12314bf4efbd6203906ea6c";
    private static final String TEST_PASSWORD_SHA1 = "8bb6118f8fd6935ad0876a3be34a717d32708ffd";
    private static final String TEST_PASSWORD_SHA256 =
            "9f735e0df9a1ddc702bf0a1a7b83033f9f7153a00c29de82cedadc9957289b05";

    private final PasswordHasher passwordHasher = new DefaultPasswordHasher();

    @Test
    public void testHashPasswordThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> passwordHasher.hashPassword(null, HashingAlgorithm.SHA256),
                "Method should throw IllegalArgumentException when password is null.");

        assertThrows(IllegalArgumentException.class,
                () -> passwordHasher.hashPassword(TEST_PASSWORD, null),
                "Method should throw IllegalArgumentException when hashingAlgorithm is null.");

        assertThrows(IllegalArgumentException.class,
                () -> passwordHasher.hashPassword(null, null),
                "Method should throw IllegalArgumentException when password and hashingAlgorithm are null.");
    }

    @Test
    public void testHashPasswordHashesToMd5Correctly() {
        assertEquals(TEST_PASSWORD_MD5, passwordHasher.hashPassword(TEST_PASSWORD, HashingAlgorithm.MD5),
                "Method does not return correct value for md5 hash.");
    }

    @Test
    public void testHashPasswordHashesToSha1Correctly() {
        assertEquals(TEST_PASSWORD_SHA1, passwordHasher.hashPassword(TEST_PASSWORD, HashingAlgorithm.SHA1),
                "Method does not return correct value for sha1 hash.");
    }

    @Test
    public void testHashPasswordHashesToSha256Correctly() {
        assertEquals(TEST_PASSWORD_SHA256, passwordHasher.hashPassword(TEST_PASSWORD, HashingAlgorithm.SHA256),
                "Method does not return correct value for sha256 hash.");
    }

}