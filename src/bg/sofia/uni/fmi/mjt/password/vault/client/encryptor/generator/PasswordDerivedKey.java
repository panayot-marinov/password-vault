package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Objects;

public class PasswordDerivedKey {

    private final int iterationsCount;
    private final byte[] salt;
    private SecretKey key;

    public PasswordDerivedKey(int iterationsCount, byte[] salt, SecretKey key) {
        this.iterationsCount = iterationsCount;
        this.salt = salt;
        this.key = key;
    }

    public int getIterationsCount() {
        return iterationsCount;
    }

    public byte[] getSalt() {
        return salt;
    }

    public SecretKey getKey() {
        return key;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordDerivedKey that = (PasswordDerivedKey) o;
        return iterationsCount == that.iterationsCount &&

                Arrays.equals(salt, that.salt) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(iterationsCount, key);
        result = 31 * result + Arrays.hashCode(salt);
        return result;
    }

}