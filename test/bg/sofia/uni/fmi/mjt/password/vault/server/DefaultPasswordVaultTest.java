package bg.sofia.uni.fmi.mjt.password.vault.server;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.Repository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.DefaultUser;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.EqualOldAndNewPasswordsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.InvalidUsernameOrPasswordException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.PasswordsDoNotMatchException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.exceptions.UserAuthenticationException;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultPasswordVaultTest {

    private static final Path TEST_FOLDER_PATH = Path.of("testData");
    private static final Path TEST_USERS_FILE_PATH = Path.of("Users.dat");
    private static final int ITERATIONS_COUNT = 1024;
    private static final String USERNAME = "USERNAME";
    private static final String APPLICATION_NAME = "APPNAME";
    private static final String CREDENTIALS_USERNAME = "CRED_USER";
    private static final String CREDENTIALS_PASSWORD = "CRED_PSWD";
    private static final Password PASSWORD_1 = new HashedPassword("hashedPassword1".toCharArray());
    private static final Password PASSWORD_2 = new HashedPassword("hashedPassword2".toCharArray());
    private static final EncryptionData ENCRYPTION_DATA =
            new EncryptionData(ITERATIONS_COUNT, "test_salt".getBytes(StandardCharsets.UTF_8));
    private static final String EXCEPTION_MESSAGE = "This is an exception thrown in a test.";

    private static final Writer STRING_WRITER = new StringWriter();
    @Mock
    private final Logger loggerMock = mock(Logger.class);
    @Mock
    private final DefaultUser userMock = mock(DefaultUser.class);
    @Mock
    private Repository<String, DefaultUser> usersMock = mock(Repository.class);
    private PasswordVault passwordVault;
    private PasswordVault passwordVaultWithFiles;

    @BeforeAll
    public static void setUpAll() {
        createFolderIfDoesNotExist(TEST_FOLDER_PATH);
        createFolderIfDoesNotExist(Path.of(TEST_FOLDER_PATH.toString(), "credentials"));
        createFolderIfDoesNotExist(Path.of(TEST_FOLDER_PATH.toString(), "users"));
        createFileIfDoesNotExist(
                Path.of(TEST_FOLDER_PATH.toString(), "users", TEST_USERS_FILE_PATH.toString()));
    }

    @BeforeEach
    public void setUp() {
        passwordVault = new DefaultPasswordVault(usersMock, loggerMock);
        passwordVaultWithFiles = new DefaultPasswordVault(usersMock, loggerMock,
                Path.of(TEST_FOLDER_PATH.toString(), TEST_USERS_FILE_PATH.toString()),
                TEST_FOLDER_PATH + File.separator + "credentials", "dat");

        Mockito.reset(loggerMock);
        Mockito.reset(userMock);
        Mockito.reset(usersMock);
    }

    @AfterAll
    public static void tearDownAll() {
        deleteAllFilesInDirectory(Path.of(TEST_FOLDER_PATH.toString(), "users"));
        deleteAllFilesInDirectory(Path.of(TEST_FOLDER_PATH.toString(), "credentials"));
        deleteAllFilesInDirectory(TEST_FOLDER_PATH);
        try {
            Files.deleteIfExists(Path.of(TEST_FOLDER_PATH.toString(), "users"));
            Files.deleteIfExists(Path.of(TEST_FOLDER_PATH.toString(), "credentials"));
            Files.deleteIfExists(TEST_FOLDER_PATH);
            STRING_WRITER.close();
        } catch (IOException e) {
            fail("Deleting test folder or closing writer has thrown an IOException");
        }

    }

    @Test
    public void testRegisterThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementAlreadyExistsException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.register("  ", null, null,
                        null, STRING_WRITER),
                "Method should thrown an IllegalArgumentException when at least one argument" +
                        "has null or blank value.");
        verify(usersMock, never()).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
    }

    @Test
    public void testRegisterThrowsPasswordsDoNotMatchExceptionWhenPasswordHashesDoNotMatch()
            throws ElementAlreadyExistsException {
        assertThrows(PasswordsDoNotMatchException.class,
                () -> passwordVault.register(USERNAME, PASSWORD_1, PASSWORD_2, ENCRYPTION_DATA, STRING_WRITER),
                "Method should throw a PasswordsDoNotMatchException when passwords have different hashes.");
        verify(usersMock, never()).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
    }

    @Test
    public void testRegisterThrowsUsernameAlreadyExistsExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementAlreadyExistsException {
        when(usersMock.contains(USERNAME)).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> passwordVault.register(USERNAME, PASSWORD_1, PASSWORD_1, ENCRYPTION_DATA, STRING_WRITER),
                "Method should throw a UsernameAlreadyExistsException when user already exists in repository.");
        verify(usersMock, never()).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).contains(USERNAME);
    }

    @Test
    public void testRegisterRegistersUserSuccessfullyWhenNoExceptionsAreThrown()
            throws UserAuthenticationException, RepositoryException {
        when(usersMock.contains(USERNAME)).thenReturn(false);

        passwordVault.register(USERNAME, PASSWORD_1, PASSWORD_1, ENCRYPTION_DATA, STRING_WRITER);
        verify(usersMock, times(1)).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).contains(USERNAME);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testRegisterRegistersUserSuccessfullyAndSavesDataInFileWhenNoExceptionsAreThrown()
            throws UserAuthenticationException, RepositoryException {
        when(usersMock.contains(USERNAME)).thenReturn(false);

        passwordVaultWithFiles.register(USERNAME, PASSWORD_1, PASSWORD_1, ENCRYPTION_DATA);
        verify(usersMock, times(1)).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).contains(USERNAME);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testDeleteAccountThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException, UserDeletionException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.deleteAccount("  ", null, null, STRING_WRITER),
                "Method should thrown an IllegalArgumentException when at least one argument" +
                        "has null or blank value.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
    }

    @Test
    public void testDeleteAccountThrowsPasswordsDoNotMatchExceptionWhenPasswordHashesDoNotMatch()
            throws ElementNotFoundException, UserDeletionException {
        assertThrows(PasswordsDoNotMatchException.class,
                () -> passwordVault.deleteAccount(USERNAME, PASSWORD_1, PASSWORD_2, STRING_WRITER),
                "Method should throw a PasswordsDoNotMatchException when passwords have different hashes.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
    }

    @Test
    public void testDeleteAccountThrowsUserNotFoundExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.deleteAccount(USERNAME, PASSWORD_1, PASSWORD_1, STRING_WRITER),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testDeleteAccountThrowsInvalidUsernameOrPasswordExceptionWhenUserExistsButPasswordDoesNotMatch()
            throws ElementNotFoundException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_2);

        assertThrows(InvalidUsernameOrPasswordException.class,
                () -> passwordVault.deleteAccount(USERNAME, PASSWORD_1, PASSWORD_1, STRING_WRITER),
                "Method should throw a InvalidUsernameOrPasswordException" +
                        " when user already exists in repository and password hashes are not equal.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).getPassword();
    }

    @Test
    public void testDeleteAccountDeletesUserWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws RepositoryException, UserAuthenticationException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVault.deleteAccount(USERNAME, PASSWORD_1, PASSWORD_1, STRING_WRITER);
        verify(usersMock, times(1)).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).getPassword();
    }

    @Test
    public void testDeleteAccountWithFilesDeletesUserWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws RepositoryException, UserAuthenticationException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVaultWithFiles.deleteAccount(USERNAME, PASSWORD_1, PASSWORD_1);
        verify(usersMock, times(1)).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).getPassword();
    }


    @Test
    public void testLoginThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.login("  ", null),
                "Method should thrown an IllegalArgumentException when at least one argument"
                        + "has null or blank value.");
        verify(usersMock, never()).get(anyString());
    }

    @Test
    public void testLoginThrowsUserNotFoundExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.login(USERNAME, PASSWORD_1),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testLoginThrowsInvalidUsernameOrPasswordExceptionWhenUserExistsButPasswordDoesNotMatch()
            throws ElementNotFoundException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_2);

        assertThrows(InvalidUsernameOrPasswordException.class,
                () -> passwordVault.login(USERNAME, PASSWORD_1),
                "Method should throw a InvalidUsernameOrPasswordException" +
                        " when user already exists in repository and password hashes are not equal.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).getPassword();
    }

    @Test
    public void testLoginReturnsSessionObjectWhenUserExistsInRepositoryAndNoExceptionIsThrown()
            throws RepositoryException, UserAuthenticationException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getUsername()).thenReturn(USERNAME);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);
        when(userMock.getEncryptionData()).thenReturn(ENCRYPTION_DATA);

        Session session = passwordVault.login(USERNAME, PASSWORD_1);
        assertTrue(session.isLoggedIn(), "Session flag for logged in user should be true.");
        assertEquals(USERNAME, session.getUsername(), "Session username is not correct.");
        assertEquals(ENCRYPTION_DATA.getIterationsCount(), session.getPasswordDerivedKey().getIterationsCount(),
                "Session iterations count in password derived key is not correct.");
        assertEquals(ENCRYPTION_DATA.getSalt(), session.getPasswordDerivedKey().getSalt(),
                "Session salt in password derived key is not correct.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).getUsername();
        verify(userMock, atLeastOnce()).getPassword();
        verify(userMock, atLeastOnce()).getEncryptionData();
    }

    @Test
    public void testLogoutThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.logout("  "),
                "Method should thrown an IllegalArgumentException when at least one argument"
                        + "has null or blank value.");
        verify(usersMock, never()).get(anyString());
    }

    @Test
    public void testChangePasswordThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.changePassword("  ", null, null,
                        null, STRING_WRITER),
                "Method should thrown an IllegalArgumentException when at least one argument" +
                        "has null or blank value.");
        verify(usersMock, never()).get(anyString());
    }

    @Test
    public void testChangePasswordThrowsInvalidUsernameOrPasswordExceptionWhenUserExistsButPasswordDoesNotMatch()
            throws ElementNotFoundException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_2);

        assertThrows(InvalidUsernameOrPasswordException.class,
                () -> passwordVault.changePassword(USERNAME, PASSWORD_1, PASSWORD_2, PASSWORD_2, STRING_WRITER),
                "Method should throw a InvalidUsernameOrPasswordException" +
                        " when user already exists in repository and password hashes are not equal.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).getPassword();
    }

    @Test
    public void testChangePasswordThrowsUserNotFoundExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.changePassword(USERNAME, PASSWORD_1, PASSWORD_2, PASSWORD_2, STRING_WRITER),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testChangePasswordThrowsPasswordsDoNotMatchExceptionWhenPasswordHashesDoNotMatch()
            throws ElementAlreadyExistsException, ElementNotFoundException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        assertThrows(PasswordsDoNotMatchException.class,
                () -> passwordVault.changePassword(USERNAME, PASSWORD_1, PASSWORD_2, PASSWORD_1, STRING_WRITER),
                "Method should throw a PasswordsDoNotMatchException when passwords have different hashes.");
        verify(usersMock, never()).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
    }

    @Test
    public void testChangePasswordThrowsEqualOldAndNewPasswordsExceptionWhenOldAndNewPasswordHashesAreEqual()
            throws ElementAlreadyExistsException, ElementNotFoundException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        assertThrows(EqualOldAndNewPasswordsException.class,
                () -> passwordVault.changePassword(USERNAME, PASSWORD_1, PASSWORD_1, PASSWORD_1, STRING_WRITER),
                "Method should throw a EqualOldAndNewPasswordsException when old and new password hashes are equal.");
        verify(usersMock, never()).put(Mockito.any(DefaultUser.class), Mockito.any(Writer.class));
    }

    @Test
    public void testChangePasswordChangesUserPasswordWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws RepositoryException, UserAuthenticationException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVault.changePassword(USERNAME, PASSWORD_1, PASSWORD_2, PASSWORD_2, STRING_WRITER);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).changePassword((HashedPassword) PASSWORD_2);
    }

    @Test
    public void testChangePasswordWithFilesChangesUserPasswordWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws RepositoryException, UserAuthenticationException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVaultWithFiles.changePassword(USERNAME, PASSWORD_1, PASSWORD_2, PASSWORD_2);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).changePassword((HashedPassword) PASSWORD_2);
    }

    @Test
    public void testGetEncryptionDataThrowsIllegalArgumentExceptionWhenusernameHasNullOrBlankValue()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.getEncryptionData("  "),
                "Method should thrown an IllegalArgumentException when username argument" +
                        "has null or blank value.");
        verify(usersMock, never()).get(anyString());
    }

    @Test
    public void testGetEncryptionDataThrowsUserNotFoundExceptionWhenUserDoesNotExistInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.getEncryptionData(USERNAME),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testGetEncryptionDataReturnCorrectDataWhenUserExistsInRepositoryAndNoExceptionIsThrown()
            throws RepositoryException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getEncryptionData()).thenReturn(ENCRYPTION_DATA);

        EncryptionData encryptionData = passwordVault.getEncryptionData(USERNAME);
        assertEquals(ENCRYPTION_DATA, encryptionData, "returned encryption data is not equal to user's data.");
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testAddCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.addCredentials("  ", null, null,
                        null, STRING_WRITER),
                "Method should thrown an IllegalArgumentException when at least one argument" +
                        "has null or blank value.");
        verify(usersMock, never()).get(ArgumentMatchers.isNull());
    }

    @Test
    public void testAddCredentialsThrowsUserNotFoundExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.addCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME,
                        CREDENTIALS_PASSWORD, STRING_WRITER),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testAddCredentialsAddsCredentialsSuccessfullyWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException, CredentialAlreadyExistsException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVault.addCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, STRING_WRITER);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).addCredentials(APPLICATION_NAME, CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, STRING_WRITER);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testAddCredentialsWithFilesAddsCredentialsSuccessfullyWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException, CredentialAlreadyExistsException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVaultWithFiles.addCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testUpdateCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.updateCredentials("  ", null, null,
                        null, STRING_WRITER),
                "Method should thrown an IllegalArgumentException when at least one argument" +
                        "has null or blank value.");
        verify(usersMock, never()).get(ArgumentMatchers.isNull());
    }

    @Test
    public void testUpdateCredentialsThrowsUserNotFoundExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.updateCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME,
                        CREDENTIALS_PASSWORD, STRING_WRITER),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testUpdateCredentialsAddsCredentialsSuccessfullyWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException, CredentialAlreadyExistsException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVault.updateCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, STRING_WRITER);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).updateCredentials(APPLICATION_NAME, CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD, STRING_WRITER);
    }

    @Test
    public void testUpdateCredentialsWithFilesAddsCredentialsSuccessfullyWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException, CredentialAlreadyExistsException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVaultWithFiles.updateCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME, CREDENTIALS_PASSWORD);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testRemoveCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.removeCredentials("  ", null, null, STRING_WRITER),
                "Method should thrown an IllegalArgumentException when at least one argument" +
                        "has null or blank value.");
        verify(usersMock, never()).get(ArgumentMatchers.isNull());
    }

    @Test
    public void testRemoveCredentialsThrowsUserNotFoundExceptionWhenTryingToRegisterUserWithExistingUsernameInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.removeCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME, STRING_WRITER),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testRemoveCredentialsRemovesCredentialsSuccessfullyWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVault.removeCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME, STRING_WRITER);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(userMock, atLeastOnce()).removeCredentials(APPLICATION_NAME, CREDENTIALS_USERNAME, STRING_WRITER);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testRemoveCredentialsWithFilesRemovesCredentialsSuccessfullyWhenExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException, UserDeletionException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getPassword()).thenReturn(PASSWORD_1);

        passwordVaultWithFiles.removeCredentials(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME);
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testGetCredentialsPasswordThrowsIllegalArgumentExceptionWhenUsernameHasNullOrBlankValue()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.getCredentialsPassword("  ", null, null),
                "Method should thrown an IllegalArgumentException when username argument" +
                        "has null or blank value.");
        verify(usersMock, never()).get(anyString());
    }

    @Test
    public void testGetCredentialsPasswordThrowsUserNotFoundExceptionWhenUserDoesNotExistInRepository()
            throws ElementNotFoundException, UserDeletionException {
        doThrow(new UserNotFoundException(EXCEPTION_MESSAGE))
                .when(usersMock).get(USERNAME);

        assertThrows(UserNotFoundException.class,
                () -> passwordVault.getCredentialsPassword(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME),
                "Method should throw a UserNotFoundException when user does not exist in repository.");
        verify(usersMock, never()).remove(anyString(), Mockito.any(Writer.class));
        verify(usersMock, atLeastOnce()).get(USERNAME);
    }

    @Test
    public void testGetCredentialsPasswordReturnCorrectDataWhenUserExistsInRepositoryAndNoExceptionIsThrown()
            throws ElementNotFoundException {
        when(usersMock.get(USERNAME)).thenReturn(userMock);
        when(userMock.getCredentials(APPLICATION_NAME, CREDENTIALS_USERNAME)).thenReturn(CREDENTIALS_PASSWORD);

        String credentialsPassword = passwordVault.getCredentialsPassword(USERNAME, APPLICATION_NAME, CREDENTIALS_USERNAME);
        assertEquals(CREDENTIALS_PASSWORD, credentialsPassword, "returned credential password is not equal to user's password.");
        verify(usersMock, atLeastOnce()).get(USERNAME);
        verify(loggerMock, never()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
    }

    @Test
    public void testIsLoggedInThrowsIllegalArgumentExceptionWhenUsernameHaveNullOrBlankValue()
            throws ElementNotFoundException {
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.isLoggedIn("  "),
                "Method should thrown an IllegalArgumentException when username has blank value.");
        assertThrows(IllegalArgumentException.class,
                () -> passwordVault.isLoggedIn(null),
                "Method should thrown an IllegalArgumentException when username has blank value.");
        verify(usersMock, never()).get(ArgumentMatchers.isNull());
    }

    private static void createFolderIfDoesNotExist(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                fail("Creating test folder has thrown an IOException", e);
            }
        }
    }

    private static void createFileIfDoesNotExist(Path path) {
        if (Files.notExists(path)) {
            File file = new File(path.toString());
            try {
                file.createNewFile();
            } catch (IOException e) {
                fail("Creating file has thrown an IOException.", e);
            }
        }
    }

    private static void deleteAllFilesInDirectory(Path dir) {
        try {
            Files.list(dir)
                    .forEach(path -> deleteFile(path));
        } catch (IOException e) {
            fail("Deleting files has thrown an IOException.", e);
        }
    }

    private static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            fail("Deleting file has thrown an IOException.", e);
        }
    }

}