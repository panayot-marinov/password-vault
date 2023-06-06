package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;

import java.time.LocalDateTime;
import java.util.Arrays;

public class LogoutRequestHandler implements NioRequestHandler {

    private final PasswordVault passwordVault;
    private final Logger logger;

    public LogoutRequestHandler(PasswordVault passwordVault, Logger logger) {
        this.passwordVault = passwordVault;
        this.logger = logger;
    }

    @Override
    public NioResponse handle(NioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request should not be null.");
        }

        String username = request.getUsername();
        if (username == null) {
            throw new IllegalStateException("Username should have a non-null value.");
        }

        try {
            passwordVault.logout(username);
        } catch (UserAuthenticationException e) {
            String logMessage = username + " tried to logout." +
                    "It failed user was not logged in. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            return new NioResponse(ResponseType.USER_NOT_LOGGED_IN, null);
        }

        String logMessage = username + " has logged out.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);
        return new NioResponse(ResponseType.LOGOUT_SUCCESSFUL, null);
    }

}