package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record Log(Level level, LocalDateTime timestamp, String packageName, String message) {

    private static final int LOG_LEVEL_INDEX = 0;
    private static final int LOG_DATE_INDEX = 1;
    private static final int LOG_CLASSNAME_INDEX = 2;
    private static final int LOG_MESSAGE_INDEX = 3;
    private static final int NUMBER_OF_ARGUMENTS_IN_LOG = 4;

    public static Log of(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Line can be neither null nor blank.");
        }

        String[] tokens = line.split("\\|", NUMBER_OF_ARGUMENTS_IN_LOG);
        Level level = Level.valueOf(tokens[0].substring(1, tokens[LOG_LEVEL_INDEX].length() - 1));
        LocalDateTime localDate = LocalDateTime.parse(tokens[LOG_DATE_INDEX]);
        String className = tokens[LOG_CLASSNAME_INDEX];
        String message = tokens[LOG_MESSAGE_INDEX];

        return new Log(level, localDate, className, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Log log = (Log) o;
        return level == log.level &&
                Objects.equals(timestamp, log.timestamp) &&
                Objects.equals(packageName, log.packageName) &&
                Objects.equals(message, log.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, timestamp, packageName, message);
    }

    @Override
    public String toString() {
        return "[" + level() + "]" + "|" +
                timestamp().format(DateTimeFormatter.ISO_DATE_TIME) + "|" +
                packageName + "|" +
                message();
    }

}