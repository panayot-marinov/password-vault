package bg.sofia.uni.fmi.mjt.password.vault.server;

import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;

import java.io.Writer;

public interface PasswordVault extends Authenticatable {

    void addCredentials(String username, String applicationName,
                        String credentialsUsername, String credentialsPassword)
            throws ElementNotFoundException, CredentialAlreadyExistsException;

    void addCredentials(String username, String applicationName,
                        String credentialsUsername, String credentialsPassword, Writer writer)
            throws ElementNotFoundException, CredentialAlreadyExistsException;

    String getCredentialsPassword(String username, String applicationName, String credentialsUsername)
            throws ElementNotFoundException;

    void updateCredentials(
            String username, String applicationName, String credentialsUsername, String credentialsPassword)
            throws ElementNotFoundException;

    void updateCredentials(String username, String applicationName, String credentialsUsername,
                           String credentialsPassword, Writer writer)
            throws ElementNotFoundException;

    void removeCredentials(String username, String applicationName, String credentialsUsername)
            throws ElementNotFoundException;

    void removeCredentials(String username, String applicationName, String credentialsUsername, Writer writer)
            throws ElementNotFoundException;

}