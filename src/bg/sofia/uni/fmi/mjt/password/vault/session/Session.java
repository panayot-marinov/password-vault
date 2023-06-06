package bg.sofia.uni.fmi.mjt.password.vault.session;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;

import java.util.Objects;

public class Session {

    private final String username;
    private boolean isLoggedIn;
    private final PasswordDerivedKey passwordDerivedKey;

    public Session(String username, PasswordDerivedKey passwordDerivedKey) {
        this.username = username;
        this.passwordDerivedKey = passwordDerivedKey;
        this.isLoggedIn = true;
    }

    public void end() {
        this.isLoggedIn = false;
    }

    public String getUsername() {
        return username;
    }

    public PasswordDerivedKey getPasswordDerivedKey() {
        return passwordDerivedKey;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return isLoggedIn == session.isLoggedIn &&
                Objects.equals(username, session.username) &&
                Objects.equals(passwordDerivedKey, session.passwordDerivedKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, isLoggedIn, passwordDerivedKey);
    }

}