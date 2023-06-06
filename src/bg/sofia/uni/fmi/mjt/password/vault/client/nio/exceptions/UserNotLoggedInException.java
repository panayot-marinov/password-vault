package bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions;

public class UserNotLoggedInException extends Exception {

    public UserNotLoggedInException(String message) {
        super(message);
    }

    public UserNotLoggedInException(String message, Throwable e) {
        super(message, e);
    }

}