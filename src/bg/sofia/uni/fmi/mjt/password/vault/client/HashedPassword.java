package bg.sofia.uni.fmi.mjt.password.vault.client;

import bg.sofia.uni.fmi.mjt.password.vault.client.hasher.DefaultPasswordHasher;
import bg.sofia.uni.fmi.mjt.password.vault.client.hasher.HashingAlgorithm;
import bg.sofia.uni.fmi.mjt.password.vault.client.hasher.PasswordHasher;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class HashedPassword implements Password {

    private final Map<HashingAlgorithm, String> passwordHashes;

    public HashedPassword(char[] password) {
        passwordHashes = createPasswordHashesMap(password);
    }

    private Map<HashingAlgorithm, String> createPasswordHashesMap(char[] password) {
        final Map<HashingAlgorithm, String> passwordHashes = new EnumMap<>(HashingAlgorithm.class);

        PasswordHasher passwordHasher = new DefaultPasswordHasher();
        passwordHashes.put(HashingAlgorithm.MD5, passwordHasher.hashPassword(password, HashingAlgorithm.MD5));
        passwordHashes.put(HashingAlgorithm.SHA1, passwordHasher.hashPassword(password, HashingAlgorithm.SHA1));
        passwordHashes.put(HashingAlgorithm.SHA256, passwordHasher.hashPassword(password, HashingAlgorithm.SHA256));

        return passwordHashes;
    }

    public HashedPassword(Map<HashingAlgorithm, String> passwordHashes) {
        this.passwordHashes = passwordHashes;
    }

    @Override
    public String getMd5() {
        return passwordHashes.get(HashingAlgorithm.MD5);
    }

    @Override
    public String getSha1() {
        return passwordHashes.get(HashingAlgorithm.SHA1);
    }

    @Override
    public String getSha256() {
        return passwordHashes.get(HashingAlgorithm.SHA256);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashedPassword that = (HashedPassword) o;
        return Objects.equals(passwordHashes, that.passwordHashes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passwordHashes);
    }

}