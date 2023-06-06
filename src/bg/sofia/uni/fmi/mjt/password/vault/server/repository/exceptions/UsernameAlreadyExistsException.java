package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public non-sealed class UsernameAlreadyExistsException extends ElementAlreadyExistsException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }

}