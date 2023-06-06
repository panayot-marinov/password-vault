package bg.sofia.uni.fmi.mjt.password.vault.server.repository;

import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentials;
import bg.sofia.uni.fmi.mjt.password.vault.server.credentials.AccountCredentialsKey;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.DataFileException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.exceptions.CredentialNotFoundException;
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

public class AccountCredentialsRepository implements Repository<AccountCredentialsKey, AccountCredentials> {

    private static final Gson GSON = new Gson();
    private String credentialsFilePath;
    transient private Map<AccountCredentialsKey, AccountCredentials> credentials;

    public AccountCredentialsRepository(String username, String credentialsDirectory, String credentialsExtension) {
        credentialsFilePath =
                generateCredentialsFilePath(username, credentialsDirectory, credentialsExtension).toString();
        FileCreator.createFileIfDoesNotExist(Path.of(credentialsFilePath));
        credentials = readCredentialsFromPath(credentialsFilePath);
    }

    public AccountCredentialsRepository(Reader reader) {
        credentials = readCredentials(reader);
    }

    @Override
    public AccountCredentials get(AccountCredentialsKey key) throws CredentialNotFoundException {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }

        AccountCredentials user = credentials.get(key);
        if (user == null) {
            throw new CredentialNotFoundException(
                    "Credentials with such an AccountCredentialsKey does not exist in repository.");
        }

        return user;
    }

    @Override
    public boolean contains(AccountCredentialsKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Key should have a non-null value.");
        }

        return credentials.containsKey(key);
    }

    @Override
    public void put(AccountCredentials accountCredentials, Writer appendWriter)
            throws CredentialAlreadyExistsException {
        if (accountCredentials == null || appendWriter == null) {
            throw new IllegalArgumentException("All arguments should have non-null values");
        }

        AccountCredentialsKey key = new AccountCredentialsKey(accountCredentials.getApplicationName(),
                accountCredentials.getUsername());
        if (credentials.containsKey(key)) {
            throw new CredentialAlreadyExistsException(
                    "Credentials with such an combination of username and password already exist.");
        }

        writeCredential(accountCredentials, appendWriter);
        credentials.put(key, accountCredentials);
    }

    @Override
    public void update(AccountCredentials accountCredentials, Writer writer) throws CredentialNotFoundException {
        if (accountCredentials == null || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null values");
        }

        AccountCredentialsKey key = new AccountCredentialsKey(accountCredentials.getApplicationName(),
                accountCredentials.getUsername());
        if (!credentials.containsKey(key)) {
            writeAllCredentials(writer);
            throw new CredentialNotFoundException(
                    "Credentials with such an combination of username and password does not exist.");
        }

        credentials.put(key, accountCredentials);
        writeAllCredentials(writer);
    }

    @Override
    public void remove(AccountCredentialsKey key, Writer writer) throws CredentialNotFoundException {
        if (key == null || writer == null) {
            throw new IllegalArgumentException("All arguments should have non-null values");
        }

        if (!credentials.containsKey(key)) {
            writeAllCredentials(writer);
            throw new CredentialNotFoundException(
                    "Credentials with such an combination of username and password does not exist.");
        }

        credentials.remove(key);
        writeAllCredentials(writer);
    }

    @Override
    public Map<AccountCredentialsKey, AccountCredentials> getAll() {
        readCredentialsFromPathIfNotPresent();

        return Map.copyOf(credentials);
    }

    @Override
    public void deletePathIfExists() throws IOException {
        Files.deleteIfExists(Path.of(credentialsFilePath));
    }

    @Override
    public void refresh() {
        readCredentialsFromPathIfNotPresent();
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public static Path generateCredentialsFilePath(String username,
                                                   String credentialsFileDirectory, String credentialsFileExtension) {
        if (username == null || credentialsFileDirectory == null || credentialsFileExtension == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }

        return Path.of(credentialsFileDirectory, username + "Credentials." + credentialsFileExtension);
    }

    private Map<AccountCredentialsKey, AccountCredentials> readCredentialsFromPath(String credentialsFilePath) {
        if (credentialsFilePath == null) {
            throw new IllegalArgumentException("UsersFile cannot be null.");
        }

        try (Reader reader = new FileReader(credentialsFilePath)) {
            return readCredentials(reader);
        } catch (FileNotFoundException e) {
            throw new DataFileException("Invalid path to credentials file.", e);
        } catch (IOException e) {
            throw new DataFileException("Cannot read credentials file.", e);
        }
    }

    private Map<AccountCredentialsKey, AccountCredentials> readCredentials(Reader credentialsReader) {
        if (credentialsReader == null) {
            throw new IllegalArgumentException("UsersReader cannot be null.");
        }

        BufferedReader bufferedReader = new BufferedReader(credentialsReader);
        Gson gson = new Gson();

        credentials = bufferedReader.lines()
                .map(line -> gson.fromJson(line, AccountCredentials.class))
                .collect(Collectors.toMap(elem ->
                                new AccountCredentialsKey(elem.getApplicationName(), elem.getUsername()),
                        elem -> new AccountCredentials(elem.getApplicationName(),
                                elem.getUsername(), elem.getEncryptedPassword())));

        return credentials;
    }

    private void writeCredential(AccountCredentials accountCredentials, Writer writer) {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), true);
        printWriter.println(accountCredentials);
    }

    private void writeAllCredentials(Writer writer) {
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), true);
        for (AccountCredentials elem : credentials.values()) {
            printWriter.println(elem);
        }
    }

    private void readCredentialsFromPathIfNotPresent() {
        if (credentials == null || credentials.size() == 0) {
            FileCreator.createFileIfDoesNotExist(Path.of(credentialsFilePath));
            credentials = readCredentialsFromPath(credentialsFilePath);
        }
    }

}