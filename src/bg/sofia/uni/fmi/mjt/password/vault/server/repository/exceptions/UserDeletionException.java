package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public class UserDeletionException extends RepositoryException {

    public UserDeletionException(String message) {
        super(message);
    }

    public UserDeletionException(String message, Throwable e) {
        super(message, e);
    }

}