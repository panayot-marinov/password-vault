package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class RemovePasswordRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final Logger logger;

    public RemovePasswordRequestHandler(PasswordVault passwordVault, Logger logger) {
        this.passwordVault = passwordVault;
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
        if (username == null || credentialsUsername == null || applicationName == null) {
            throw new IllegalStateException(
                    "Username, credentialsUsername and applicationName should have non-null values.");
        }

        try {
            passwordVault.removeCredentials(username, applicationName, credentialsUsername);
        } catch (UserNotFoundException e) {
            String logMessage = username + " tried to remove credentials password. " +
                    "It failed because user with such an username was not found. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.USER_NOT_FOUND, null);
        } catch (RepositoryException e) {
            String logMessage = username + " tried to remove credentials for application  " + applicationName + "." +
                    "It failed because credentials with such an combination of application name and " +
                    "credentials username were not found. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.CREDENTIALS_NOT_FOUND, null);
        }

        String logMessage = username + " has removed credentials for application  "
                + applicationName + " successfully. ";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);

        return new NioResponse(ResponseType.PASSWORD_REMOVED_SUCCESSFULLY, null);
    }

}