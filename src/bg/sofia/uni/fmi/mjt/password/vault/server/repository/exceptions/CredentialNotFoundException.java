package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public non-sealed class CredentialNotFoundException extends ElementNotFoundException {

    public CredentialNotFoundException(String message) {
        super(message);
    }

    public CredentialNotFoundException(String message, Throwable e) {
        super(message, e);
    }

}