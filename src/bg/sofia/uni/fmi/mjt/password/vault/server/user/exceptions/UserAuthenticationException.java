package bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions;

public class UserAuthenticationException extends Exception {

    public UserAuthenticationException(String message) {
        super(message);
    }

    public UserAuthenticationException(String message, Throwable e) {
        super(message, e);
    }

}