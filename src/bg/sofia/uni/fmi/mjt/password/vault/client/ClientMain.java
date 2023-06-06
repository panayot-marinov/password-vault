package bg.sofia.uni.fmi.mjt.password.vault.client;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidArgumentsCountException;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.Pbkdf2KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.DefaultPasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.WordlistPasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.PasswordVaultClient;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UnsupportedCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.ConfigurationData;
import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.ConfigurationDataException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.ConnectionException;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestCreator;
import bg.sofia.uni.fmi.mjt.password.vault.request.DefaultNioRequestCreator;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserAlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.NioPasswordVaultClient;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMain {

    private static final Path CONFIG_FILE_DIRECTORY = Path.of("config");
    private static final Path CONFIG_FILE_PATH = Path.of(CONFIG_FILE_DIRECTORY.toString(), "clientConfig.dat");

    public static void main(String[] args) {
        ConfigurationData data = null;
        try {
            data = ConfigurationData.of(CONFIG_FILE_PATH);
        } catch (ConfigurationDataException e) {
            System.out.println("Configuration file cannot be read. A new one will be created.");
            data = createConfiguration();
        }

        PasswordVaultClient client = null;
        ExecutorService clientExecutorService = Executors.newSingleThreadExecutor(
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });

        client = connectClient(data, client, clientExecutorService);
        runClient(client, clientExecutorService);
    }

    private static void runClient(PasswordVaultClient client, ExecutorService clientExecutorService) {
        try (Scanner scanner = new Scanner(System.in);
             Reader passwordGeneratorWordlistReader =
                     new FileReader("wordlist/PasswordGeneratorWordlist.dat")) {
            KeyGenerator keyGenerator = Pbkdf2KeyGenerator.builder().build();

            PasswordGenerator defualtPasswordGenerator = DefaultPasswordGenerator.builder()
                    .build();
            PasswordGenerator wordlistPasswordGenerator = WordlistPasswordGenerator.builder()
                    .setWordlistReader(passwordGeneratorWordlistReader)
                    .build();
            RequestCreator requestCreator = new DefaultNioRequestCreator(keyGenerator,
                    defualtPasswordGenerator, wordlistPasswordGenerator);

            executeCommands(client, scanner, requestCreator, clientExecutorService);
        } catch (IOException e) {
            throw new UncheckedIOException("An error occured while running client application.", e);
        } catch (ConnectionException e) {
            throw new RuntimeException("There is a connection problem with the server.", e);
        }
    }

    private static PasswordVaultClient connectClient(
            ConfigurationData data, PasswordVaultClient client, ExecutorService clientExecutorService) {
        while (client == null || !client.isConnected()) {
            try {
                client = new NioPasswordVaultClient(data, clientExecutorService);
                client.connect();
            } catch (ConnectionException e) {
                System.out.println("Cannot connect to the server.");
                System.out.println("Please enter new configuration in format: [HOST] [PORT]");
                data = ConfigurationData.of(System.in);
            }
        }
        return client;
    }

    private static void executeCommands(PasswordVaultClient client, Scanner scanner,
                                        RequestCreator requestCreator, ExecutorService executorService)
            throws ConnectionException {
        Session session = null;
        ClientCommand command = null;
        do {
            System.out.println("Enter command:");
            String commandString = scanner.nextLine();
            client.updateLogoutTime();
            try {
                command = ClientCommand.of(commandString);
            } catch (InvalidArgumentsCountException e) {
                System.out.println("Invalid arguments count");
                continue;
            } catch (InvalidCommandException e) {
                System.out.println("Invalid command");
                continue;
            }
            ClientCommandType commandType = command.type();

            switch (commandType) {
                case HELP: {
                    printHelp();
                    break;
                }
                case CONNECT: {
                    client.connect();
                    break;
                }
                case DISCONNECT: {
                    client.disconnect();
                    break;
                }
                case QUIT: {
                    client.disconnect();
                    executorService.shutdown();
                    System.out.println("Quitting the application.");
                    break;
                }
                case UNKNOWN: {
                    System.out.println("Unknown command");
                    break;
                }
                default: {
                    NioRequest request = null;
                    try {
                        request = requestCreator.create(command, session);
                    } catch (UserAlreadyLoggedInException e) {
                        System.out.println("User should logout first before logging in again.");
                        continue;
                    } catch (UserNotLoggedInException e) {
                        System.out.println(e.getMessage());
                        continue;
                    } catch (UnsupportedCommandException e) {
                        throw new RuntimeException(
                                "Cannot create request with command type " + command.type(), e);
                    }

                    session = client.send(request, command);
                    break;
                }
            }
        }
        while (command == null || !command.type().equals(ClientCommandType.QUIT));
    }

    private static void printHelp() {
        System.out.println("----------------------------------Help----------------------------------");
        for (ClientCommandType commandType : ClientCommandType.values()) {
            if (commandType.helpMessage.equals("")) {
                continue;
            }
            System.out.println(" * " + commandType.helpMessage);
        }
        System.out.println("------------------------------------------------------------------------");
    }

    private static ConfigurationData createConfiguration() {
        System.out.println("Please enter new configuration in format: [HOST] [PORT]");
        ConfigurationData data = ConfigurationData.of(System.in);

        try {
            if (Files.notExists(CONFIG_FILE_DIRECTORY)) {
                Files.createDirectory(CONFIG_FILE_DIRECTORY);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create directory for configuration file.", e);
        }
        try (Writer fileWriter = new FileWriter(CONFIG_FILE_PATH.toString(), true)) {
            data.writeConfiguration(fileWriter);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write configuration to file.", e);
        }

        return data;
    }


}