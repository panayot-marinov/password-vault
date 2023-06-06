package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public non-sealed class CredentialAlreadyExistsException extends ElementAlreadyExistsException {

    public CredentialAlreadyExistsException(String message) {
        super(message);
    }

    public CredentialAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }

}