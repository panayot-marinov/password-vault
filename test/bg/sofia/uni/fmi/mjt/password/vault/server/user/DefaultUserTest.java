package bg.sofia.uni.fmi.mjt.password.vault.server.user;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentials;
import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentialsKey;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.AccountCredentialsRepository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.RepositoryNotInitialisedException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultUserTest {

    private static final String APP_NAME = "AppName";
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";
    private static final HashedPassword HASHED_PASSWORD = new HashedPassword(PASSWORD.toCharArray());
    private static final EncryptionData ENCRYPTION_DATA =
            new EncryptionData(1024, "123".getBytes(StandardCharsets.UTF_8));
    private static final Writer WRITER = new StringWriter();
    private static final AccountCredentials ACCOUNT_CREDENTIALS = new AccountCredentials(APP_NAME, USERNAME, PASSWORD);
    private static final AccountCredentialsKey ACCOUNT_CREDENTIALS_KEY =
            new AccountCredentialsKey(ACCOUNT_CREDENTIALS.getApplicationName(), ACCOUNT_CREDENTIALS.getUsername());
    @Mock
    private final AccountCredentialsRepository repositoryMock = mock(AccountCredentialsRepository.class);
    private final User userWithRepository = new DefaultUser(USERNAME, HASHED_PASSWORD, ENCRYPTION_DATA, repositoryMock);
    private final User userWithoutRepository = new DefaultUser(USERNAME, HASHED_PASSWORD, ENCRYPTION_DATA);

    @BeforeEach
    public void setUp() {
        Mockito.reset(repositoryMock);
    }

    @AfterAll
    public static void tearDownAll() throws IOException {
        WRITER.close();
    }

    @Test
    public void testChangePasswordThrowsIllegalArgumentExceptionWhenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userWithoutRepository.changePassword(null),
                "Method should throw an IllegalArgumentException when password is null.");
    }

    @Test
    public void testAddCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues() {
        assertThrows(IllegalArgumentException.class,
                () -> userWithoutRepository.addCredentials("", " ", "  ", null),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testAddCredentialsThrowsRepositoryNotInitialisedExceptionWhenRepositoryIsNotSet() {
        assertThrows(RepositoryNotInitialisedException.class,
                () -> userWithoutRepository.addCredentials(APP_NAME, USERNAME, PASSWORD, WRITER),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testAddCredentialsPutsUserInRepositoryWhenNoExceptionIsThrown()
            throws CredentialAlreadyExistsException {
        userWithRepository.addCredentials(APP_NAME, USERNAME, PASSWORD, WRITER);

        verify(repositoryMock, times(1)).put(
                new AccountCredentials(APP_NAME, USERNAME, PASSWORD), WRITER);
    }

    @Test
    public void testUpdateCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues() {
        assertThrows(IllegalArgumentException.class,
                () -> userWithoutRepository.updateCredentials("", " ", "  ", null),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testUpdateCredentialsThrowsRepositoryNotInitialisedExceptionWhenRepositoryIsNotSet() {
        assertThrows(RepositoryNotInitialisedException.class,
                () -> userWithoutRepository.updateCredentials(APP_NAME, USERNAME, PASSWORD, WRITER),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testUpdateCredentialsPutsUserInRepositoryWhenNoExceptionIsThrown()
            throws ElementNotFoundException {
        userWithRepository.updateCredentials(APP_NAME, USERNAME, PASSWORD, WRITER);

        verify(repositoryMock, times(1)).update(
                ACCOUNT_CREDENTIALS, WRITER);
    }

    @Test
    public void testGetCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues() {
        assertThrows(IllegalArgumentException.class,
                () -> userWithoutRepository.getCredentials("", null),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testGetCredentialsThrowsRepositoryNotInitialisedExceptionWhenRepositoryIsNotSet() {
        assertThrows(RepositoryNotInitialisedException.class,
                () -> userWithoutRepository.getCredentials(APP_NAME, USERNAME),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testGetCredentialsGetsUserFromRepositoryWhenNoExceptionIsThrown() throws CredentialNotFoundException {
        when(repositoryMock.get(ACCOUNT_CREDENTIALS_KEY)).thenReturn(ACCOUNT_CREDENTIALS);
        userWithRepository.getCredentials(APP_NAME, USERNAME);

        verify(repositoryMock, times(1)).get(
                ACCOUNT_CREDENTIALS_KEY);
    }

    @Test
    public void testRemoveCredentialsThrowsIllegalArgumentExceptionWhenArgumentsHaveNullOrBlankValues() {
        assertThrows(IllegalArgumentException.class,
                () -> userWithoutRepository.removeCredentials("", null, null),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testRemoveCredentialsThrowsRepositoryNotInitialisedExceptionWhenRepositoryIsNotSet() {
        assertThrows(RepositoryNotInitialisedException.class,
                () -> userWithoutRepository.removeCredentials(APP_NAME, USERNAME, WRITER),
                "Method should throw an IllegalArgumentException when arguments have null or blank values.");
    }

    @Test
    public void testRemoveCredentialsRemovesUserFromRepositoryWhenNoExceptionIsThrown()
            throws CredentialNotFoundException {
        when(repositoryMock.get(ACCOUNT_CREDENTIALS_KEY)).thenReturn(ACCOUNT_CREDENTIALS);
        userWithRepository.removeCredentials(APP_NAME, USERNAME, WRITER);

        verify(repositoryMock, times(1)).remove(ACCOUNT_CREDENTIALS_KEY, WRITER);
    }

    @Test
    public void testDeleteThrowsUserDeletionExceptionWhenRepositoryThrowsAnIOException()
            throws IOException {
        doThrow(new IOException("An IOException message"))
                .when(repositoryMock).deletePathIfExists();

        assertThrows(UserDeletionException.class,
                () -> userWithRepository.delete(),
                "Method should throw an UserDeletionException when repository throws an IOException.");
        verify(repositoryMock, times(1)).deletePathIfExists();
    }

}