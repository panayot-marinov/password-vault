package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public non-sealed class UserNotFoundException extends ElementNotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable e) {
        super(message, e);
    }

}