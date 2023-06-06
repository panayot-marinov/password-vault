package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import bg.sofia.uni.fmi.mjt.password.vault.server.logger.collector.OldLogsCollector;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions.LogException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultLogger implements Logger {

    private static final long INITIAL_COLLECTOR_DELAY = 1;
    private static final long COLLECTOR_DELAY = 12 * 60;

    //required
    private final Class<?> clazz;
    private final Path directory;

    //optional
    private final String filePrefix;
    private final String fileExtension;
    private final long maxFileSizeBytes;
    private final Level minLogLevel;
    private final boolean shouldThrowErrors;
    private final int deleteLogsOlderThanDays;

    private int currentFileNumber;
    private final ScheduledExecutorService scheduledExecutorService;

    @Override
    public void log(Level level, LocalDateTime timestamp, String message) {
        if (level == null || timestamp == null || message == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }
        if (minLogLevel.getLevel() > level.getLevel()) {
            return;
        }

        Log log = new Log(level, timestamp, clazz.getPackageName().toString(), message);
        String logMessage = log + System.lineSeparator();

        try {
            while (Files.exists(getCurrentFilePath()) &&
                    Files.size(getCurrentFilePath()) + logMessage.length() > maxFileSizeBytes) {
                ++currentFileNumber;
            }
        } catch (IOException e) {
            if (shouldThrowErrors) {
                throw new LogException("Cannot get file size.", e);
            }
        }

        String path = getCurrentFilePath().toString();
        synchronized (directory) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path, true))) {
                bufferedWriter.append(logMessage);
                bufferedWriter.flush();
            } catch (IOException e) {
                if (shouldThrowErrors) {
                    throw new LogException("Log operation cannot be finished.", e);
                }
            }
        }
    }

    @Override
    public Path getCurrentFilePath() {
        return Path.of(directory.toString(), filePrefix + "-" + currentFileNumber + "." + fileExtension);
    }

    public static DefaultLoggerBuilder builder() {
        return new DefaultLoggerBuilder();
    }

    private DefaultLogger(DefaultLoggerBuilder builder) {
        this.clazz = builder.clazz;
        this.directory = builder.directory;
        this.filePrefix = builder.filePrefix;
        this.fileExtension = builder.fileExtension;
        this.maxFileSizeBytes = builder.maxFileSizeBytes;
        this.minLogLevel = builder.minLogLevel;
        this.shouldThrowErrors = builder.shouldThrowErrors;
        this.deleteLogsOlderThanDays = builder.deleteLogsOlderThanDays;
        this.currentFileNumber = getLastLogFileNumber(directory);
        this.scheduledExecutorService = builder.scheduledExecutorService;
        if (scheduledExecutorService != null) {
            scheduleOldLogsCollector();
        }
    }

    private void scheduleOldLogsCollector() {
        this.scheduledExecutorService.scheduleAtFixedRate(
                new OldLogsCollector(directory, deleteLogsOlderThanDays, this),
                INITIAL_COLLECTOR_DELAY, COLLECTOR_DELAY, TimeUnit.MINUTES);
    }

    private int getLastLogFileNumber(Path directory) {
        try {
            Path lastLogFilePath = Files.list(directory)
                    .map(path -> path.toFile())
                    .sorted((file1, file2) -> Long.valueOf(file2.lastModified()).compareTo(file1.lastModified()))
                    .map(file -> file.toPath())
                    .findFirst()
                    .orElse(Path.of("log-0.txt"));

            int lastLogFileNumber = Integer.parseInt(lastLogFilePath.toString()
                    .split("-")[1]
                    .split("\\.")[0]);
            return lastLogFileNumber;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read files from log directory.", e);
        }
    }

    public static class DefaultLoggerBuilder {

        private static final String DEFAULT_FILE_PREFIX = "log";
        private static final String DEFAULT_FILE_EXTENSION = "txt";
        private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 4096;
        private static final Level DEFAULT_MIN_LOG_LEVEL = Level.INFO;
        private static final boolean DEFAULT_SHOULD_THROW_ERRORS = false;
        private static final int DEFAULT_DELETE_LOGS_OLDER_THAN_DAYS = 15;

        //required
        private Class<?> clazz;
        private Path directory;

        //optional
        private String filePrefix = DEFAULT_FILE_PREFIX;
        private String fileExtension = DEFAULT_FILE_EXTENSION;
        private long maxFileSizeBytes = DEFAULT_MAX_FILE_SIZE_BYTES;
        private Level minLogLevel = DEFAULT_MIN_LOG_LEVEL;
        private boolean shouldThrowErrors = DEFAULT_SHOULD_THROW_ERRORS;
        private int deleteLogsOlderThanDays = DEFAULT_DELETE_LOGS_OLDER_THAN_DAYS;
        private ScheduledExecutorService scheduledExecutorService;

        public DefaultLoggerBuilder setClazz(Class<?> clazz) {
            if (clazz == null) {
                throw new IllegalArgumentException("Class argument cannot be null.");
            }

            this.clazz = clazz;
            return this;
        }

        public DefaultLoggerBuilder setDirectory(Path directory) {
            if (directory == null) {
                throw new IllegalArgumentException("Directory argument cannot be null.");
            }

            this.directory = directory;
            return this;
        }

        public DefaultLoggerBuilder setFilePrefix(String filePrefix) {
            if (filePrefix == null || filePrefix.isBlank() || filePrefix.contains("-")) {
                throw new IllegalArgumentException(
                        "filePrefix cannot be neither null or blank and cannot contain the symbol '-'.");
            }

            this.filePrefix = filePrefix;
            return this;
        }

        public DefaultLoggerBuilder setFileExtension(String fileExtension) {
            if (fileExtension == null || fileExtension.isBlank()) {
                throw new IllegalArgumentException("fileExtension cannot be neither null or blank.");
            }

            this.fileExtension = fileExtension;
            return this;
        }

        public DefaultLoggerBuilder setMaxFileSizeBytes(long maxFileSizeBytes) {
            if (maxFileSizeBytes == 0) {
                throw new IllegalArgumentException("maxFileSizeBytes cannot be zero.");
            }

            this.maxFileSizeBytes = maxFileSizeBytes;
            return this;
        }

        public DefaultLoggerBuilder setMinLogLevel(Level minLogLevel) {
            if (minLogLevel == null) {
                throw new IllegalArgumentException("minLogLevel cannot be null.");
            }
            this.minLogLevel = minLogLevel;
            return this;
        }

        public DefaultLoggerBuilder setShouldThrowErrors(boolean shouldThrowErrors) {
            this.shouldThrowErrors = shouldThrowErrors;
            return this;
        }

        public DefaultLoggerBuilder setDeleteLogsOlderThanDays(int days) {
            if (days <= 0) {
                throw new IllegalArgumentException("Arguments should have a positive value.");
            }

            this.deleteLogsOlderThanDays = days;
            return this;
        }

        public DefaultLoggerBuilder setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
            if (scheduledExecutorService == null) {
                throw new IllegalArgumentException("Argument should not have a null value.");
            }

            this.scheduledExecutorService = scheduledExecutorService;
            return this;
        }

        public DefaultLogger build() {
            if (clazz == null || directory == null) {
                throw new IllegalStateException("Clazz, directory cannot be null.");
            }

            return new DefaultLogger(this);
        }

    }

}