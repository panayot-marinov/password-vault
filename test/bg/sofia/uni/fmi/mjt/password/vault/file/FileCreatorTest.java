package bg.sofia.uni.fmi.mjt.password.vault.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FileCreatorTest {

    private static final String FILE_NAME = "testFile.txt";

    @Test
    public void testCreateFileIfDoesNotExistThrowsIllegalArgumentExceptionWhenPathIsNull() {
        assertThrows(IllegalArgumentException.class, () -> FileCreator.createFileIfDoesNotExist(null),
                "Method should throw an IllegalArgumentException when path is null.");
    }

    @Test
    public void testCreateFileIfDoesNotExistCreatesFileIfFileIsNotNull() throws IOException {
        assertFalse(Files.exists(Path.of(FILE_NAME)), "File should not exist before creation.");

        FileCreator.createFileIfDoesNotExist(Path.of(FILE_NAME));
        assertTrue(Files.exists(Path.of(FILE_NAME)),"File should not exist after creation.");
    }

    @AfterEach
    public void tearDown() {
        try {
            Files.deleteIfExists(Path.of(FILE_NAME));
        } catch (IOException e) {
            fail("File deletion has thrown an IOException.", e);
        }

    }

}