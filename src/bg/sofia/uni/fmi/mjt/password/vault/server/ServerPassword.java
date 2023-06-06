package bg.sofia.uni.fmi.mjt.password.vault.server;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;

import java.util.Objects;

public class ServerPassword {

    private final HashedPassword hashedPassword;
    private final String encryptedPassword;

    public ServerPassword(HashedPassword hashedPassword, String encryptedPassword) {
        this.hashedPassword = hashedPassword;
        this.encryptedPassword = encryptedPassword;
    }

    public HashedPassword getHashedPassword() {
        return hashedPassword;
    }

    public String getAesEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerPassword password = (ServerPassword) o;
        return Objects.equals(hashedPassword, password.hashedPassword) &&
                Objects.equals(encryptedPassword, password.encryptedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedPassword, encryptedPassword);
    }

}