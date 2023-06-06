package bg.sofia.uni.fmi.mjt.password.vault.client.nio.request;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UnsupportedCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserAlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.request.DefaultNioRequestCreator;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestCreator;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestType;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultNioRequestCreatorTest {

    private static final byte[] HASH = "4PhCQ27+Luk3Nu9HJ+IHQQ==".getBytes(StandardCharsets.UTF_8);
    @Mock
    private static final KeyGenerator KEY_GENERATOR_MOCK = mock(KeyGenerator.class);
    @Mock
    private static final PasswordGenerator DEFAULT_PASSWORD_GENERATOR_GENERATOR_MOCK = mock(PasswordGenerator.class);
    @Mock
    private static final PasswordGenerator WORDLIST_PASSWORD_GENERATOR_MOCK = mock(PasswordGenerator.class);
    private static final char[] GENERATED_PASSWORD = "examplePassword".toCharArray();
    private static final PasswordDerivedKey PASSWORD_DERIVED_KEY =
            new PasswordDerivedKey(1024, "exampleSalt".getBytes(StandardCharsets.UTF_8),
                    new SecretKeySpec(HASH, 0, HASH.length, "AES"));
    private static final char[] APP_NAME = "AppName".toCharArray();
    private static final char[] USERNAME = "Username".toCharArray();
    private static final char[] PASSWORD = "password".toCharArray();
    private static final Session SESSION_LOGGED_IN = new Session(String.valueOf(USERNAME),
            PASSWORD_DERIVED_KEY);
    private static final Session SESSION_NOT_LOGGED_IN = new Session(String.valueOf(USERNAME),
            PASSWORD_DERIVED_KEY);

    private final RequestCreator nioRequestCreator =
            new DefaultNioRequestCreator(KEY_GENERATOR_MOCK, DEFAULT_PASSWORD_GENERATOR_GENERATOR_MOCK, WORDLIST_PASSWORD_GENERATOR_MOCK);

    @BeforeAll
    public static void setUpAll() {
        SESSION_NOT_LOGGED_IN.end();
    }

    @BeforeEach
    public void setUp() {
        Mockito.reset(KEY_GENERATOR_MOCK);
        Mockito.reset(DEFAULT_PASSWORD_GENERATOR_GENERATOR_MOCK);
        Mockito.reset(WORDLIST_PASSWORD_GENERATOR_MOCK);
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionWhenCommandIsNull()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(null, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when command is null.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserAlreadyLoggedInExceptionWhenCommandIsRegisterAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.REGISTER, List.of(USERNAME, PASSWORD, PASSWORD));
        assertThrows(UserAlreadyLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an UserAlreadyLoggedInException when session has logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsRegisterAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.REGISTER, List.of(USERNAME, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsNotLoggedInAndCommandIsRegister()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        when(KEY_GENERATOR_MOCK.generateKey(PASSWORD)).thenReturn(PASSWORD_DERIVED_KEY);
        ClientCommand command = new ClientCommand(ClientCommandType.REGISTER, List.of(USERNAME, PASSWORD, PASSWORD));

        ServerPassword password = new ServerPassword(new HashedPassword(PASSWORD), null);
        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.REGISTER)
                .setUsername(String.valueOf(USERNAME))
                .setPassword(password)
                .setPasswordRepeated(password)
                .build();


        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of REGISTER and a correct username.");
        assertEquals(password, createdRequest.getPassword(),
                "Created request's password is not equal to expected password.");
        assertEquals(password, createdRequest.getPasswordRepeated(),
                "Created request's passwordRepeated is not equal to expected passwordRepeated.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, times(1)).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsDeleteAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.DELETE_ACCOUNT, List.of(PASSWORD, PASSWORD));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsDeleteAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.DELETE_ACCOUNT, List.of(USERNAME, PASSWORD, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsDelete()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.DELETE_ACCOUNT, List.of(PASSWORD, PASSWORD));

        ServerPassword password = new ServerPassword(new HashedPassword(PASSWORD), null);
        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.DELETE_ACCOUNT)
                .setUsername(String.valueOf(USERNAME))
                .setPassword(password)
                .setPasswordRepeated(password)
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of DELETE_ACCOUNT and a correct username.");
        assertEquals(password, createdRequest.getPassword(),
                "Created request's password is not equal to expected password.");
        assertEquals(password, createdRequest.getPasswordRepeated(),
                "Created request's passwordRepeated is not equal to expected passwordRepeated.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserAlreadyLoggedInExceptionWhenCommandIsLoginAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGIN, List.of(USERNAME, PASSWORD));
        assertThrows(UserAlreadyLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an UserAlreadyLoggedInException when session has logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsLoginAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGIN, List.of(USERNAME, PASSWORD, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsLogin()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGIN, List.of(USERNAME, PASSWORD));

        ServerPassword password = new ServerPassword(new HashedPassword(PASSWORD), null);
        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.LOGIN)
                .setUsername(String.valueOf(USERNAME))
                .setPassword(password)
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of LOGIN and a correct username.");
        assertEquals(password, createdRequest.getPassword(),
                "Created request's password is not equal to expected password.");

        verify(KEY_GENERATOR_MOCK, never())
                .generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsChangePasswordAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.CHANGE_PASSWORD, List.of(PASSWORD, PASSWORD, PASSWORD));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsChangePasswordAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.CHANGE_PASSWORD, List.of(PASSWORD, PASSWORD));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsNotLoggedInAndCommandIsChangePassword()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.CHANGE_PASSWORD, List.of(PASSWORD, PASSWORD, PASSWORD));

        ServerPassword password = new ServerPassword(new HashedPassword(PASSWORD), null);
        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.CHANGE_ACCOUNT_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .setOldPassword(password)
                .setPassword(password)
                .setPasswordRepeated(password)
                .build();


        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of CHANGE_PASSWORD and a correct username.");
        assertEquals(password, createdRequest.getOldPassword(),
                "Created request's old password is not equal to expected password.");
        assertEquals(password, createdRequest.getPassword(),
                "Created request's password is not equal to expected password.");
        assertEquals(password, createdRequest.getPasswordRepeated(),
                "Created request's passwordRepeated is not equal to expected passwordRepeated.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsLogoutAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGOUT, List.of());
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsLogoutAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGOUT, List.of(USERNAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsLogout()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.LOGOUT, List.of());

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.LOGOUT)
                .setUsername(String.valueOf(USERNAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of DELETE_ACCOUNT and a correct username.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsGeneratePasswordAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.GENERATE_PASSWORD, List.of(PASSWORD, PASSWORD));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsGeneratePasswordAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.GENERATE_PASSWORD, List.of(APP_NAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsGeneratePassword()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        when(DEFAULT_PASSWORD_GENERATOR_GENERATOR_MOCK.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(KEY_GENERATOR_MOCK.generateKey(PASSWORD)).thenReturn(PASSWORD_DERIVED_KEY);
        ClientCommand command = new ClientCommand(ClientCommandType.GENERATE_PASSWORD, List.of(APP_NAME, USERNAME));

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.STORE_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of GENERATE_PASSWORD and a correct username.");

        verify(DEFAULT_PASSWORD_GENERATOR_GENERATOR_MOCK, times(1)).generatePassword();
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsGeneratePasswordWordsAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.GENERATE_PASSWORD_WORDS, List.of(PASSWORD, PASSWORD));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsGeneratePasswordWordsAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.GENERATE_PASSWORD_WORDS, List.of(APP_NAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsGeneratePasswordWords()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        when(WORDLIST_PASSWORD_GENERATOR_MOCK.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(KEY_GENERATOR_MOCK.generateKey(PASSWORD)).thenReturn(PASSWORD_DERIVED_KEY);
        ClientCommand command = new ClientCommand(ClientCommandType.GENERATE_PASSWORD_WORDS, List.of(APP_NAME, USERNAME));

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.STORE_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of GENERATE_PASSWORD_WORDS and a correct username.");

        verify(WORDLIST_PASSWORD_GENERATOR_MOCK, times(1)).generatePassword();
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsAddPasswordAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.ADD_PASSWORD, List.of(APP_NAME, USERNAME, PASSWORD));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsAddPasswordAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.ADD_PASSWORD, List.of(APP_NAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsAddPassword()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.ADD_PASSWORD, List.of(APP_NAME, USERNAME, PASSWORD));

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.STORE_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .setCredentialsUsername(String.valueOf(USERNAME))
                .setApplicationName(String.valueOf(APP_NAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of ADD_PASSWORD and a correct username.");
        assertEquals(String.valueOf(USERNAME), createdRequest.getCredentialsUsername(),
                "Created request's credentials username is not equal to expected.");
        assertEquals(String.valueOf(APP_NAME), createdRequest.getApplicationName(),
                "Created request's application name  is not equal to expected.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsUpdatePasswordAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.UPDATE_PASSWORD, List.of(APP_NAME, USERNAME, PASSWORD));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsUpdatePasswordAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.UPDATE_PASSWORD, List.of(APP_NAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsUpdatePassword()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.UPDATE_PASSWORD, List.of(APP_NAME, USERNAME, PASSWORD));

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.UPDATE_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .setCredentialsUsername(String.valueOf(USERNAME))
                .setApplicationName(String.valueOf(APP_NAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of UPDATE_PASSWORD and a correct username.");
        assertEquals(String.valueOf(USERNAME), createdRequest.getCredentialsUsername(),
                "Created request's credentials username is not equal to expected.");
        assertEquals(String.valueOf(APP_NAME), createdRequest.getApplicationName(),
                "Created request's application name  is not equal to expected.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsRetrieveCredentialsAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.RETRIEVE_CREDENTIALS, List.of(APP_NAME, USERNAME));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsRetrieveCredentialsAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.RETRIEVE_CREDENTIALS, List.of(APP_NAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsRetrieveCredentials()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.RETRIEVE_CREDENTIALS, List.of(APP_NAME, USERNAME));

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.GET_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .setCredentialsUsername(String.valueOf(USERNAME))
                .setApplicationName(String.valueOf(APP_NAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of UPDATE_PASSWORD and a correct username.");
        assertEquals(String.valueOf(USERNAME), createdRequest.getCredentialsUsername(),
                "Created request's credentials username is not equal to expected.");
        assertEquals(String.valueOf(APP_NAME), createdRequest.getApplicationName(),
                "Created request's application name  is not equal to expected.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsUserNotLoggedInExceptionWhenCommandIsRemovePasswordAndSessionIsLoggedIn()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.REMOVE_PASSWORD, List.of(APP_NAME, USERNAME));
        assertThrows(UserNotLoggedInException.class, () -> nioRequestCreator.create(command, SESSION_NOT_LOGGED_IN),
                "Method should throw an UserNotLoggedInException when session does not have logged in state.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateThrowsIllegalArgumentExceptionExceptionWhenCommandIsRemovePasswordAndArgumentsCountIsNotCorrect()
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.REMOVE_PASSWORD, List.of(APP_NAME));
        assertThrows(IllegalArgumentException.class, () -> nioRequestCreator.create(command, SESSION_LOGGED_IN),
                "Method should throw an IllegalArgumentException when arguments count is not correct.");
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

    @Test
    public void testCreateReturnsAValidNioRequestWhenUserIsLoggedInAndCommandIsRemovePassword()
            throws UnsupportedCommandException, UserNotLoggedInException, UserAlreadyLoggedInException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        ClientCommand command = new ClientCommand(ClientCommandType.REMOVE_PASSWORD, List.of(APP_NAME, USERNAME));

        NioRequest expectedRequest = NioRequest.builder()
                .setType(RequestType.REMOVE_PASSWORD)
                .setUsername(String.valueOf(USERNAME))
                .setCredentialsUsername(String.valueOf(USERNAME))
                .setApplicationName(String.valueOf(APP_NAME))
                .build();

        NioRequest createdRequest = nioRequestCreator.create(command, SESSION_LOGGED_IN);
        assertEquals(expectedRequest, createdRequest,
                "Created request should have a type of UPDATE_PASSWORD and a correct username.");
        assertEquals(String.valueOf(USERNAME), createdRequest.getCredentialsUsername(),
                "Created request's credentials username is not equal to expected.");
        assertEquals(String.valueOf(APP_NAME), createdRequest.getApplicationName(),
                "Created request's application name  is not equal to expected.");

        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class), Mockito.any(byte[].class));
        verify(KEY_GENERATOR_MOCK, never()).generateKey(Mockito.any(char[].class));
    }

}