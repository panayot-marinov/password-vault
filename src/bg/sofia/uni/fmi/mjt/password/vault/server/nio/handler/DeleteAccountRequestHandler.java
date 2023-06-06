package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.InvalidUsernameOrPasswordException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class DeleteAccountRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final Logger logger;

    public DeleteAccountRequestHandler(PasswordVault passwordVault, Logger logger) {
        this.passwordVault = passwordVault;
        this.logger = logger;
    }

    @Override
    public NioResponse handle(NioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request should not be null.");
        }

        String username = request.getUsername();
        ServerPassword password = request.getPassword();
        ServerPassword passwordRepeated = request.getPasswordRepeated();
        if (username == null || password == null || passwordRepeated == null) {
            throw new IllegalStateException(
                    "Username, password and passwordRepeated should have non-null values.");
        }

        try {
            passwordVault.deleteAccount(username,
                    password.getHashedPassword(), passwordRepeated.getHashedPassword());
        } catch (ElementNotFoundException e) {
            String logMessage = username + " tried to delete account. " +
                    "It failed because user with such an username was not found. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.USER_NOT_FOUND, null);
        } catch (InvalidUsernameOrPasswordException e) {
            String logMessage = username + " tried to delete account. " +
                    "It failed because password is invalid." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.INVALID_USERNAME_OR_PASSWORD, null);
        } catch (UserAuthenticationException e) {
            String logMessage = username + " tried to delete account. " +
                    "It failed because new passwords are not equal. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.PASSWORDS_DO_NOT_MATCH, null);
        } catch (RepositoryException e) {
            String logMessage = username + " tried to delete account. " +
                    "Deletion failed. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.INTERNAL_SERVER_ERROR, "Cannot delete user");
        }

        String logMessage = username + " has deleted its account successfully.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);

        return new NioResponse(ResponseType.ACCOUNT_DELETED_SUCCESSFULLY, null);
    }

}