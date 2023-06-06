package bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions;

public class PasswordsDoNotMatchException extends UserAuthenticationException {

    public PasswordsDoNotMatchException(String message) {
        super(message);
    }

    public PasswordsDoNotMatchException(String message, Throwable e) {
        super(message, e);
    }

}