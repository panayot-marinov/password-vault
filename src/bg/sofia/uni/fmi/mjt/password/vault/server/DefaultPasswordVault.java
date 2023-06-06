package bg.sofia.uni.fmi.mjt.password.vault.server;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.AccountCredentialsRepository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.Repository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.DefaultUser;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.User;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.EqualOldAndNewPasswordsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.InvalidUsernameOrPasswordException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.PasswordsDoNotMatchException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DefaultPasswordVault implements PasswordVault {

    private Path usersFilePath;
    private String userCredentialsDirectory;
    private String userCredentialsExtension;

    private final Repository<String, DefaultUser> users;
    private final Map<String, Session> loggedInUsers;
    private final Logger logger;

    public DefaultPasswordVault(Repository<String, DefaultUser> users, Logger logger, Path usersFilePath,
                                String userCredentialsDirectory, String userCredentialsExtension) {
        this.users = users;
        loggedInUsers = new HashMap<>();
        this.usersFilePath = usersFilePath;
        this.userCredentialsDirectory = userCredentialsDirectory;
        this.userCredentialsExtension = userCredentialsExtension;
        this.logger = logger;
        users.refresh();
    }

    public DefaultPasswordVault(Repository<String, DefaultUser> users, Logger logger) {
        this.users = users;
        loggedInUsers = new HashMap<>();
        this.logger = logger;
        users.refresh();
    }

    @Override
    public void register(String username, Password password, Password passwordRepeated, EncryptionData encryptionData)
            throws RepositoryException, UserAuthenticationException {
        try (Writer usersWriter = new FileWriter(usersFilePath.toString(), true)) {
            register(username, password, passwordRepeated, encryptionData, usersWriter);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open users file for writing.", e);
        }
    }

    @Override
    public void register(String username, Password password, Password passwordRepeated,
                         EncryptionData encryptionData, Writer writer)
            throws RepositoryException, UserAuthenticationException {
        if (username == null || username.isBlank() ||
                password == null || passwordRepeated == null || encryptionData == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }
        if (!password.getSha256().equals(passwordRepeated.getSha256())) {

            throw new PasswordsDoNotMatchException(
                    "Password and passwordRepeated SHA256 hashes should have equal values.");
        }
        if (users.contains(username)) {
            throw new UsernameAlreadyExistsException("User with such an username already exists");
        }

        DefaultUser newUser = null;
        if (userCredentialsDirectory != null && userCredentialsExtension != null) {
            AccountCredentialsRepository repository =
                    new AccountCredentialsRepository(username, userCredentialsDirectory, userCredentialsExtension);
            newUser = new DefaultUser(username, (HashedPassword) password,
                    encryptionData, repository);
        } else {
            newUser = new DefaultUser(username, (HashedPassword) password,
                    encryptionData);
        }

        users.put(newUser, writer);
    }

    @Override
    public void deleteAccount(String username, Password password, Password passwordRepeated)
            throws RepositoryException, UserAuthenticationException {
        try (Writer usersWriter = new FileWriter(usersFilePath.toString(), false)) {
            deleteAccount(username, password, passwordRepeated, usersWriter);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open users file for writing.", e);
        }
    }

    @Override
    public void deleteAccount(String username, Password password, Password passwordRepeated, Writer writer)
            throws RepositoryException, UserAuthenticationException {
        if (username == null || username.isBlank() || password == null || passwordRepeated == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }

        DefaultUser user = null;
        user = users.get(username);

        if (!password.getSha256().equals(passwordRepeated.getSha256())) {
            throw new PasswordsDoNotMatchException(
                    "newPassword and newPasswordRepeated SHA256 hashes should have equal values.");
        }

        Password userPassword = user.getPassword();
        if (passwordsDoNotMatch(password, userPassword)) {
            throw new InvalidUsernameOrPasswordException(
                    "There is no user with such an combination of username and password.");
        }

        users.remove(username, writer);
    }

    @Override
    public Session login(String username, Password password)
            throws RepositoryException, UserAuthenticationException {
        if (username == null || username.isBlank() || password == null) {
            throw new IllegalArgumentException("All arguments should have non-null and non-empty values.");
        }

        User user = null;
        user = users.get(username);

        Password userPassword = user.getPassword();
        if (passwordsDoNotMatch(password, userPassword)) {
            throw new InvalidUsernameOrPasswordException(
                    "There is no user with such an combination of username and password.");
        }

        EncryptionData encryptionData = user.getEncryptionData();
        Session session = new Session(user.getUsername(),
                new PasswordDerivedKey(encryptionData.getIterationsCount(), encryptionData.getSalt(), null));
        loggedInUsers.put(user.getUsername(), session);

        return session;
    }

    @Override
    public void logout(String username) throws UserNotLoggedInException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username should have non-null and non-empty value.");
        }

        Session removedSession = loggedInUsers.remove(username);
        if (removedSession == null) {
            throw new UserNotLoggedInException("User should be logged in before logging out.");
        }
    }

    @Override
    public void changePassword(
            String username, Password oldPassword, Password newPassword, Password newPasswordRepeated)
            throws RepositoryException, UserAuthenticationException {
        try (Writer usersWriter = new FileWriter(usersFilePath.toString(), false)) {
            changePassword(username, oldPassword, newPassword, newPasswordRepeated, usersWriter);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open users file for writing.", e);
        }
    }

    @Override
    public void changePassword(String username, Password oldPassword, Password newPassword,
                               Password newPasswordRepeated, Writer writer)
            throws RepositoryException, UserAuthenticationException {
        if (username == null || username.isBlank() ||
                oldPassword == null || newPassword == null || newPasswordRepeated == null) {
            throw new IllegalArgumentException("All arguments should have non-null and non-empty values.");
        }

        DefaultUser user = null;
        user = users.get(username);

        Password userPassword = user.getPassword();
        if (passwordsDoNotMatch(oldPassword, userPassword)) {
            throw new InvalidUsernameOrPasswordException(
                    "There is no user with such an combination of username and password.");
        }

        if (!passwordsDoNotMatch(oldPassword, newPassword)) {
            throw new EqualOldAndNewPasswordsException("New password cannot be the same as the old password.");
        }

        if (!newPassword.getSha256().equals(newPasswordRepeated.getSha256())) {
            throw new PasswordsDoNotMatchException(
                    "newPassword and newPasswordRepeated SHA256 hashes should have equal values.");
        }

        user.changePassword((HashedPassword) newPassword);
        users.update(user, writer);
    }

    @Override
    public boolean isLoggedIn(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username should have non-null and non-empty value.");
        }

        return loggedInUsers.containsKey(username);
    }

    @Override
    public EncryptionData getEncryptionData(String username) throws ElementNotFoundException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username should have non-null and non-empty value.");
        }

        return users.get(username).getEncryptionData();
    }

    @Override
    public void addCredentials(String username, String applicationName,
                               String credentialsUsername, String credentialsPassword)
            throws ElementNotFoundException, CredentialAlreadyExistsException {
        try (Writer writer = new FileWriter(AccountCredentialsRepository
                .generateCredentialsFilePath(username, userCredentialsDirectory, userCredentialsExtension).toString(),
                true)) {
            addCredentials(username, applicationName, credentialsUsername, credentialsPassword, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open users file for writing.", e);
        }
    }

    @Override
    public void addCredentials(String username, String applicationName,
                               String credentialsUsername, String credentialsPassword, Writer writer)
            throws ElementNotFoundException, CredentialAlreadyExistsException {
        if (username == null || username.isBlank() ||
                applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank() ||
                credentialsPassword == null || credentialsPassword.isBlank()) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }

        DefaultUser user = null;
        user = users.get(username);
        user.addCredentials(applicationName, credentialsUsername, credentialsPassword, writer);
    }

    @Override
    public void updateCredentials(String username, String applicationName,
                                  String credentialsUsername, String credentialsPassword)
            throws ElementNotFoundException {
        try (Writer writer = new FileWriter(AccountCredentialsRepository
                .generateCredentialsFilePath(username, userCredentialsDirectory, userCredentialsExtension).toString(),
                false)) {
            updateCredentials(username, applicationName, credentialsUsername, credentialsPassword, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open users file for writing.", e);
        }
    }

    @Override
    public void updateCredentials(String username, String applicationName,
                                  String credentialsUsername, String credentialsPassword, Writer writer)
            throws ElementNotFoundException {
        if (username == null || username.isBlank() ||
                applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank() ||
                credentialsPassword == null || credentialsPassword.isBlank()) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }

        DefaultUser user = null;
        user = users.get(username);
        user.updateCredentials(applicationName, credentialsUsername, credentialsPassword, writer);
    }

    @Override
    public void removeCredentials(String username, String applicationName, String credentialsUsername)
            throws ElementNotFoundException {
        try (Writer writer = new FileWriter(AccountCredentialsRepository
                .generateCredentialsFilePath(username, userCredentialsDirectory, userCredentialsExtension).toString(),
                false)) {
            removeCredentials(username, applicationName, credentialsUsername, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot open users file for writing.", e);
        }
    }

    @Override
    public void removeCredentials(String username, String applicationName, String credentialsUsername, Writer writer)
            throws ElementNotFoundException {
        if (username == null || username.isBlank() ||
                applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank()) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }

        DefaultUser user = null;
        user = users.get(username);
        user.removeCredentials(applicationName, credentialsUsername, writer);
    }

    @Override
    public String getCredentialsPassword(String username, String applicationName, String credentialsUsername)
            throws ElementNotFoundException {
        if (username == null || username.isBlank() ||
                applicationName == null || applicationName.isBlank() ||
                credentialsUsername == null || credentialsUsername.isBlank()) {
            throw new IllegalArgumentException("All arguments should have non-null and non-blank values.");
        }

        User user = users.get(username);
        String credentials = user.getCredentials(applicationName, credentialsUsername);

        return credentials;
    }

    private boolean passwordsDoNotMatch(Password password, Password userPassword) {
        return (userPassword.getSha256() != null && !userPassword.getSha256().equals(password.getSha256())) &&
                (userPassword.getSha1() != null && !userPassword.getSha1().equals(password.getSha1())) &&
                (userPassword.getMd5() != null && !userPassword.getMd5().equals(password.getMd5()));
    }

}