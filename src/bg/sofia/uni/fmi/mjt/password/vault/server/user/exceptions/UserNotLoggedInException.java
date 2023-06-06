package bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions;

public class UserNotLoggedInException extends UserAuthenticationException {

    public UserNotLoggedInException(String message) {
        super(message);
    }

    public UserNotLoggedInException(String message, Throwable e) {
        super(message, e);
    }

}