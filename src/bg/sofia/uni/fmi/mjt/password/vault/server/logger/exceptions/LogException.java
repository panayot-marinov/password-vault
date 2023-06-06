package bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions;

public class LogException extends RuntimeException {

    public LogException(String message) {
        super(message);
    }

    public LogException(String message, Throwable e) {
        super(message, e);
    }

}