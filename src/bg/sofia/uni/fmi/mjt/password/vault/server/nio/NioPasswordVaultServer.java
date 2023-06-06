package bg.sofia.uni.fmi.mjt.password.vault.server.nio;

import bg.sofia.uni.fmi.mjt.password.vault.configuration.ConfigurationData;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.CompromisedPasswordsClient;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.nio.exceptions.RequestNotSupportedException;
import bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler.NioRequestHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class NioPasswordVaultServer implements PasswordVaultServer {

    private static final Gson GSON = new Gson();
    private final String serverHost;
    private static final int BUFFER_SIZE = 1024;
    private final int port;
    private final PasswordVault passwordVault;
    private final CompromisedPasswordsClient compromisedPasswordsClient;
    private final Logger logger;

    private boolean isServerWorking = true;
    private Selector selector;

    private final boolean hasDisconnected = false;

    public NioPasswordVaultServer(ConfigurationData data, PasswordVault passwordVault,
                                  CompromisedPasswordsClient compromisedPasswordsClient, Logger logger) {
        this.serverHost = data.getServerHost();
        this.port = data.getServerPort();
        this.passwordVault = passwordVault;
        this.compromisedPasswordsClient = compromisedPasswordsClient;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(serverHost, port));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            String logMessage = "The server has been started.";
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            while (!hasDisconnected && isServerWorking) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    // select() is blocking but may still return with 0, check javadoc
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        //Write mode
                        buffer.clear();
                        int readSymbols = 0;
                        try {
                            readSymbols = socketChannel.read(buffer);

                        } catch (IOException e) {
                            System.out.println("Client has disconnected.");
                            continue;
                        }
                        if (readSymbols < 0) {
                            System.out.println("Client has closed the connection");
                            socketChannel.close();
                            continue;
                        }

                        NioRequest request = readRequestFromClient(buffer, readSymbols);
                        NioResponse response = handleRequest(request);
                        try {
                            writeResponseToClient(buffer, socketChannel, response);
                        } catch (IOException e) {
                            System.out.println("Client has disconnected.");
                            continue;
                        }
                    } else if (key.isAcceptable()) {
                        acceptNewClient(key);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            String logMessage = "A problem with the server socket occurred." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            throw new UncheckedIOException("A problem with the server socket occurred.", e);
        }
    }

    @Override
    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }

        String logMessage = "The server has been stopped.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);
    }

    private void acceptNewClient(SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel newClient = socketChannel.accept();
        newClient.configureBlocking(false);
        newClient.register(selector, SelectionKey.OP_READ);
    }

    private NioResponse handleRequest(NioRequest request) {
        NioResponse response = null;
        try {
            NioRequestHandler handler =
                    NioRequestHandler.of(request, passwordVault, compromisedPasswordsClient, logger);
            response = handler.handle(request);
        } catch (RequestNotSupportedException e) {
            String logMessage = "Client has send an unsupported request. Type: " + request.getType().toString() +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.WARN, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            response = new NioResponse(ResponseType.REQUEST_NOT_SUPPORTED, null);
        }
        return response;
    }

    private NioRequest readRequestFromClient(ByteBuffer buffer, int readSymbols) {
        int requestSymbols = readSymbols - System.lineSeparator().length();
        byte[] byteRequest = new byte[requestSymbols];
        //Read mode
        buffer.flip();
        buffer.get(byteRequest, 0, requestSymbols);
        //System.out.println("Command: " + new String(byteRequest, StandardCharsets.UTF_8));

        NioRequest request = NioRequest.of(byteRequest);
        return request;
    }

    private void writeResponseToClient(ByteBuffer buffer, SocketChannel socketChannel, NioResponse response)
            throws IOException {
        String responseMessage = GSON.toJson(response) + System.lineSeparator();
        //Write mode
        buffer.clear();
        buffer.put(responseMessage.getBytes(StandardCharsets.UTF_8));

        //Read mode
        buffer.flip();
        socketChannel.write(buffer);
    }

}