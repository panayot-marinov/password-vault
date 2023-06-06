package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public class DataFileException extends RuntimeException {

    public DataFileException(String message) {
        super(message);
    }

    public DataFileException(String message, Throwable e) {
        super(message, e);
    }

}