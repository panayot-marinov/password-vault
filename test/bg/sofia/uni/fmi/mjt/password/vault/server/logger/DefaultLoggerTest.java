package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import bg.sofia.uni.fmi.mjt.password.vault.configuration.ConfigurationData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.deleteIfExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DefaultLoggerTest {

    private static final Path TEST_LOGS_DIRECTORY = Path.of("testLogs");
    private static final String TEST_LOG_FILES_PREFIX = "log";
    private static final String TEST_LOG_FILES_EXTENSION = "txt";
    private static final String TEST_LOG_FILE_NAME_0 = TEST_LOG_FILES_PREFIX + "-0." + TEST_LOG_FILES_EXTENSION;
    private static final String TEST_LOG_FILE_NAME_1 = TEST_LOG_FILES_PREFIX + "-1." + TEST_LOG_FILES_EXTENSION;
    private static final String TEST_LOG_FILE_NAME_2 = TEST_LOG_FILES_PREFIX + "-2." + TEST_LOG_FILES_EXTENSION;
    private static final LocalDateTime DATE = LocalDateTime.of(2022,2,2,2,2);
    private static final String PACKAGE_NAME = "bg.sofia.uni.fmi.mjt.password.vault.configuration";
    private static Logger defaultLogger;


    @BeforeAll
    public static void setUpAll() {
        createLogDirectoryIfDoesNotExist();
    }

    @BeforeEach
    public void setUp() {
        defaultLogger = DefaultLogger.builder()
                .setClazz(ConfigurationData.class)
                .setDirectory(TEST_LOGS_DIRECTORY)
                .setMinLogLevel(Level.INFO)
                .setShouldThrowErrors(true)
                .setFilePrefix(TEST_LOG_FILES_PREFIX)
                .setFileExtension(TEST_LOG_FILES_EXTENSION)
                .setMaxFileSizeBytes(50)
                .build();
    }

    @AfterEach
    public void tearDown() {
        deleteLogFiles();
    }

    @AfterAll
    public static void tearDownAll() {
        deleteLogDirectory();
    }

    @Test
    public void testLogThrowsIllegalArgumentExceptionWhenArgumentsAreNull() {
        assertThrows(IllegalArgumentException.class,
                () -> defaultLogger.log(null, LocalDateTime.now(), "someMessage"),
                "Method does not throw IllegalArgument exception when level is null.");
        assertThrows(IllegalArgumentException.class,
                () -> defaultLogger.log(Level.INFO, null, "someMessage"),
                "Method does not throw IllegalArgument exception when LocalDateTime is null.");
        assertThrows(IllegalArgumentException.class,
                () -> defaultLogger.log(Level.INFO, LocalDateTime.now(), null),
                "Method does not throw IllegalArgument exception when message is null");
    }

    @Test
    public void testLogShouldCreateTwoNewFilesWhenDataOverflows() {
        defaultLogger.log(Level.ERROR, DATE, "test");
        defaultLogger.log(Level.INFO, DATE, "test2");
        defaultLogger.log(Level.DEBUG, DATE, "test3");
        defaultLogger.log(Level.WARN, DATE, "test4");

        List<Path> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_LOGS_DIRECTORY)) {
            for (Path file : stream) {
                files.add(file);
            }
        } catch (IOException | DirectoryIteratorException e) {
            fail("Error iterating files in test directory.");
        }

        //assert contains file
        assertEquals(3, files.size(), "There are more than needed files in test directory.");
        assertTrue(files.contains(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_0)), "Test files folder does not contain correct test file.");
        assertTrue(files.contains(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_1)), "Test files folder does not contain correct test file.");
        assertTrue(files.contains(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_2)), "Test files folder does not contain correct test file.");
    }

    @Test
    public void testLogShouldCreateNewFileWhenFileDoesNotExist() {
        defaultLogger.log(Level.ERROR, DATE, "test");

        List<Path> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_LOGS_DIRECTORY)) {
            for (Path file : stream) {
                files.add(file);
            }
        } catch (IOException | DirectoryIteratorException e) {
            fail("Error iterating files in test directory.");
        }

        //assert contains file
        assertEquals(1, files.size(), "There are more than needed files in test directory.");
        assertTrue(files.contains(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_0)));

        //read file
        try (BufferedReader br = new BufferedReader(new FileReader(TEST_LOGS_DIRECTORY + File.separator + TEST_LOG_FILE_NAME_0))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                Log expectedLog = new Log(Level.ERROR, DATE, PACKAGE_NAME, "test");
                Log log = Log.of(line);
                assertEquals(expectedLog, log, "The generated log is equal to the expected log.");
            }
        } catch (IOException e) {
            fail("File read operation cannot be finished.");
        }
    }

    private static void createLogDirectoryIfDoesNotExist() {
        if (Files.notExists(TEST_LOGS_DIRECTORY)) {
            try {
                Files.createDirectories(TEST_LOGS_DIRECTORY);
            } catch (IOException e) {
                fail("Cannot create test log directory.");
            }
        }
    }

    private static void deleteLogDirectory() {
        try {
            deleteIfExists(TEST_LOGS_DIRECTORY);
        } catch (IOException e) {
            fail("Cannot delete test log directory.");
        }
    }

    private void deleteLogFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_LOGS_DIRECTORY)) {
            for (Path file : stream) {
                deleteIfExists(file);
            }
        } catch (IOException | DirectoryIteratorException e) {
            fail("Error iterating files in test directory.");
        }
    }
}