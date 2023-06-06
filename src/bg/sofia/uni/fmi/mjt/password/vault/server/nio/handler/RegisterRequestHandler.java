package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class RegisterRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final Logger logger;

    public RegisterRequestHandler(PasswordVault passwordVault, Logger logger) {
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
            passwordVault.register(username, password.getHashedPassword(),
                    passwordRepeated.getHashedPassword(), request.getEncryptionData());
        } catch (RepositoryException e) {
            String logMessage = username + " tried to register" +
                    ". It failed because username already exists in repository." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.USERNAME_ALREADY_EXISTS, null);
        } catch (UserAuthenticationException e) {
            String logMessage = username + " tried to register" +
                    ". It failed because passwords are not equal." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.PASSWORDS_DO_NOT_MATCH, null);
        }

        String logMessage = username + " has been registered successfully.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);
        return  new NioResponse(ResponseType.REGISTER_SUCCESSFUL, null);
    }

}