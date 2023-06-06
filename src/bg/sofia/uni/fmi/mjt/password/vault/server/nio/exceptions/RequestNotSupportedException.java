package bg.sofia.uni.fmi.mjt.password.vault.server.nio.exceptions;

public class RequestNotSupportedException extends Exception {

    public RequestNotSupportedException(String message) {
        super(message);
    }

    public RequestNotSupportedException(String message, Throwable e) {
        super(message, e);
    }

}