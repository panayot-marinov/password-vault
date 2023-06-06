package bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions;

public class ConfigurationDataException extends Exception {

    public ConfigurationDataException(String message) {
        super(message);
    }

    public ConfigurationDataException(String message, Throwable e) {
        super(message, e);
    }

}