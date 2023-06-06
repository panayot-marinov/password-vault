package bg.sofia.uni.fmi.mjt.password.vault.server;

import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;

import java.io.Writer;

public interface Authenticatable {

    void register(String username, Password password, Password passwordRepeated, EncryptionData encryptionData)
            throws RepositoryException, UserAuthenticationException;

    void register(String username, Password password, Password passwordRepeated,
                  EncryptionData encryptionData, Writer writer)
            throws RepositoryException, UserAuthenticationException;

    void deleteAccount(String username, Password password, Password passwordRepeated)
            throws RepositoryException, UserAuthenticationException;

    void deleteAccount(String username, Password password, Password passwordRepeated, Writer writer)
            throws RepositoryException, UserAuthenticationException;

    Session login(String username, Password password)
            throws RepositoryException, UserAuthenticationException;

    void logout(String username) throws UserAuthenticationException;

    void changePassword(String username, Password oldPassword, Password newPassword, Password newPasswordRepeated)
            throws RepositoryException, UserAuthenticationException;

    void changePassword(String username, Password oldPassword, Password newPassword,
                        Password newPasswordRepeated, Writer writer)
            throws RepositoryException, UserAuthenticationException;

    boolean isLoggedIn(String username);

    EncryptionData getEncryptionData(String username) throws RepositoryException;

}