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
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.EqualOldAndNewPasswordsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.InvalidUsernameOrPasswordException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.PasswordsDoNotMatchException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
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

public class ChangeAccountPasswordRequestHandlerTest {

    private static final RequestType REQUEST_TYPE = RequestType.CHANGE_ACCOUNT_PASSWORD;
    private static final String USERNAME = "TEST_USERNAME";
    private static final ServerPassword PASSWORD = new ServerPassword(
            new HashedPassword("somePassword".toCharArray()),
            null);

    private static final NioRequest REQUEST = NioRequest.builder()
            .setType(REQUEST_TYPE)
            .setUsername(USERNAME)
            .setOldPassword(PASSWORD)
            .setPassword(PASSWORD)
            .setPasswordRepeated(PASSWORD)
            .build();

    private static final String EXCEPTION_MESSAGE = "This is an exception thrown in a test.";

    @Mock
    private final Logger logger = mock(Logger.class);
    @Mock
    private final PasswordVault passwordVaultMock = mock(PasswordVault.class);
    private final NioRequestHandler nioRequestHandler = new ChangeAccountPasswordRequestHandler(passwordVaultMock, logger);

    @BeforeEach
    public void setUp() {
        Mockito.reset(passwordVaultMock);
    }

    @Test
    public void testHandleThrowsIllegalArgumentExceptionWhenRequestIsNull()
            throws RepositoryException, UserAuthenticationException {
        assertThrows(IllegalArgumentException.class,
                () -> nioRequestHandler.handle(null),
                "Method should throw an IllegalArgumentException when arguments are null.");

        verify(passwordVaultMock, never())
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
    }

    @Test
    public void testHandleThrowsIllegalStateExceptionWhenAtLeastOneOfTheRequiredArgumentsIsNull()
            throws RepositoryException, UserAuthenticationException {
        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setOldPassword(PASSWORD)
                                .setPassword(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when passwordRepeated is null.");

        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setOldPassword(PASSWORD)
                                .setPasswordRepeated(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when password argument is null.");

        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setUsername(USERNAME)
                                .setPassword(PASSWORD)
                                .setPasswordRepeated(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when oldPassword argument is null.");

        assertThrows(IllegalStateException.class,
                () -> nioRequestHandler.handle(
                        NioRequest.builder()
                                .setType(REQUEST_TYPE)
                                .setOldPassword(PASSWORD)
                                .setPassword(PASSWORD)
                                .setPasswordRepeated(PASSWORD)
                                .build()),
                "Method should throw an IllegalArgumentException when username argument is null.");

        verify(passwordVaultMock, never())
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsInvalidUsernameOrPasswordException()
            throws RepositoryException, UserAuthenticationException {
        doThrow(new InvalidUsernameOrPasswordException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.INVALID_USERNAME_OR_PASSWORD, response.getType(),
                "The type of generated response should be ResponseType.INVALID_USERNAME_OR_PASSWORD.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsUserNotFoundException()
            throws RepositoryException, UserAuthenticationException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.USER_NOT_FOUND, response.getType(),
                "The type of generated response should be ResponseType.USER_NOT_FOUND.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsPasswordsDoNotMatchException()
            throws RepositoryException, UserAuthenticationException {
        doThrow(new PasswordsDoNotMatchException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.PASSWORDS_DO_NOT_MATCH, response.getType(),
                "The type of generated response should be ResponseType.PASSWORDS_DO_NOT_MATCH.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultThrowsEqualOldAndNewPasswordsException()
            throws RepositoryException, UserAuthenticationException {
        doThrow(new EqualOldAndNewPasswordsException(EXCEPTION_MESSAGE))
                .when(passwordVaultMock).changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.EQUAL_OLD_AND_NEW_PASSWORDS, response.getType(),
                "The type of generated response should be ResponseType.EQUAL_OLD_AND_NEW_PASSWORDS.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    void testHandleReturnsCorrectResponseWhenPasswordVaultDoesNotThrowException()
            throws RepositoryException, UserAuthenticationException {
        NioResponse response = nioRequestHandler.handle(REQUEST);

        assertEquals(ResponseType.ACCOUNT_PASSWORD_CHANGED_SUCCESSFULLY, response.getType(),
                "The type of generated response should be ResponseType.ACCOUNT_PASSWORD_CHANGED_SUCCESSFULLY.");
        assertNull(response.getBody(),
                "The body of generated response should be null.");
        verify(passwordVaultMock, times(1))
                .changePassword(anyString(), Mockito.any(Password.class),
                        Mockito.any(Password.class), Mockito.any(Password.class));
        verify(logger, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

}
