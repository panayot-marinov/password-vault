package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.CompromisedPasswordsClient;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.exceptions.CompromisedPasswordsClientException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class UpdatePasswordRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final CompromisedPasswordsClient compromisedPasswordsClient;
    private final Logger logger;

    public UpdatePasswordRequestHandler(
            PasswordVault passwordVault, CompromisedPasswordsClient compromisedPasswordsClient, Logger logger) {
        this.passwordVault = passwordVault;
        this.compromisedPasswordsClient = compromisedPasswordsClient;
        this.logger = logger;
    }

    @Override
    public NioResponse handle(NioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request should not be null.");
        }

        String username = request.getUsername();
        String credentialsUsername = request.getCredentialsUsername();
        String applicationName = request.getApplicationName();
        ServerPassword password = request.getPassword();
        if (username == null || credentialsUsername == null || applicationName == null || password == null) {
            throw new IllegalStateException(
                    "Username, credentialsUsername, applicationName and password should have non-null values.");
        }
        String encryptedPassword = password.getAesEncryptedPassword();

        boolean isCompromised = false;
        try {
            isCompromised = compromisedPasswordsClient.isCompromised(password.getHashedPassword());
        } catch (CompromisedPasswordsClientException e) {
            String logMessage = username + " tried to add credentials for application  " + applicationName + "." +
                    "Compromised passwords api has thrown an exception. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.WARN, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);
        }

        if (!isCompromised) {
            try {
                passwordVault.updateCredentials(
                        username, applicationName, credentialsUsername, encryptedPassword);
            } catch (UserNotFoundException e) {
                String logMessage = username +
                        "tried to add credentials. It failed because user with such an username was not found. " +
                        "Stacktrace: " + Arrays.toString(e.getStackTrace());
                logger.log(Level.INFO, LocalDateTime.now(), logMessage);
                System.out.println(logMessage);

                return new NioResponse(ResponseType.USER_NOT_FOUND, null);
            } catch (ElementNotFoundException e) {
                String logMessage = username + " tried to add credentials for application  " + applicationName + "." +
                        "It failed because credentials with such an combination of application name and " +
                        "credentials username were not found. " +
                        "Stacktrace: " + Arrays.toString(e.getStackTrace());
                logger.log(Level.INFO, LocalDateTime.now(), logMessage);
                System.out.println(logMessage);

                return new NioResponse(ResponseType.CREDENTIALS_NOT_FOUND, null);
            }
            String logMessage = username + " tried to add credentials for application  " + applicationName + "." +
                    "It failed because password is compromised.";
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.PASSWORD_UPDATED_SUCCESSFULLY, null);
        }

        String logMessage = username + " tried to add credentials for application  " + applicationName + "." +
                "It failed because password is compromised.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);

        return new NioResponse(ResponseType.PASSWORD_COMPROMISED, null);
    }

}