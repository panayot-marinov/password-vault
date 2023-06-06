package bg.sofia.uni.fmi.mjt.password.vault.server.repository;

import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.DataFileException;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.User;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserDeletionException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.file.FileCreator;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRepository<T extends User> implements Repository<String, T> {

    private final Map<String, T> users;
    private String usersFilePath;

    public UserRepository(Path usersFilePath, Class<T> clazz) {
        users = readUsersFromPath(usersFilePath, clazz);
        FileCreator.createFileIfDoesNotExist(usersFilePath);
        this.usersFilePath = usersFilePath.toString();
    }

    public UserRepository(Reader usersReader, Class<T> clazz) {
        users = readUsers(usersReader, clazz);
    }

    @Override
    public T get(String username) throws UserNotFoundException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username should have a non-null and non-empty value");
        }

        T user = users.get(username);
        if (user == null) {
            throw new UserNotFoundException("User with such an username does not exist in repository.");
        }

        return user;
    }

    @Override
    public boolean contains(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username should have a non-null and non-empty value");
        }

        return users.containsKey(username);
    }

    @Override
    public void put(T user, Writer writer) throws UsernameAlreadyExistsException {
        if (user == null || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null values");
        }

        if (users.containsKey(user.getUsername())) {
            throw new UsernameAlreadyExistsException("User with such an username already exists.");
        }

        writeUser(user, writer);
        users.put(user.getUsername(), user);
    }

    @Override
    public void update(T user, Writer writer) throws UserNotFoundException {
        if (user == null || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null values");
        }

        if (!users.containsKey(user.getUsername())) {
            writeAllUsers(writer);
            throw new UserNotFoundException(
                    "User with such an username does not exist in repository.");
        }

        users.put(user.getUsername(), user);
        writeAllUsers(writer);
    }

    @Override
    public void remove(String username, Writer writer) throws UserNotFoundException, UserDeletionException {
        if (username == null || username.isBlank() || writer == null) {
            throw new IllegalArgumentException("" +
                    "Username should have a non-null and non-empty value and writer should not be null.");
        }

        User user = users.get(username);
        if (user == null) {
            writeAllUsers(writer);
            throw new UserNotFoundException("User with such an username does not exist");
        }

        users.remove(username);
        writeAllUsers(writer);
        user.delete();
    }

    @Override
    public Map<String, T> getAll() {
        return Map.copyOf(users);
    }

    @Override
    public void deletePathIfExists() throws IOException {
        Files.deleteIfExists(Path.of(usersFilePath));
    }

    @Override
    public void refresh() {
        users.values().stream()
                .forEach(user -> user.refreshCredentials());
    }

    private Map<String, T> readUsersFromPath(Path usersFile, Class<T> clazz) {
        if (usersFile == null || clazz == null) {
            throw new IllegalArgumentException("Nether usersFile nor clazz can be null.");
        }

        try (Reader reader = new FileReader(usersFile.toString())) {
            return readUsers(reader, clazz);
        } catch (FileNotFoundException e) {
            throw new DataFileException("Invalid path to users file.");
        } catch (IOException e) {
            throw new DataFileException("Cannot read users file.");
        }
    }

    private Map<String, T> readUsers(Reader usersReader, Class<T> clazz) {
        if (usersReader == null || clazz == null) {
            throw new IllegalArgumentException("Nether usersFile nor clazz can be null.");
        }

        BufferedReader bufferedReader = new BufferedReader(usersReader);
        Gson gson = new Gson();
        Map<String, T> users = bufferedReader.lines()
                .map(line -> gson.fromJson(line, clazz))
                .collect(Collectors.toMap(elem -> elem.getUsername(), elem -> elem));

        return users;
    }

    private void writeUser(User user, Writer writer) {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), true);
        printWriter.println(user);
    }

    private void writeAllUsers(Writer writer) {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), true);
        for (User user : users.values()) {
            printWriter.println(user);
        }
    }

}