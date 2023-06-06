package bg.sofia.uni.fmi.mjt.password.vault.client.hasher;

public interface PasswordHasher {

    String hashPassword(char[] password, HashingAlgorithm algorithm);

}