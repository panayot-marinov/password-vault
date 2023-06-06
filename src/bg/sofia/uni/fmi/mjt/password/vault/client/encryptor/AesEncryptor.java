package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AesEncryptor {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static String encryptData(byte[] data, SecretKey key)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (data == null || key == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot encrypt data. Algorithm does not exist.", e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("Cannot encrypt data. Padding does not exist.", e);
        }

        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data);

        return Base64.getEncoder()
                .encodeToString(encryptedData);
    }

    public static byte[] decryptData(String encryptedData, SecretKey key)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (encryptedData == null || key == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot decrypt data. Algorithm does not exist.", e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("Cannot decrypt data. Padding does not exist.", e);
        }

        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedData = cipher.doFinal(
                Base64.getDecoder()
                        .decode(encryptedData));

        return decryptedData;
    }

}