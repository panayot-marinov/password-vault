package bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions;

public class EqualOldAndNewPasswordsException extends UserAuthenticationException {

    public EqualOldAndNewPasswordsException(String message) {
        super(message);
    }

    public EqualOldAndNewPasswordsException(String message, Throwable e) {
        super(message, e);
    }

}