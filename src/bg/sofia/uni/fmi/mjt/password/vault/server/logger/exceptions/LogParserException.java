package bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions;

public class LogParserException extends Exception {

    public LogParserException(String message) {
        super(message);
    }

    public LogParserException(String message, Throwable e) {
        super(message, e);
    }

}