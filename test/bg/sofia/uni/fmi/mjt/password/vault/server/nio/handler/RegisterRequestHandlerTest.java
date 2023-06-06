package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestType;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.response.ResponseType;
import bg.sofia.uni.fmi.mjt.password.vault.server.PasswordVault;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.PasswordsDoNotMatchException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

public class RegisterRequestHandlerTest {

    private static final RequestType REQUEST_TYPE = RequestType.REGISTER;
    private static final String USERNAME = "TEST_USERNAME";
    private static final ServerPassword PASSWORD = new ServerPassword(
            new HashedPassword("somePassword".toCharArray()),
            null);

    private static final NioRequest REQUEST = NioRequest.builder()
            .setType(REQUEST_TYPE)
            .setUsername(USERNAME)
            .setPassword(PASSWORD)
            .setPasswordRepeated(PASSWORD)
            .build();

    private static final String EXCEPTION_MESSAGE = "This is an exception thrown in a test.";

    @Mock
    private final Logger logger = mock(Logger.class);
    @Mock
    private final PasswordVault passwordVaultMock = mock(PasswordVault.class);
    private final NioRequestHandler nioRequestHandler = new RegisterRequestHandler(passwordVaultMock, logger);

    @BeforeEach
    public void setUp() {
        Mockito.reset(passwordVaultMock);
    }

    @Test
    public void testHandleThrowsIllegalArgumentExceptionWhenRequestIsNull()
            throws UserAuthenticationException, RepositoryException {
        assertThrows(IllegalArgumentException.class,
                () -> nioRequestHandler.handle(null),
                "Method should throw an IllegalArgumentException when arguments are null.");
        verify(passwordVaultMock, never()).register(anyString(), Mockito.any(Password.class),
                Mockito.any(Password.class), ArgumentMatchers.isNull());
    }

    @Test
    public void testHandleThrowsIllegalStateExceptionWhenAtLeastOneOfTheRequiredArgumentsIsNull()
            throws UserAuthenticationException, RepositoryException {
        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setPassword(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when passwordRepeated is null.");

        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setPasswordRepeated(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when password argument is null.");


        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setPassword(PASSWORD)
                                .setPasswordRepeated(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when username argument is null.");
        verify(passwordVaultMock, never()).register(anyString(), Mockito.any(Password.class),
                Mockito.any(Password.class), ArgumentMatchers.isNull());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsPasswordsDoNotMatchException()
            throws UserAuthenticationException, RepositoryException {
        doThrow(new PasswordsDoNotMatchException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).register(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), ArgumentMatchers.isNull());
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.PASSWORDS_DO_NOT_MATCH, response.getType(),
                "The type of generated response should be ResponseType.PASSWORDS_DO_NOT_MATCH.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1)).register(anyString(), Mockito.any(Password.class),
                Mockito.any(Password.class), ArgumentMatchers.isNull());
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsUsernameAlreadyExistsException()
            throws UserAuthenticationException, RepositoryException {
        doThrow(new UsernameAlreadyExistsException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).register(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), ArgumentMatchers.isNull());
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.USERNAME_ALREADY_EXISTS, response.getType(),
                "The type of generated response should be ResponseType.USERNAME_ALREADY_EXISTS.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1)).register(anyString(), Mockito.any(Password.class),
                Mockito.any(Password.class), ArgumentMatchers.isNull());
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultDoesNotThrowException()
            throws UserAuthenticationException, RepositoryException {
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.REGISTER_SUCCESSFUL, response.getType(),
                "The type of generated response should be ResponseType.REGISTER_SUCCESSFUL.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1)).register(anyString(), Mockito.any(Password.class),
                Mockito.any(Password.class), ArgumentMatchers.isNull());
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

}