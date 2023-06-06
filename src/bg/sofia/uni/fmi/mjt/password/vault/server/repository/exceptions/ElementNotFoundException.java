package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public sealed class ElementNotFoundException
        extends RepositoryException permits UserNotFoundException, CredentialNotFoundException {

    public ElementNotFoundException(String message) {
        super(message);
    }

    public ElementNotFoundException(String message, Throwable e) {
        super(message, e);
    }

}