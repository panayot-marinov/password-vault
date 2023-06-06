package bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions;

public class InvalidArgumentsCountException extends InvalidCommandException {

    public InvalidArgumentsCountException(String message) {
        super(message);
    }

    public InvalidArgumentsCountException(String message, Throwable e) {
        super(message, e);
    }

}