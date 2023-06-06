package bg.sofia.uni.fmi.mjt.password.vault.server.credentials;

import com.google.gson.Gson;

import java.util.Objects;

public class AccountCredentials {

    private static final Gson GSON = new Gson();

    private final String applicationName;
    private final String username;
    private final String encryptedPassword;

    public AccountCredentials(String applicationName, String username, String encryptedPassword) {
        this.applicationName = applicationName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountCredentials that = (AccountCredentials) o;
        return Objects.equals(applicationName, that.applicationName) &&
                Objects.equals(username, that.username) &&
                Objects.equals(encryptedPassword, that.encryptedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, username, encryptedPassword);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

}