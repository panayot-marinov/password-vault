package bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions;

public class UserAlreadyLoggedInException extends Exception {

    public UserAlreadyLoggedInException(String message) {
        super(message);
    }

    public UserAlreadyLoggedInException(String message, Throwable e) {
        super(message, e);
    }

}