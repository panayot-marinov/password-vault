package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.Arrays;

public class LoginRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final Logger logger;

    public LoginRequestHandler(PasswordVault passwordVault, Logger logger) {
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
        if (username == null || password == null) {
            throw new IllegalStateException("Username and password should have non-null values.");
        }

        Session session = null;
        try {
            session = passwordVault.login(username, password.getHashedPassword());
        } catch (UserAuthenticationException e) {
            String logMessage = username + " tried to login. " +
                    "It failed because username or password are invalid. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.INVALID_USERNAME_OR_PASSWORD, null);
        } catch (RepositoryException e) {
            String logMessage = username + " tried to login." +
                    "It failed because user with such an username was not found. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.USER_NOT_FOUND, null);
        }

        String logMessage = username + " logged in successfully.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);

        Gson gson = new Gson();
        return new NioResponse(ResponseType.LOGIN_SUCCESSFUL, gson.toJson(session));
    }

}