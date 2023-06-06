package bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions;

public class InvalidUsernameOrPasswordException extends UserAuthenticationException {

    public InvalidUsernameOrPasswordException(String message) {
        super(message);
    }

    public InvalidUsernameOrPasswordException(String message, Throwable e) {
        super(message, e);
    }

}