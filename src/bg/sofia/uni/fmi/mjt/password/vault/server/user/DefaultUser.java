package bg.sofia.uni.fmi.mjt.password.vault.server.user;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentials;
import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentialsKey;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.AccountCredentialsRepository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryNotInitialisedException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class DefaultUser implements User {

    private static final Gson GSON = new Gson();

    private final String username;
    private HashedPassword password;
    private final EncryptionData encryptionData;
    private AccountCredentialsRepository accountCredentialsRepository;

    public DefaultUser(String username, HashedPassword password, EncryptionData encryptionData) {
        this.username = username;
        this.password = password;
        this.encryptionData = encryptionData;
    }

    public DefaultUser(String username, HashedPassword password, EncryptionData encryptionData,
                       AccountCredentialsRepository accountCredentialsRepository) {
        this.username = username;
        this.password = password;
        this.encryptionData = encryptionData;
        this.accountCredentialsRepository = accountCredentialsRepository;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Password getPassword() {
        return password;
    }

    @Override
    public void changePassword(HashedPassword password) {
        if (password == null) {
            throw new IllegalArgumentException("Password should not be null.");
        }

        this.password = password;
    }

    public EncryptionData getEncryptionData() {
        return encryptionData;
    }

    @Override
    public void addCredentials(String applicationName, String username, String password, Writer writer)
            throws CredentialAlreadyExistsException {
        if (applicationName == null || applicationName.isBlank() || username == null || username.isBlank() ||
                password == null || password.isBlank() || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }
        if (accountCredentialsRepository == null) {
            throw new RepositoryNotInitialisedException("AccountCredentialsRepository is null.");
        }

        accountCredentialsRepository.put(
                new AccountCredentials(applicationName, username, password), writer);
    }

    @Override
    public void updateCredentials(String applicationName, String credentialsUsername,
                                  String credentialsPassword, Writer writer)
            throws CredentialNotFoundException {
        if (applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank() ||
                credentialsPassword == null || credentialsPassword.isBlank() || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }
        if (accountCredentialsRepository == null) {
            throw new RepositoryNotInitialisedException("AccountCredentialsRepository is null.");
        }

        accountCredentialsRepository.update(
                new AccountCredentials(applicationName, credentialsUsername, credentialsPassword), writer);
    }

    @Override
    public String getCredentials(String applicationName, String credentialsUsername)
            throws CredentialNotFoundException {
        if (applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank()) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }
        if (accountCredentialsRepository == null) {
            throw new RepositoryNotInitialisedException("AccountCredentialsRepository is null.");
        }

        return accountCredentialsRepository
                .get(new AccountCredentialsKey(applicationName, credentialsUsername))
                .getEncryptedPassword();
    }

    @Override
    public void removeCredentials(String applicationName, String credentialsUsername, Writer writer)
            throws CredentialNotFoundException {
        if (applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank() || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }
        if (accountCredentialsRepository == null) {
            throw new RepositoryNotInitialisedException("AccountCredentialsRepository is null.");
        }

        accountCredentialsRepository.remove(
                new AccountCredentialsKey(applicationName, credentialsUsername), writer);
    }

    @Override
    public void delete() throws UserDeletionException {
        if (accountCredentialsRepository != null) {
            try {
                accountCredentialsRepository.deletePathIfExists();
            } catch (IOException e) {
                throw new UserDeletionException("Cannot delete user credentials data.", e);
            }
        }
    }

    @Override
    public void refreshCredentials() {
        accountCredentialsRepository.refresh();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultUser that = (DefaultUser) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

}