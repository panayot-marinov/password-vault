package bg.sofia.uni.fmi.mjt.password.vault.client.nio.response;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponseHandler;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioResponseHandlerTest {

    private static final char[] USERNAME = "Username".toCharArray();
    private static final char[] PASSWORD = "password".toCharArray();

    private static final byte[] HASH = "bQeShVmYq3t6w9z$".getBytes(StandardCharsets.UTF_8);
    private static final PasswordDerivedKey PASSWORD_DERIVED_KEY =
            new PasswordDerivedKey(1024, "exampleSalt".getBytes(StandardCharsets.UTF_8),
                    new SecretKeySpec(HASH, 0, HASH.length, "AES"));

    private final NioResponseHandler nioResponseHandler = new NioResponseHandler();

    @Test
    public void testHandleThrowsIllegalArgumentExceptionWhenArgumentsHaveNullValues() {
        assertThrows(IllegalArgumentException.class,
                () -> nioResponseHandler.handle(null, null, null),
                "Method should throw an IllegalArgumentException when arguments are null.");
    }

    @Test
    public void testHandleReturnsEndsSessionWhenResponseIsLogoutSuccessful() {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGOUT, List.of());
        NioResponse response = new NioResponse(ResponseType.LOGOUT_SUCCESSFUL, null);
        Session session = new Session(String.valueOf(USERNAME), PASSWORD_DERIVED_KEY);
        session = nioResponseHandler.handle(command, response, session);

        assertFalse(session.isLoggedIn(), "Session should have state of logged out user.");
        assertEquals(String.valueOf(USERNAME), session.getUsername(), "Session does not have correct username.");
    }

}