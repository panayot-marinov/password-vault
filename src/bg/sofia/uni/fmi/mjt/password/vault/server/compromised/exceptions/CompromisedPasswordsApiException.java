package bg.sofia.uni.fmi.mjt.password.vault.server.compromised.exceptions;

public class CompromisedPasswordsApiException extends Exception {

    public CompromisedPasswordsApiException(String message) {
        super(message);
    }

    public CompromisedPasswordsApiException(String message, Throwable e) {
        super(message, e);
    }

}