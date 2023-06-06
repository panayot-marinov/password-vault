package bg.sofia.uni.fmi.mjt.password.vault.client.nio;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.ConfigurationData;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.ConnectionException;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequestSender;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class NioPasswordVaultClient implements PasswordVaultClient {

    private static final int INACTIVE_TIME_LOGOUT = 60_000;

    private final String serverHost;
    private final int serverPort;

    private NioRequestSender nioRequestSender;
    private AutoLogoutService autoLogoutService;
    private SocketChannel socketChannel;
    private BufferedReader reader;
    private PrintWriter writer;
    private Session session = null;
    private boolean logoutServiceRunning = false;
    private boolean isConnected = false;
    private final ExecutorService executorService;

    public NioPasswordVaultClient(ConfigurationData data, ExecutorService executorService) {
        this.serverHost = data.getServerHost();
        this.serverPort = data.getServerPort();
        this.executorService = executorService;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        } catch (IOException e) {
            throw new ConnectionException("Cannot connect to the server. Please enter valid configuration data.", e);
        }

        reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
        writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
        nioRequestSender = new NioRequestSender(reader, writer);
        autoLogoutService = new AutoLogoutService(INACTIVE_TIME_LOGOUT, nioRequestSender);
        isConnected = true;

        System.out.println("Connected to the server.");
    }

    @Override
    public void disconnect() throws ConnectionException {
        if (!isConnected) {
            System.out.println("Client is not connected");
            return;
        }

        if (session != null) {
            session.end();
        }
        try {
            socketChannel.close();
            reader.close();
        } catch (IOException e) {
            throw new ConnectionException("Cannot disconnect from the server.", e);
        }
        writer.close();
        isConnected = false;

        System.out.println("Disconnected from the server.");
    }

    @Override
    public Session send(NioRequest request, ClientCommand command) {
        if (request == null || command == null) {
            throw new IllegalArgumentException("Neither request nor command can have null values.");
        }

        if (!isConnected) {
            System.out.println("Client is not connected");
            return null;
        }

        autoLogoutService.updateMaxTime();
        session = nioRequestSender.sendRequest(request, command, session);
        if (session != null && session.isLoggedIn() && !logoutServiceRunning) {
            CompletableFuture.supplyAsync(() -> autoLogoutService.logoutAfterDelay(session), executorService)
                    .thenAccept(session -> autoLogout(session));
            logoutServiceRunning = true;
        }

        return session;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void updateLogoutTime() {
        autoLogoutService.updateMaxTime();
    }

    private void autoLogout(Session newSession) {
        session = newSession;
        logoutServiceRunning = false;
    }
}