package bg.sofia.uni.fmi.mjt.password.vault.server;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidArgumentsCountException;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.CompromisedApiKeyData;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.ConfigurationData;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.CompromisedApiKeyDataException;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.ConfigurationDataException;
import bg.sofia.uni.fmi.mjt.password.vault.server.command.ServerCommand;
import bg.sofia.uni.fmi.mjt.password.vault.server.command.ServerCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.CompromisedPasswordsClient;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.DefaultCompromisedPasswordsClient;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.DefaultLogParser;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.DefaultLogger;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Level;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.LogParser;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.Logger;
import bg.sofia.uni.fmi.mjt.password.vault.server.logger.exceptions.LogParserException;
import bg.sofia.uni.fmi.mjt.password.vault.server.nio.NioPasswordVaultServer;
import bg.sofia.uni.fmi.mjt.password.vault.server.nio.PasswordVaultServer;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.Repository;
import bg.sofia.uni.fmi.mjt.password.vault.server.repository.UserRepository;
import bg.sofia.uni.fmi.mjt.password.vault.server.user.DefaultUser;
import bg.sofia.uni.fmi.mjt.password.vault.file.FileCreator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerMain {

    private static final Path CONFIG_FILE_DIRECTORY = Path.of("config");
    private static final Path CONFIG_FILE_PATH = Path.of(CONFIG_FILE_DIRECTORY.toString(), "serverConfig.dat");
    private static final Path COMPROMISED_API_KEYS_FILE_PATH =
            Path.of(CONFIG_FILE_DIRECTORY.toString(), "compromisedApiKeys.dat");

    private static final Path USERS_FILE_DIRECTORY =
            Path.of("data" + File.separator + "server" + File.separator + "users" + File.separator);
    private static final Path USERS_FILE_PATH =
            Path.of(USERS_FILE_DIRECTORY.toString(), "Users.dat");

    private static final Path CREDENTIALS_FILE_DIRECTORY =
            Path.of("data" + File.separator + "server" + File.separator + "credentials" + File.separator);
    private static final String CREDENTIALS_FILE_EXTENSION = "dat";

    private static final Path LOG_FILES_DIRECTORY = Path.of("logs");

    public static void main(String[] args) throws IOException {
        createRepositoryDirectoriesIfDoesNotExist();
        createLogDirectoryIfDoesNotExist();

        ScheduledExecutorService loggerExecutorService = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });

        Logger logger = DefaultLogger.builder()
                .setDirectory(LOG_FILES_DIRECTORY)
                .setClazz(ConfigurationData.class)
                .setShouldThrowErrors(true)
                .setMinLogLevel(Level.INFO)
                .setScheduledExecutorService(loggerExecutorService)
                .build();

        ConfigurationData configurationData = initializeConfigurationData(logger);
        CompromisedApiKeyData compromisedApiKeyData = initializeCompromisedApiKeyData(logger);

        LogParser logParser = new DefaultLogParser(LOG_FILES_DIRECTORY);

        Repository<String, DefaultUser> users = new UserRepository<>(USERS_FILE_PATH, DefaultUser.class);
        PasswordVault passwordVault = new DefaultPasswordVault(users, logger, USERS_FILE_PATH,
                CREDENTIALS_FILE_DIRECTORY.toString(), CREDENTIALS_FILE_EXTENSION);

        HttpClient httpClient = HttpClient.newBuilder()
                .build();
        CompromisedPasswordsClient compromisedPasswordsClient = DefaultCompromisedPasswordsClient.builder()
                .setHttpClient(httpClient)
                .setApiKey(compromisedApiKeyData.getApiKey())
                .setSecret(compromisedApiKeyData.getSecret())
                .build();

        runServer(loggerExecutorService, logger,
                configurationData, logParser, passwordVault, compromisedPasswordsClient);
    }

    private static void runServer(ScheduledExecutorService loggerExecutorService, Logger logger,
                                  ConfigurationData configurationData, LogParser logParser,
                                  PasswordVault passwordVault, CompromisedPasswordsClient compromisedPasswordsClient) {
        PasswordVaultServer passwordVaultServer =
                new NioPasswordVaultServer(configurationData,
                        passwordVault, compromisedPasswordsClient, logger);
        Thread serverThread = new Thread(passwordVaultServer);
        serverThread.start();

        executeCommands(loggerExecutorService, logger, logParser, passwordVaultServer);
    }

    private static void executeCommands(ScheduledExecutorService loggerExecutorService, Logger logger,
                                        LogParser logParser, PasswordVaultServer passwordVaultServer) {
        try (Scanner scanner = new Scanner(System.in)) {
            ServerCommand command = null;
            do {
                String commandString = scanner.nextLine();
                try {
                    command = ServerCommand.of(commandString);
                } catch (InvalidArgumentsCountException e) {
                    System.out.println("Invalid arguments count");
                    continue;
                } catch (InvalidCommandException e) {
                    System.out.println("Invalid command");
                    continue;
                }
                ServerCommandType commandType = command.type();

                switch (commandType) {
                    case HELP -> printHelp();
                    case STOP -> stopServer(loggerExecutorService, passwordVaultServer);
                    case LAST_LOGS -> printLastLogs(logger, logParser, command);
                    case UNKNOWN -> System.out.println("Unknown command");
                }
            }
            while (command == null || !command.type().equals(ServerCommandType.STOP));
        }
    }

    private static void stopServer(ScheduledExecutorService loggerExecutorService,
                                   PasswordVaultServer passwordVaultServer) {
        passwordVaultServer.stop();
        loggerExecutorService.shutdown();
        System.out.println("Quitting the application.");
    }

    private static void printLastLogs(Logger logger, LogParser logParser, ServerCommand command) {
        try {
            logParser.getLogsTail(command.argument());
            logParser.getLogsTail(command.argument()).stream()
                    .forEach(System.out::println);
        } catch (LogParserException e) {
            String logMessage = "Log files cannot be parsed. " +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.WARN, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);
        }
    }


    private static void printHelp() {
        System.out.println("----------------------------------Help----------------------------------");
        for (ServerCommandType commandType : ServerCommandType.values()) {
            if (commandType.helpMessage.equals("")) {
                continue;
            }
            System.out.println(" * " + commandType.helpMessage);
        }
        System.out.println("------------------------------------------------------------------------");
    }

    private static void createRepositoryDirectoriesIfDoesNotExist() throws IOException {
        if (Files.notExists(USERS_FILE_DIRECTORY)) {
            Files.createDirectories(USERS_FILE_DIRECTORY);
        }
        if (Files.notExists(CREDENTIALS_FILE_DIRECTORY)) {
            Files.createDirectories(CREDENTIALS_FILE_DIRECTORY);
        }
        FileCreator.createFileIfDoesNotExist(USERS_FILE_PATH);
    }

    private static void createLogDirectoryIfDoesNotExist() throws IOException {
        if (Files.notExists(LOG_FILES_DIRECTORY)) {
            Files.createDirectories(LOG_FILES_DIRECTORY);
        }
    }

    private static CompromisedApiKeyData initializeCompromisedApiKeyData(Logger logger) {
        CompromisedApiKeyData compromisedApiKeyData = null;
        try {
            compromisedApiKeyData = CompromisedApiKeyData.of(COMPROMISED_API_KEYS_FILE_PATH);
        } catch (CompromisedApiKeyDataException e) {
            String logMessage = "Compromised api key data file cannot be read. A new one will be created.";
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);
            compromisedApiKeyData = createCompromisedKeyData(logger);
        }
        return compromisedApiKeyData;
    }

    private static ConfigurationData initializeConfigurationData(Logger logger) {
        ConfigurationData configurationData = null;
        try {
            configurationData = ConfigurationData.of(CONFIG_FILE_PATH);
        } catch (ConfigurationDataException e) {
            String logMessage = "Configuration file cannot be read. A new one will be created." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.INFO, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            configurationData = createConfiguration(logger);
        }
        return configurationData;
    }

    private static ConfigurationData createConfiguration(Logger logger) {
        System.out.println("Please enter new configuration in format: [HOST] [PORT]");
        ConfigurationData data = ConfigurationData.of(System.in);

        try {
            if (Files.notExists(CONFIG_FILE_DIRECTORY)) {
                Files.createDirectory(CONFIG_FILE_DIRECTORY);
            }
        } catch (IOException e) {
            String logMessage = "Cannot create directory for configuration file." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            throw new UncheckedIOException("Cannot create directory for configuration file.", e);
        }
        try (Writer fileWriter = new FileWriter(CONFIG_FILE_PATH.toString(), true)) {
            data.writeConfiguration(fileWriter);
        } catch (IOException e) {
            String logMessage = "Cannot write configuration to file." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            throw new UncheckedIOException("Cannot write configuration to file.", e);
        }

        return data;
    }

    private static CompromisedApiKeyData createCompromisedKeyData(Logger logger) {
        System.out.println("Please enter key data in format: [APIKEY] [SECRET]");
        CompromisedApiKeyData data = CompromisedApiKeyData.of(System.in);

        try {
            if (Files.notExists(CONFIG_FILE_DIRECTORY)) {
                Files.createDirectory(CONFIG_FILE_DIRECTORY);
            }
        } catch (IOException e) {
            String logMessage = "Cannot create directory for key data file." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            throw new UncheckedIOException("Cannot create directory for key data file.", e);
        }
        try (Writer fileWriter = new FileWriter(COMPROMISED_API_KEYS_FILE_PATH.toString(), true)) {
            data.writeConfiguration(fileWriter);
        } catch (IOException e) {
            String logMessage = "Cannot write key data to file." +
                    "Stacktrace: " + Arrays.toString(e.getStackTrace());
            logger.log(Level.ERROR, LocalDateTime.now(), logMessage);
            System.out.println(logMessage);

            throw new UncheckedIOException("Cannot write key data to file.", e);
        }

        return data;
    }

}