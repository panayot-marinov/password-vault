package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public sealed class ElementAlreadyExistsException
        extends RepositoryException permits UsernameAlreadyExistsException, CredentialAlreadyExistsException {

    public ElementAlreadyExistsException(String message) {
        super(message);
    }

    public ElementAlreadyExistsException(String message, Throwable e) {
        super(message, e);
    }

}