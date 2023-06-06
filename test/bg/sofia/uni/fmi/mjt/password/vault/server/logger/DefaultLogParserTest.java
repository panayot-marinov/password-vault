package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions.LogParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.deleteIfExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class DefaultLogParserTest {

    private static final Path TEST_LOGS_DIRECTORY = Path.of("testLogs");
    private static final String TEST_LOG_FILE_NAME_0 = "test-logs-0.txt";
    private static final String TEST_LOG_FILE_NAME_1 = "test-logs-1.txt";
    private static final String TEST_LOG_FILE_NAME_2 = "test-logs-2.txt";
    private static final String PACKAGE_NAME = "bg.sofia.uni.fmi.mjt.password.vault.configuration";
    private static final LocalDateTime DATE_1 = LocalDateTime.of(2022, 1, 6, 8, 8);
    private static final LocalDateTime DATE_2 = LocalDateTime.of(2022, 1, 6, 15, 15);
    private static final List<Log> LOGS = List.of(
            new Log(Level.INFO, DATE_1, PACKAGE_NAME, "message_0"),
            new Log(Level.ERROR, DATE_1.plusMinutes(1), PACKAGE_NAME, "message_1"),
            new Log(Level.WARN, DATE_1.plusMinutes(2), PACKAGE_NAME, "message_2"),
            new Log(Level.WARN, DATE_2, PACKAGE_NAME, "message_3"),
            new Log(Level.INFO, DATE_2.plusMinutes(1), PACKAGE_NAME, "message_4")
    );
    private static final String TEST_LOG_FILE_CONTENT_0 =
            LOGS.get(0) + System.lineSeparator() + LOGS.get(1);
    private static final String TEST_LOG_FILE_CONTENT_1 =
            LOGS.get(2) + System.lineSeparator() + LOGS.get(3);
    private static final String TEST_LOG_FILE_CONTENT_2 =
            LOGS.get(4).toString();
    private static final LocalDateTime DATE_FROM = LocalDateTime.of(2000, 11, 11, 11, 11);
    private static final LocalDateTime DATE_TO = LocalDateTime.of(2022, 11, 11, 11, 11);

    private static DefaultLogParser defaultLogParser;

    @BeforeAll
    public static void setUpAll() {
        createLogDirectoryIfNotExist();
        createLogFile(TEST_LOG_FILE_NAME_0, TEST_LOG_FILE_CONTENT_0, FileTime.fromMillis(20_000));
        createLogFile(TEST_LOG_FILE_NAME_1, TEST_LOG_FILE_CONTENT_1, FileTime.fromMillis(30_000));
        createLogFile(TEST_LOG_FILE_NAME_2, TEST_LOG_FILE_CONTENT_2, FileTime.fromMillis(40_000));
        defaultLogParser = new DefaultLogParser(TEST_LOGS_DIRECTORY);
    }

    @AfterAll
    public static void tearDownAll() {
        deleteLogFiles();
        deleteLogDirectory();
    }

    @Test
    public void testGetLogsShouldReturnCorrectValuesWhenLevelIsNotNull() {
        List<Log> logs =
                null;
        try {
            logs = defaultLogParser.getLogs(Level.WARN, DATE_1, DATE_2);
        } catch (LogParserException e) {
            fail("Method has thrown a LogParserException.");
        }
        List<Log> logsCopy = new ArrayList<>(LOGS);
        List<Log> expectedLogs =
                logsCopy.stream()
                        .filter(log -> log.level().equals(Level.WARN))
                        .toList();

        assertEquals(expectedLogs.size(), logs.size(), "Logs size() should be 4");

        for (int i = 0; i < expectedLogs.size(); i++) {
            assertEquals(expectedLogs.get(i), logs.get(i), "getLogsTail does not return correct logs.");
        }

    }

    @Test
    void testGetLogsOfThreeArgumentsShouldThrowIllegalArgumentExceptionWhenAtleastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> defaultLogParser.getLogs(null, DATE_FROM, DATE_TO),
                "Method does not throw IllegalArgumentException when level is null");
        assertThrows(IllegalArgumentException.class, () -> defaultLogParser.getLogs(Level.INFO, null, DATE_TO),
                "Method does not throw IllegalArgumentException when date from is null");
        assertThrows(IllegalArgumentException.class, () -> defaultLogParser.getLogs(Level.INFO, DATE_FROM, null),
                "Method does not throw IllegalArgumentException when date to is null");
    }

    @Test
    public void testGetLogsOfTwoArgumentsShouldThrowIllegalArgumentExceptionWhenAtLeastOneArgumentIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> defaultLogParser.getLogs(DATE_TO, null),
                "Method does not throw IllegalArgumentException when to is null");
        assertThrows(IllegalArgumentException.class,
                () -> defaultLogParser.getLogs(null, DATE_TO),
                "Method does not throw IllegalArgumentException when to is null");
    }

    @Test
    public void testGetLogsShouldReturnCorrectValuesWhenFromAndToAreNotNull() {
        List<Log> logs = null;
        try {
            logs = defaultLogParser.getLogs(DATE_1, DATE_1);
        } catch (LogParserException e) {
            fail("Method has thrown a LogParserException.");
        }


        List<Log> logsCopy = new ArrayList<>(LOGS);
        List<Log> expectedLogs =
                logsCopy.stream()
                        .filter(log -> log.timestamp().equals(DATE_1))
                        .toList();

        assertEquals(expectedLogs.size(), logs.size(), "Logs size() should be 4");

        for (int i = 0; i < expectedLogs.size(); i++) {
            assertEquals(expectedLogs.get(i), logs.get(i), "getLogsTail does not return correct logs.");
        }
    }

    @Test
    public void testGetLogsTailShouldThrowIllegalArgumentExceptionWhenNIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> defaultLogParser.getLogsTail(-1),
                "Method does not throw IllegalArgumentException when n is negative");
    }

    @Test
    public void testGetLogsTailShouldReturnCorrectValuesWhenNIsZero() {
        Collection<Log> logs = null;
        try {
            logs = defaultLogParser.getLogsTail(0);
        } catch (LogParserException e) {
            fail("Method has thrown a LogParserException.");
        }

        assertEquals(0, logs.size(),
                "Logs size() should be zero when getLogsTail is called with argument zero.");
    }

    @Test
    public void testGetLogsTailShouldReturnCorrectValuesWhenNIsPositive() {
        List<Log> logs = null;
        try {
            logs = defaultLogParser.getLogsTail(4);
        } catch (LogParserException e) {
            fail("Method has thrown a LogParserException.");
        }

        List<Log> logsCopy = new ArrayList<>(LOGS);
        Collections.reverse(logsCopy);
        List<Log> expectedLogs =
                logsCopy.stream()
                        .limit(4)
                        .toList();
        assertEquals(expectedLogs.size(), logs.size(), "Logs size() should be 4");

        for (int i = 0; i < expectedLogs.size(); i++) {
            assertEquals(expectedLogs.get(i), logs.get(i), "getLogsTail does not return correct logs.");
        }
    }

    private static void createLogDirectoryIfNotExist() {
        Path directoryPath = TEST_LOGS_DIRECTORY;
        if (Files.notExists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                fail("Cannot create directories.");
            }
        }
    }

    private static void deleteLogDirectory() {
        try {
            deleteIfExists(TEST_LOGS_DIRECTORY);
        } catch (IOException e) {
            fail("Cannot delete test log directory");
        }
    }

    private static void deleteLogFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEST_LOGS_DIRECTORY)) {
            for (Path file : stream) {
                deleteIfExists(file);
            }
        } catch (IOException | DirectoryIteratorException e) {
            fail("Error iterating files in test directory.");
        }
    }

    private static void createLogFile(String fileName, String fileContent, FileTime modifyTime) {
        Path filePath = Path.of(TEST_LOGS_DIRECTORY.toString(), fileName);
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(filePath.toString()), true)) {
            printWriter.println(fileContent);
            Files.setLastModifiedTime(filePath, modifyTime);
        } catch (IOException e) {
            fail("Cannot create test log file.");
        }
    }

}