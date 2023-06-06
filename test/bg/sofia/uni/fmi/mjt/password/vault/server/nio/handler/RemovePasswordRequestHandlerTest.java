package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestType;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RemovePasswordRequestHandlerTest {

    private static final RequestType REQUEST_TYPE = RequestType.REMOVE_PASSWORD;
    private static final String USERNAME = "TEST_USERNAME";
    private static final String APP_NAME = "TEST_APP_NAME";

    private static final NioRequest REQUEST = NioRequest.builder()
            .setType(REQUEST_TYPE)
            .setUsername(USERNAME)
            .setCredentialsUsername(USERNAME)
            .setApplicationName(APP_NAME)
            .build();

    private static final String EXCEPTION_MESSAGE = "This is an exception thrown in a test.";

    @Mock
    private final Logger logger = mock(Logger.class);
    @Mock
    private final PasswordVault passwordVaultMock = mock(PasswordVault.class);
    private final NioRequestHandler nioRequestHandler = new RemovePasswordRequestHandler(passwordVaultMock, logger);

    @BeforeEach
    public void setUp() {
        Mockito.reset(passwordVaultMock);
    }

    @Test
    public void testHandleThrowsIllegalArgumentExceptionWhenRequestIsNull()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> nioRequestHandler.handle(null),
                "Method should throw an IllegalArgumentException when arguments are null.");
        verify(passwordVaultMock, never())
                .removeCredentials(anyString(), anyString(), anyString());
    }

    @Test
    public void testHandleThrowsIllegalStateExceptionWhenAtLeastOneOfTheRequiredArgumentsIsNull()
            throws ElementNotFoundException {
        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setCredentialsUsername(USERNAME)
                                .build()),
                "Method should throw an IllegalArgumentException when applicationName is null.");

        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setApplicationName(APP_NAME)
                                .build()),
                "Method should throw an IllegalArgumentException when credentialsUsername is null.");

        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setCredentialsUsername(USERNAME)
                                .setApplicationName(APP_NAME)
                                .build()),
                "Method should throw an IllegalArgumentException when username argument is null.");
        verify(passwordVaultMock, never())
                .removeCredentials(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsUserNotFoundException()
            throws ElementNotFoundException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).removeCredentials(anyString(), anyString(), anyString());
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.USER_NOT_FOUND, response.getType(),
                "The type of generated response should be ResponseType.USER_NOT_FOUND.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .removeCredentials(anyString(), anyString(), anyString());
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsCredentialNotFoundException()
            throws ElementNotFoundException {
        doThrow(new CredentialNotFoundException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).removeCredentials(anyString(), anyString(), anyString());
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.CREDENTIALS_NOT_FOUND, response.getType(),
                "The type of generated response should be ResponseType.PASSWORDS_DO_NOT_MATCH.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .removeCredentials(anyString(), anyString(), anyString());
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultDoesNotThrowException() throws ElementNotFoundException {
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.PASSWORD_REMOVED_SUCCESSFULLY, response.getType(),
                "The type of generated response should be ResponseType.PASSWORD_REMOVED_SUCCESSFULLY.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .removeCredentials(anyString(), anyString(), anyString());
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

}