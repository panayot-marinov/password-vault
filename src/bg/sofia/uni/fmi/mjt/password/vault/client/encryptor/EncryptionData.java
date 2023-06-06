package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor;

import com.google.gson.Gson;

public class EncryptionData {

    private static final Gson GSON = new Gson();

    private final int iterationsCount;
    private final byte[] salt;

    public EncryptionData(int iterationsCount, byte[] salt) {
        this.iterationsCount = iterationsCount;
        this.salt = salt;
    }

    public int getIterationsCount() {
        return iterationsCount;
    }

    public byte[] getSalt() {
        return salt;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}