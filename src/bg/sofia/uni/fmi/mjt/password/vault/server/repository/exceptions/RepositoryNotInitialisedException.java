
package bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions;

public class RepositoryNotInitialisedException extends RuntimeException {

    public RepositoryNotInitialisedException(String message) {
        super(message);
    }

    public RepositoryNotInitialisedException(String message, Throwable e) {
        super(message, e);
    }

}
