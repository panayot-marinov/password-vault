package bg.sofia.uni.fmi.mjt.password.vault.server.repository;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.ElementNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.DefaultUser;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.User;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class UserRepositoryTest {

    private static final Path TEST_FOLDER_PATH = Path.of("testData", "users");
    private static final Path TEST_USERS_FILE_PATH = Path.of(TEST_FOLDER_PATH.toString(), "Users.dat");

    private static final int ITERATIONS_COUNT = 1024;
    private static final Map<String, DefaultUser> USERS = Map.of(
            "user1",
            new DefaultUser("user1",
                    new HashedPassword("password1".toCharArray()),
                    new EncryptionData(ITERATIONS_COUNT, "salt1".getBytes(StandardCharsets.UTF_8))),
            "user2",
            new DefaultUser("user2",
                    new HashedPassword("password2".toCharArray()),
                    new EncryptionData(ITERATIONS_COUNT, "salt2".getBytes(StandardCharsets.UTF_8))),
            "user3",
            new DefaultUser("user3",
                    new HashedPassword("password3".toCharArray()),
                    new EncryptionData(ITERATIONS_COUNT, "salt3".getBytes(StandardCharsets.UTF_8)))
    );

    private static final String USERS_MERGED_STRING =
            String.join(System.lineSeparator(),
                    USERS.values().stream()
                            .map(user -> user.toString())
                            .toList());

    private static final DefaultUser TEST_USER_NOT_CONTAINED = new DefaultUser("someUser",
            new HashedPassword("somePass".toCharArray()),
            new EncryptionData(ITERATIONS_COUNT, "someSalt".getBytes(StandardCharsets.UTF_8)));

    private static final DefaultUser TEST_USER_CONTAINED = new DefaultUser("user3",
            new HashedPassword("somePass".toCharArray()),
            new EncryptionData(ITERATIONS_COUNT, "someSalt".getBytes(StandardCharsets.UTF_8)));

    private StringReader stringReader;
    private StringWriter stringWriter;
    private Repository<String, DefaultUser> userRepository;
    private Repository<String, DefaultUser> userRepositoryWithFile;

    @BeforeAll
    public static void setUpAll() {
        createFolderIfDoesNotExist(TEST_FOLDER_PATH);
        createFileIfDoesNotExist(TEST_USERS_FILE_PATH);
    }

    @BeforeEach
    public void setUp() {
        stringReader = new StringReader(USERS_MERGED_STRING);
        stringWriter = new StringWriter();
        userRepository = new UserRepository<>(stringReader, DefaultUser.class);
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

    @AfterAll
    public static void tearDownAll() {
        try {
            Files.deleteIfExists(TEST_USERS_FILE_PATH);
            Files.deleteIfExists(TEST_FOLDER_PATH);
            Files.deleteIfExists(Path.of("testData"));
        } catch (IOException e) {
            fail("Deleting files and folders has thrown an IOException.");
        }
    }

    @Test
    public void testGetAllReturnsAllUsers() {
        Map<String, DefaultUser> users = userRepository.getAll();
        assertTrue(mapsAreEqual(USERS, users),
                "returned map is not equal to the input map.");
    }

    @Test
    public void testGetThrowsIllegalArgumentExceptionWhenKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userRepository.get(null),
                "Method should throw an IllegalArgumentException when key is null.");
    }

    @Test
    public void testGetThrowsUserNotFoundExceptionWhenKeyIsNotFound() {
        assertThrows(UserNotFoundException.class,
                () -> userRepository.get("user4"),
                "Method should throw an UserNotFoundException when username is not found.");
    }

    @Test
    public void testGetReturnsCorrectUserWhenKeyExists() {
        String username = "user1";
        User expectedUser = USERS.get(username);
        User actualUser = null;
        try {
            actualUser = userRepository.get(username);
        } catch (ElementNotFoundException e) {
            fail("Method has thrown an ElementNotFoundException");
        }

        assertEquals(expectedUser, actualUser,
                "Method does not return correct user.");
    }

    @Test
    public void testContainsThrowsIllegalArgumentExceptionWhenKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userRepository.contains(null),
                "Method should throw an IllegalArgumentException when key is null.");
    }

    @Test
    public void testContainsReturnsFalseWhenKeyIsNotFound() {
        assertFalse(userRepository.contains("user6"),
                "Method should return false when username is not found.");
    }

    @Test
    public void testContainsReturnsTrueWhenKeyExists() {
        String username = "user2";
        assertTrue(userRepository.contains(username),
                "Method should return false when key exists.");
    }

    @Test
    public void testPutThrowsIllegalArgumentExceptionAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userRepository.put(null, stringWriter),
                "Method should throw an IllegalArgumentException when value is null.");
        assertThrows(IllegalArgumentException.class, () -> userRepository.put(TEST_USER_NOT_CONTAINED, null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testPutThrowsUsernameAlreadyExistsExceptionWhenKeyExists() {
        assertThrows(UsernameAlreadyExistsException.class,
                () -> userRepository.put(TEST_USER_CONTAINED, stringWriter),
                "Method should throw an UsernameAlreadyExistsException when key  exists.");
        assertEquals(stringWriter.toString(), "",
                "put should not have written any data on writer when data is not updated successfully.");
    }

    @Test
    public void testPutSavesUserCorrectlyWhenKeyDoesNotExist() {
        try {
            userRepository.put(TEST_USER_NOT_CONTAINED, stringWriter);
        } catch (ElementAlreadyExistsException e) {
            fail("Method has thrown an ElementAlreadyExistsException.");
        }

        assertTrue(userRepository.contains(TEST_USER_NOT_CONTAINED.getUsername()),
                "Users repository should contain the newly added user after put operation.");
        assertEquals(stringWriter.toString(), TEST_USER_NOT_CONTAINED + System.lineSeparator(),
                "put has not written correct data on writer.");
    }

    @Test
    public void testUpdateThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userRepository.update(null, stringWriter),
                "Method should throw an IllegalArgumentException when value is null.");
        assertThrows(IllegalArgumentException.class, () -> userRepository.update(TEST_USER_CONTAINED, null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testUpdateThrowsUserNotFoundExceptionWhenKeyIsNotFound() {
        assertThrows(UserNotFoundException.class,
                () -> userRepository.update(TEST_USER_NOT_CONTAINED, stringWriter),
                "Method should throw an UserNotFoundException when key is not found.");
        assertEquals(stringWriter.toString().length(), USERS_MERGED_STRING.length() + System.lineSeparator().length(),
                "update should not have written any new data on writer when data is not updated successfully.");
    }

    @Test
    public void testUpdateUpdatesUserCorrectlyWhenKeyDoesExist() {
        try {
            userRepository.update(TEST_USER_CONTAINED, stringWriter);
        } catch (ElementNotFoundException e) {
            fail("Method has thrown an ElementNotFoundException.");
        }

        try {
            assertEquals(TEST_USER_CONTAINED, userRepository.get(TEST_USER_CONTAINED.getUsername()),
                    "The updated user value is not correct.");
        } catch (ElementNotFoundException e) {
            fail("Method thrown an ElementNotFoundException.");
        }

        String expectedUsersString =
                String.join(System.lineSeparator(),
                        USERS.values().stream()
                                .filter(user -> !user.getUsername().equals(TEST_USER_CONTAINED.getUsername()))
                                .map(user -> user.toString())
                                .toList()) + System.lineSeparator() +
                        TEST_USER_CONTAINED + System.lineSeparator();

        assertTrue(stringWriter.toString().contains(TEST_USER_CONTAINED.toString()) &&
                        stringWriter.toString().length() == expectedUsersString.length(),
                "update has not written correct data on writer.");
    }

    @Test
    public void testRemoveThrowsIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userRepository.remove(null, stringWriter),
                "Method should throw an IllegalArgumentException when key is null.");
        assertThrows(IllegalArgumentException.class,
                () -> userRepository.remove(TEST_USER_CONTAINED.getUsername(), null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testRemoveThrowsUserNotFoundExceptionWhenKeyIsNotFound() {
        assertThrows(UserNotFoundException.class,
                () -> userRepository.remove(TEST_USER_NOT_CONTAINED.getUsername(), stringWriter),
                "Method should throw an CredentialsNotFoundException when key is not found.");
        assertEquals(stringWriter.toString().length(), USERS_MERGED_STRING.length() + System.lineSeparator().length(),
                "remove should not have written any new data on writer when data is not removed successfully.");
    }

    @Test
    public void testRemoveRemovesUserCorrectlyWhenKeyDoesExists() throws UserDeletionException, ElementNotFoundException {
        userRepository.remove(TEST_USER_CONTAINED.getUsername(), stringWriter);

        assertFalse(userRepository.contains(TEST_USER_CONTAINED.getUsername()),
                "After removing user users repository should not contain the user.");

        String expectedUsersString =
                String.join(System.lineSeparator(),
                        USERS.values().stream()
                                .filter(user -> !user.getUsername().equals(TEST_USER_CONTAINED.getUsername()))
                                .map(user -> user.toString())
                                .toList()) + System.lineSeparator();

        assertTrue(!stringWriter.toString().contains(TEST_USER_CONTAINED.toString()) &&
                        stringWriter.toString().length() == expectedUsersString.length(),
                "remove has not written correct data on writer.");
    }

    @Test
    public void testUserRepositoryConstructorWithReaderThrowsIllegalArgumentExceptionIfAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new UserRepository<DefaultUser>(stringReader, null),
                "Constructor should throw an IllegalArgumentException when clazz is null");
    }

    @Test
    public void testUserRepositoryConstructorWithPathThrowsIllegalArgumentExceptionIfAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new UserRepository<DefaultUser>(TEST_USERS_FILE_PATH, null),
                "Constructor should throw an IllegalArgumentException when clazz is null");
    }

    @Test
    public void testDeletePathIfExistsDeletesUsersFile() throws IOException {
        assertTrue(Files.exists(TEST_USERS_FILE_PATH));
        userRepositoryWithFile = new UserRepository<>(TEST_USERS_FILE_PATH, DefaultUser.class);
        userRepositoryWithFile.deletePathIfExists();
        assertFalse(Files.exists(TEST_USERS_FILE_PATH));
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

}
