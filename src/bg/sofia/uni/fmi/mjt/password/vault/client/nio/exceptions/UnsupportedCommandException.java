package bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions;

public class UnsupportedCommandException extends Exception {

    public UnsupportedCommandException(String message) {
        super(message);
    }

    public UnsupportedCommandException(String message, Throwable e) {
        super(message, e);
    }

}