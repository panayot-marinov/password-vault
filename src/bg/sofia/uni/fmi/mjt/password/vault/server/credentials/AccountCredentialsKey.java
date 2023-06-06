package bg.sofia.uni.fmi.mjt.password.vault.server.credentials;

import com.google.gson.Gson;

import java.util.Objects;

public class AccountCredentialsKey {

    private static final Gson GSON = new Gson();

    private final String applicationName;
    private final String username;

    public AccountCredentialsKey(String applicationName, String username) {
        this.applicationName = applicationName;
        this.username = username;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountCredentialsKey that = (AccountCredentialsKey) o;
        return Objects.equals(applicationName, that.applicationName) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, username);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

}