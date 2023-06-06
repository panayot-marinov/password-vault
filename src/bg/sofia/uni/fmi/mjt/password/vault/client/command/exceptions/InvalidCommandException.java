package bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions;

public class InvalidCommandException extends Exception {

    public InvalidCommandException(String message) {
        super(message);
    }

    public InvalidCommandException(String message, Throwable e) {
        super(message, e);
    }

}