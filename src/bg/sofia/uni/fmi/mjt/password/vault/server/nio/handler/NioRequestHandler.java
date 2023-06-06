package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestType;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.CompromisedPasswordsClient;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.nio.exceptions.RequestNotSupportedException;

public interface NioRequestHandler {

    NioResponse handle(NioRequest request);

    static NioRequestHandler of(
            NioRequest request, PasswordVault passwordVault,
            CompromisedPasswordsClient compromisedPasswordsClient, Logger logger)
            throws RequestNotSupportedException {
        if (request == null || passwordVault == null || compromisedPasswordsClient == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }

        RequestType requestType = request.getType();

        return switch (requestType) {
            case REGISTER -> new RegisterRequestHandler(passwordVault, logger);
            case DELETE_ACCOUNT -> new DeleteAccountRequestHandler(passwordVault, logger);
            case LOGIN -> new LoginRequestHandler(passwordVault, logger);
            case CHANGE_ACCOUNT_PASSWORD -> new ChangeAccountPasswordRequestHandler(passwordVault, logger);
            case LOGOUT -> new LogoutRequestHandler(passwordVault, logger);
            case STORE_PASSWORD -> new StorePasswordRequestHandler(passwordVault, compromisedPasswordsClient, logger);
            case UPDATE_PASSWORD -> new UpdatePasswordRequestHandler(passwordVault, compromisedPasswordsClient, logger);
            case GET_PASSWORD -> new GetPasswordRequestHandler(passwordVault, logger);
            case REMOVE_PASSWORD -> new RemovePasswordRequestHandler(passwordVault, logger);
            default -> throw new RequestNotSupportedException("Request with such a type is not supported.");
        };
    }

}