package bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions;

public class ConnectionException extends Exception {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable e) {
        super(message, e);
    }

}