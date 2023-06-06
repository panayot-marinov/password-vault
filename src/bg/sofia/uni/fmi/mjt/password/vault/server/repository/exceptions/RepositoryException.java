package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public class RepositoryException extends Exception {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable e) {
        super(message, e);
    }

}