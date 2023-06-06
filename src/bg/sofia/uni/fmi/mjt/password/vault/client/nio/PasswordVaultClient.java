package bg.sofia.uni.fmi.mjt.password.vault.client.nio;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.ConnectionException;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;

public interface PasswordVaultClient {

    void connect() throws ConnectionException;

    void disconnect() throws ConnectionException;

    Session send(NioRequest request, ClientCommand command);

    boolean isConnected();

    void updateLogoutTime();

}