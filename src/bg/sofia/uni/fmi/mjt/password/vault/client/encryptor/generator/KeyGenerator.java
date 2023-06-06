package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface KeyGenerator {

    PasswordDerivedKey generateKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException;

    PasswordDerivedKey generateKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException;

}