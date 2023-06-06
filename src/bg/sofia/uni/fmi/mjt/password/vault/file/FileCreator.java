package bg.sofia.uni.fmi.mjt.password.vault.file;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCreator {

    public static void createFileIfDoesNotExist(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path should not be null.");
        }

        if (Files.notExists(path)) {
            File file = new File(path.toString());
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot create file.", e);
            }
        }
    }

}