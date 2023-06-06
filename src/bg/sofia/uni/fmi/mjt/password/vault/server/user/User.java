package bg.sofia.uni.fmi.mjt.password.vault.server.user;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;

import java.io.Writer;

public interface User {

    String getUsername();

    Password getPassword();

    void changePassword(HashedPassword password);

    void addCredentials(String applicationName, String username, String password, Writer writer)
            throws CredentialAlreadyExistsException;

    void updateCredentials(String applicationName, String credentialsUsername,
                           String credentialsPassword, Writer writer)
            throws ElementNotFoundException;

    String getCredentials(String applicationName, String username) throws CredentialNotFoundException;

    void removeCredentials(String applicationName, String credentialsUsername, Writer writer)
            throws CredentialNotFoundException;

    void delete() throws UserDeletionException;

    void refreshCredentials();

    EncryptionData getEncryptionData();
}