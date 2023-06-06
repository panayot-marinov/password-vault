package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AesEncryptorTest {

    private static final byte[] NON_ENCRYPTED_DATA = "examplePassword".getBytes(StandardCharsets.UTF_8);
    private static final String ENCRYPTED_DATA = "cUW3Ibh2qxcgI6E/315vlQ==";
    private static final byte[] HASH = "bQeShVmYq3t6w9z$".getBytes(StandardCharsets.UTF_8);
    private static final SecretKey SECRET_KEY = new SecretKeySpec(HASH, 0, HASH.length, "AES");

    @Test
    public void testEncryptDataThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentHasNullValue() {
        assertThrows(IllegalArgumentException.class, () -> AesEncryptor.encryptData(null, SECRET_KEY),
                "Method should throw an IllegalArgumentException when data is null.");
        assertThrows(IllegalArgumentException.class, () -> AesEncryptor.encryptData(NON_ENCRYPTED_DATA, null),
                "Method should throw an IllegalArgumentException when key is null.");
        System.out.println();
    }

    @Test
    public void testEncryptDataEncryptsDataCorrectlyWhenArgumentsAreNotNull()
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        assertEquals(ENCRYPTED_DATA, AesEncryptor.encryptData(NON_ENCRYPTED_DATA, SECRET_KEY),
                "Encrypted data value is not correct.");
    }

    @Test
    public void testDecryptDataThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentHasNullValue() {
        assertThrows(IllegalArgumentException.class, () -> AesEncryptor.decryptData(null, SECRET_KEY),
                "Method should throw an IllegalArgumentException when encryptedData is null.");
        assertThrows(IllegalArgumentException.class, () -> AesEncryptor.decryptData(ENCRYPTED_DATA, null),
                "Method should throw an IllegalArgumentException when key is null.");
    }

    @Test
    public void testDecryptDataDecryptsDataCorrectlyWhenArgumentsAreNotNull()
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        assertArrayEquals(NON_ENCRYPTED_DATA, AesEncryptor.decryptData(ENCRYPTED_DATA, SECRET_KEY),
                "Decrypted data value is not correct.");
    }

}