package bg.sofia.uni.fmi.mjt.password.vault.server.compromised.exceptions;

public class CompromisedPasswordsClientException extends Exception {

    public CompromisedPasswordsClientException(String message) {
        super(message);
    }

    public CompromisedPasswordsClientException(String message, Throwable e) {
        super(message, e);
    }

}