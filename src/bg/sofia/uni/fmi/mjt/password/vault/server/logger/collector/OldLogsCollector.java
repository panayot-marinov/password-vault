package bg.sofia.uni.fmi.mjt.password.vault.server.logger.collector;

import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OldLogsCollector implements Runnable {

    private final Path logsDirectory;
    private final LocalDateTime deleteBefore;
    private final Logger logger;

    public OldLogsCollector(Path logsDirectory, int deleteBeforeDays, Logger logger) {
        this.logsDirectory = logsDirectory;
        this.deleteBefore = LocalDateTime.now().minusDays(deleteBeforeDays);
        this.logger = logger;
    }

    public OldLogsCollector(Path logsDirectory, LocalDateTime deleteBefore, Logger logger) {
        this.logsDirectory = logsDirectory;
        this.deleteBefore = deleteBefore;
        this.logger = logger;
    }

    @Override
    public void run() {
        String logMessage = "Deleting old log files.";
        logger.log(Level.INFO, LocalDateTime.now(), logMessage);
        System.out.println(logMessage);

        synchronized (logsDirectory) {
            try {
                Files.list(logsDirectory)
                        .filter(path -> isOldEnough(path))
                        .forEach(path -> delete(path));
            } catch (IOException e) {
                logMessage = "Cannot read log files from the directory. Stacktrace: " + e.getStackTrace();
                logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
                System.out.println(logMessage);
            }
        }
    }

    private void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            String logMessage = "Cannot delete path. Stacktrace: " + e.getStackTrace();
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);
        }
    }

    private boolean isOldEnough(Path path) {
        try {
            return LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault()).isBefore(deleteBefore);
        } catch (IOException e) {
            String logMessage = "Cannot get lastModifiedTime of path. Stacktrace: " + e.getStackTrace();
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);
            return false;
        }
    }

}