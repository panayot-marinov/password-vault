package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.exceptions;

public class InvalidWordEntryDataException extends RuntimeException {

    public InvalidWordEntryDataException(String message) {
        super(message);
    }

    public InvalidWordEntryDataException(String message, Throwable e) {
        super(message, e);
    }

}