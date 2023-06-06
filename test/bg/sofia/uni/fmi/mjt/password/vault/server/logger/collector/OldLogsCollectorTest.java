package bg.sofia.uni.fmi.mjt.password.vault.server.logger.collector;

import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OldLogsCollectorTest {

    private static final Path TEST_LOGS_DIRECTORY = Path.of("testLogs");
    private static final String TEST_LOG_FILE_NAME_0 = "test-logs-0.txt";
    private static final String TEST_LOG_FILE_NAME_1 = "test-logs-1.txt";
    private static final String TEST_LOG_FILE_NAME_2 = "test-logs-2.txt";

    @Mock
    private static Logger loggerMock = mock(Logger.class);

    @BeforeAll
    public static void setUpAll() {
        createFolderIfDoesNotExist(TEST_LOGS_DIRECTORY);
        createFileIfDoesNotExist(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_0));
        createFileIfDoesNotExist(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_1));
        createFileIfDoesNotExist(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_2));

        try {
            Files.setLastModifiedTime(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_0),
                    FileTime.from(LocalDateTime.of(2022,1,1,1,1).toInstant(ZoneOffset.UTC)));
            Files.setLastModifiedTime(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_1),
                    FileTime.from(LocalDateTime.of(2022,1,2,1,1).toInstant(ZoneOffset.UTC)));
            Files.setLastModifiedTime(Path.of(TEST_LOGS_DIRECTORY.toString(), TEST_LOG_FILE_NAME_2),
                    FileTime.from(LocalDateTime.of(2022,1,3,1,1).toInstant(ZoneOffset.UTC)));
        } catch (IOException e) {
            fail("Setting last modified time has thrown an IOException.");
        }

        Mockito.reset(loggerMock);
    }

    @AfterAll
    public static void tearDownAll() {
        deleteAllFilesInDirectory(TEST_LOGS_DIRECTORY);
        try {
            Files.deleteIfExists(TEST_LOGS_DIRECTORY);
        } catch (IOException e) {
            fail("Deleting testLogs directory has thrown an IOException.", e);
        }
    }

    @Test
    public void testRunDeletesOldLogsCorrectly() throws IOException {
        assertEquals(3, Files.list(TEST_LOGS_DIRECTORY).count(), "Log files before deletion should be 3.");

        OldLogsCollector oldLogsCollector = new OldLogsCollector(TEST_LOGS_DIRECTORY,
                LocalDateTime.of(2022, 1,3, 1, 0 ), loggerMock);
        oldLogsCollector.run();

        assertEquals(1, Files.list(TEST_LOGS_DIRECTORY).count(), "Log files after deletion should be 1.");
        verify(loggerMock, atLeastOnce()).log(Mockito.any(Level.class), Mockito.any(LocalDateTime.class), anyString());
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