package bg.sofia.uni.fmi.mjt.password.vault.server.repository;

import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentials;
import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentialsKey;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.AccountCredentialsRepository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AccountCredentialsRepositoryTest {

    private static final Map<AccountCredentialsKey, AccountCredentials> ACCOUNT_CREDENTIALS = Map.of(
            new AccountCredentialsKey("app1", "user1"),
            new AccountCredentials("app1", "user1", "encPass1"),
            new AccountCredentialsKey("app2", "user2"),
            new AccountCredentials("app2", "user2", "encPass2"),
            new AccountCredentialsKey("app3", "user3"),
            new AccountCredentials("app3", "user3", "encPass3")
    );

    private static final String ACCOUNT_CREDENTIALS_MERGED_STRING =
            String.join(System.lineSeparator(),
                    ACCOUNT_CREDENTIALS.values().stream()
                            .map(credentials -> credentials.toString())
                            .toList());
    private StringReader stringReader;
    private StringWriter stringWriter;
    private Repository<AccountCredentialsKey, AccountCredentials> accountCredentialsRepository;

    @BeforeEach
    public void setUp() {
        stringReader = new StringReader(ACCOUNT_CREDENTIALS_MERGED_STRING);
        stringWriter = new StringWriter();
        accountCredentialsRepository = new AccountCredentialsRepository(stringReader);
    }

    @AfterEach
    public void tearDown() {
        stringReader.close();
        try {
            stringWriter.close();
        } catch (IOException e) {
            fail("Closing stringWriter has thrown an IOException.");
        }
    }


    @Test
    public void testGetAllReturnsAllAccountCredentials() {
        Map<AccountCredentialsKey, AccountCredentials> accountCredentials = accountCredentialsRepository.getAll();
        assertTrue(mapsAreEqual(ACCOUNT_CREDENTIALS, accountCredentials),
                "returned map is not equal to the input map.");
    }

    @Test
    public void testGetThrowsIllegalArgumentExceptionWhenKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> accountCredentialsRepository.get(null),
                "Method should throw an IllegalArgumentException when key is null.");
    }

    @Test
    public void testGetThrowsCredentialNotFoundExceptionWhenKeyIsNotFound() {
        assertThrows(CredentialNotFoundException.class,
                () -> accountCredentialsRepository.get(
                        new AccountCredentialsKey("app4", "user6")),
                "Method should throw an CredentialsNotFoundException when key is not found.");
    }

    @Test
    public void testGetReturnsCorrectAccountCredentialsWhenKeyExists() {
        AccountCredentialsKey key = new AccountCredentialsKey("app3", "user3");

        AccountCredentials expectedCredentials = ACCOUNT_CREDENTIALS.get(key);
        AccountCredentials actualCredentials = null;
        try {
            actualCredentials = accountCredentialsRepository.get(key);
        } catch (ElementNotFoundException e) {
            fail("Method has thrown an ElementNotFoundException");
        }

        assertEquals(expectedCredentials, actualCredentials,
                "Method does not return correct account credentials.");
    }

    @Test
    public void testContainsThrowsIllegalArgumentExceptionWhenKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> accountCredentialsRepository.contains(null),
                "Method should throw an IllegalArgumentException when key is null.");
    }

    @Test
    public void testContainsReturnsFalseWhenKeyIsNotFound() {
        assertFalse(accountCredentialsRepository.contains(
                        new AccountCredentialsKey("app4", "user6")),
                "Method should return false when key is not found.");
    }

    @Test
    public void testContainsReturnsTrueWhenKeyExists() {
        AccountCredentialsKey key = new AccountCredentialsKey("app3", "user3");
        assertTrue(accountCredentialsRepository.contains(key),
                "Method should return false when key exists.");
    }

    @Test
    public void testPutThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> accountCredentialsRepository.put(null, stringWriter),
                "Method should throw an IllegalArgumentException when value is null.");

        assertThrows(IllegalArgumentException.class, () -> accountCredentialsRepository.put(
                        new AccountCredentials("someName",
                                "someUsername", "somePass"), null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testPutThrowsCredentialAlreadyExistsExceptionWhenKeyExists() {
        AccountCredentials accountCredentials =
                new AccountCredentials("app3", "user3", "somePassword");

        assertThrows(CredentialAlreadyExistsException.class,
                () -> accountCredentialsRepository.put(accountCredentials, stringWriter),
                "Method should throw an AccountCredentialsNotFoundException when key  exists.");
        assertEquals(stringWriter.toString(), "",
                "put should not have written any data on writer when data is not updated successfully.");
    }

    @Test
    public void testPutSavesCredentialsCorrectlyWhenKeyDoesNotExist() {
        AccountCredentials accountCredentials =
                new AccountCredentials("app4", "user4", "somePassword");

        try {
            accountCredentialsRepository.put(accountCredentials, stringWriter);
        } catch (ElementAlreadyExistsException e) {
            fail("Method has thrown an ElementAlreadyExistsException.");
        }

        assertTrue(accountCredentialsRepository.contains(
                        new AccountCredentialsKey(accountCredentials.getApplicationName(), accountCredentials.getUsername())),
                "Credentials repository should contain the newly added key after put operation.");
        assertEquals(stringWriter.toString(), accountCredentials + System.lineSeparator(),
                "put has not written correct data on writer.");
    }

    @Test
    public void testUpdateThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> accountCredentialsRepository.update(null, stringWriter),
                "Method should throw an IllegalArgumentException when value is null.");
        assertThrows(IllegalArgumentException.class,
                () -> accountCredentialsRepository.update(new AccountCredentials("someName",
                        "someUsername", "somePass"), null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testUpdateThrowsCredentialNotFoundExceptionWhenKeyIsNotFound() {
        assertThrows(CredentialNotFoundException.class,
                () -> accountCredentialsRepository.update(
                        new AccountCredentials("app4", "user6", "somePassword"), stringWriter),
                "Method should throw an CredentialsNotFoundException when key is not found.");
        assertEquals(stringWriter.toString().length(), ACCOUNT_CREDENTIALS_MERGED_STRING.length() + System.lineSeparator().length(),
                "update should not have written any new data on writer when data is not updated successfully.");
    }

    @Test
    public void testUpdateUpdatesCredentialsCorrectlyWhenKeyDoesExist() {
        AccountCredentials accountCredentials =
                new AccountCredentials("app3", "user3", "somePassword");

        try {
            accountCredentialsRepository.update(accountCredentials, stringWriter);
        } catch (ElementNotFoundException e) {
            fail("Method has thrown an ElementNotFoundException.");
        }

        try {
            assertEquals(accountCredentials, accountCredentialsRepository.get(
                            new AccountCredentialsKey(accountCredentials.getApplicationName(),
                                    accountCredentials.getUsername())),
                    "The updated account credentials value is not correct.");
        } catch (ElementNotFoundException e) {
            fail("Method thrown an ElementNotFoundException.");
        }

        String expectedAccountCredentialsString =
                String.join(System.lineSeparator(),
                        ACCOUNT_CREDENTIALS.values().stream()
                                .filter(credentials -> !credentials.getApplicationName().equals("app3"))
                                .map(credentials -> credentials.toString())
                                .toList()) + System.lineSeparator() +
                        accountCredentials + System.lineSeparator();

        assertTrue(stringWriter.toString().contains(accountCredentials.toString()) &&
                        stringWriter.toString().length() == expectedAccountCredentialsString.length(),
                "update has not written correct data on writer.");
    }

    @Test
    public void testRemoveThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> accountCredentialsRepository.remove(null, stringWriter),
                "Method should throw an IllegalArgumentException when key is null.");
        assertThrows(IllegalArgumentException.class,
                () -> accountCredentialsRepository.remove(
                        new AccountCredentialsKey("someName", "someUser"), null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testRemoveThrowsCredentialNotFoundExceptionWhenKeyIsNotFound() {
        assertThrows(CredentialNotFoundException.class,
                () -> accountCredentialsRepository.remove(
                        new AccountCredentialsKey("app4", "user6"), stringWriter),
                "Method should throw an CredentialsNotFoundException when key is not found.");
        assertEquals(stringWriter.toString().length(), ACCOUNT_CREDENTIALS_MERGED_STRING.length() + System.lineSeparator().length(),
                "remove should not have written any new data on writer when data is not removed successfully.");
    }

    @Test
    public void testRemoveRemovesCredentialsCorrectlyWhenKeyDoesExist() throws UserDeletionException, ElementNotFoundException {
        AccountCredentials accountCredentials =
                new AccountCredentials("app2", "user2", "somePassword");
        AccountCredentialsKey key =
                new AccountCredentialsKey(accountCredentials.getApplicationName(), accountCredentials.getUsername());
        accountCredentialsRepository.remove(key, stringWriter);

        assertFalse(accountCredentialsRepository.contains(key),
                "Account credentials after removing credentials should not contain them.");

        String expectedAccountCredentialsString =
                String.join(System.lineSeparator(),
                        ACCOUNT_CREDENTIALS.values().stream()
                                .filter(credentials -> !credentials.getUsername().equals("user2"))
                                .map(credentials -> credentials.toString())
                                .toList()) + System.lineSeparator();

        assertTrue(!stringWriter.toString().contains(accountCredentials.toString()) &&
                        stringWriter.toString().length() == expectedAccountCredentialsString.length(),
                "remove has not written correct data on writer.");
    }

    @Test
    public void testUserRepositoryConstructorWithReaderThrowsIllegalArgumentExceptionIfAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new AccountCredentialsRepository(null),
                "Constructor should throw an IllegalArgumentException when reader is null");
    }

    @Test
    public void testUserRepositoryConstructorWithPathThrowsIllegalArgumentExceptionIfAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new AccountCredentialsRepository("Username", null, null),
                "Constructor should throw an IllegalArgumentException when directory and extension is null");
    }

    private <K, V> boolean mapsAreEqual(Map<K, V> map1, Map<K, V> map2) {
        for (Map.Entry<K, V> map1Entry : map1.entrySet()) {
            if (!map2.containsKey(map1Entry.getKey())) {
                return false;
            } else if (!map2.get(map1Entry.getKey()).equals(map1Entry.getValue())) {
                return false;
            }
        }

        return map1.size() == map2.size();
    }

}