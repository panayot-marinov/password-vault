package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.InvalidUsernameOrPasswordException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.PasswordsDoNotMatchException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class ChangeAccountPasswordRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final Logger logger;

    public ChangeAccountPasswordRequestHandler(PasswordVault passwordVault, Logger logger) {
        this.passwordVault = passwordVault;
        this.logger = logger;
    }

    @Override
    public NioResponse handle(NioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request should not be null.");
        }

        String username = request.getUsername();
        ServerPassword oldPassword = request.getOldPassword();
        ServerPassword newPassword = request.getPassword();
        ServerPassword newPasswordRepeated = request.getPasswordRepeated();
        if (username == null || oldPassword == null || newPassword == null || newPasswordRepeated == null) {
            throw new IllegalStateException(
                    "Username, oldPassword, password and passwordRepeated should have non-null values.");
        }

        try {
            passwordVault.changePassword(username, oldPassword.getHashedPassword(),
                    newPassword.getHashedPassword(), newPasswordRepeated.getHashedPassword());
        } catch (RepositoryException e) {
            String logMessage = username + " tried to change password. " +
                    "It failed because user with such an username was not found. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.USER_NOT_FOUND, null);
        } catch (InvalidUsernameOrPasswordException e) {
            String logMessage = username + " tried to change password. " +
                    "It failed because username or password are invalid." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.INVALID_USERNAME_OR_PASSWORD, null);
        } catch (PasswordsDoNotMatchException e) {
            String logMessage = username + " tried to change password. " +
                    "It failed because new passwords are not equal. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.PASSWORDS_DO_NOT_MATCH, null);
        } catch (UserAuthenticationException e) {
            String logMessage = username + " tried to change password. " +
                    "It failed because old and new password are equal. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.EQUAL_OLD_AND_NEW_PASSWORDS, null);
        }
        String logMessage = username + " has changed its password successfully.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);

        return new NioResponse(ResponseType.ACCOUNT_PASSWORD_CHANGED_SUCCESSFULLY, null);
    }

}