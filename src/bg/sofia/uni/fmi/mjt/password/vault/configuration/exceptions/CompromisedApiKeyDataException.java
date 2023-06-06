package bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions;

public class CompromisedApiKeyDataException extends Exception {

    public CompromisedApiKeyDataException(String message) {
        super(message);
    }

    public CompromisedApiKeyDataException(String message, Throwable e) {
        super(message, e);
    }

}