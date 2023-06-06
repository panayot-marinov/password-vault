package bg.sofia.uni.fmi.mjt.password.vault.client.nio;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.ConfigurationData;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioPasswordVaultClientTest {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ConfigurationData configurationData = new ConfigurationData("host", 1234);
    private final NioPasswordVaultClient passwordVaultClient = new NioPasswordVaultClient(configurationData, executorService);

    @Test
    public void testSendThrowsIllegalArgumentExceptionWhenAtLeastOneOfTheArgumentsIsNull() {
        NioRequest request = NioRequest.builder()
                .setType(RequestType.LOGOUT)
                .setUsername("exampleUsername")
                .build();
        ClientCommand command = new ClientCommand(ClientCommandType.LOGOUT, List.of());

        assertThrows(IllegalArgumentException.class, () -> passwordVaultClient.send(null, command),
                "method should throw an IllegalArgumentException when request is null");

        assertThrows(IllegalArgumentException.class, () -> passwordVaultClient.send(request, null),
                "Method should throw an IllegalArgumentException when request is null");

        assertThrows(IllegalArgumentException.class, () -> passwordVaultClient.send(null, null),
                "Method should throw an IllegalArgumentException when request and command are null");
    }

}