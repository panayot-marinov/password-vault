package bg.sofia.uni.fmi.mjt.password.vault.server.logger;

import bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions.LogParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class DefaultLogParser implements LogParser {

    private static final int LOG_LEVEL_INDEX = 0;
    private static final int LOG_DATETIME_INDEX = 1;
    private static final int LOG_CLASSNAME_INDEX = 2;
    private static final int LOG_MESSAGE_INDEX = 3;
    private static final int NUMBER_OF_ARGUMENTS_IN_LOG = 4;

    private final Path directory;

    public DefaultLogParser(Path directory) {
        this.directory = directory;
    }

    @Override
    public List<Log> getLogs(Level level, LocalDateTime from, LocalDateTime to)
            throws LogParserException {
        if (level == null || from == null || to == null) {
            throw new IllegalArgumentException("All arguments should have non-null values.");
        }

        List<Path> paths = null;
        synchronized (directory) {
            paths = readPathsFromDirectory(
                    (file1, file2) -> Long.valueOf(file1.lastModified()).compareTo(file2.lastModified()));
        }
        List<Log> resultLogs = new ArrayList<>();

        for (Path path : paths) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toString()))) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] tokens = line.split("\\|", NUMBER_OF_ARGUMENTS_IN_LOG);
                    String levelName = tokens[0].substring(1, tokens[LOG_LEVEL_INDEX].length() - 1);
                    String className = tokens[LOG_CLASSNAME_INDEX];
                    LocalDateTime localDateTime =
                            LocalDateTime.parse(tokens[LOG_DATETIME_INDEX], DateTimeFormatter.ISO_DATE_TIME);

                    if (levelName.equals(level.toString()) &&
                            (from.isBefore(localDateTime) || from.equals(localDateTime)) &&
                            (to.isAfter(localDateTime) || to.equals(localDateTime))) {
                        Log log =
                                new Log(Level.valueOf(levelName),
                                        LocalDateTime.parse(tokens[LOG_DATETIME_INDEX]),
                                        className, tokens[LOG_MESSAGE_INDEX]);
                        resultLogs.add(log);
                    }
                    if (to.isBefore(localDateTime)) {
                        return resultLogs;
                    }
                }
            } catch (IOException e) {
                throw new LogParserException("Cannot read log files.", e);
            }
        }

        return resultLogs;
    }

    @Override
    public List<Log> getLogs(LocalDateTime from, LocalDateTime to) throws LogParserException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Neither from nor to could be null.");
        }

        List<Path> paths = null;
        synchronized (directory) {
            paths = readPathsFromDirectory(
                    (file1, file2) -> Long.valueOf(file1.lastModified()).compareTo(file2.lastModified()));
        }
        List<Log> resultLogs = new ArrayList<>();

        for (Path path : paths) {
            try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split("\\|", NUMBER_OF_ARGUMENTS_IN_LOG);
                    String levelName = tokens[LOG_LEVEL_INDEX].substring(1, tokens[LOG_LEVEL_INDEX].length() - 1);
                    String className = tokens[LOG_CLASSNAME_INDEX];
                    LocalDateTime localDateTime =
                            LocalDateTime.parse(tokens[LOG_DATETIME_INDEX], DateTimeFormatter.ISO_DATE_TIME);

                    if ((from.isBefore(localDateTime) || from.equals(localDateTime)) &&
                            (to.isAfter(localDateTime) || to.equals(localDateTime))) {
                        Log log = new Log(
                                Level.valueOf(levelName), localDateTime, className, tokens[LOG_MESSAGE_INDEX]);
                        resultLogs.add(log);
                    }
                    if (to.isBefore(localDateTime)) {
                        return resultLogs;
                    }
                }
            } catch (IOException e) {
                throw new LogParserException("Cannot read log files.", e);
            }
        }

        return resultLogs;
    }

    @Override
    public List<Log> getLogsTail(int n) throws LogParserException {
        if (n < 0) {
            throw new IllegalArgumentException("n should be a non-negative number.");
        }

        List<Path> paths = null;
        synchronized (directory) {
            paths = readPathsFromDirectory(
                    (file1, file2) -> Long.valueOf(file2.lastModified()).compareTo(file1.lastModified()));
        }
        List<Log> lastNlogs = new ArrayList<>();
        List<Log> allLogsFromCurrentFile = null;

        for (Path path : paths) {
            allLogsFromCurrentFile = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split("\\|", NUMBER_OF_ARGUMENTS_IN_LOG);
                    String levelName = tokens[LOG_LEVEL_INDEX].substring(1, tokens[LOG_LEVEL_INDEX].length() - 1);
                    String className = tokens[LOG_CLASSNAME_INDEX];
                    LocalDateTime localDateTime = LocalDateTime.parse(tokens[1], DateTimeFormatter.ISO_DATE_TIME);

                    Log log = new Log(Level.valueOf(levelName), localDateTime, className, tokens[LOG_MESSAGE_INDEX]);
                    allLogsFromCurrentFile.add(log);
                }
            } catch (IOException e) {
                throw new LogParserException("Cannot read log files.", e);
            }

            Collections.reverse(allLogsFromCurrentFile);
            //Merge logs
            if (lastNlogs.size() + allLogsFromCurrentFile.size() >= n) {
                lastNlogs = Stream.concat(
                                lastNlogs.stream(), allLogsFromCurrentFile.stream().limit(n - lastNlogs.size()))
                        .toList();
                break;
            } else {
                lastNlogs = Stream.concat(lastNlogs.stream(), allLogsFromCurrentFile.stream())
                        .toList();
            }
        }

        return lastNlogs;
    }

    private List<Path> readPathsFromDirectory(Comparator<File> comparator) throws LogParserException {
        List<Path> paths = null;
        try {
            paths = Files.list(directory)
                    .map(path -> path.toFile())
                    .sorted(comparator)
                    .map(file -> file.toPath())
                    .toList();
        } catch (IOException e) {
            throw new LogParserException("Cannot read log files from the directory.", e);
        }

        return paths;
    }

}