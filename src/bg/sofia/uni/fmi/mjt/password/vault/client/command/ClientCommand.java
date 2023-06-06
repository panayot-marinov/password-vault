package bg.sofia.uni.fmi.mjt.password.vault.client.command;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidArgumentsCountException;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ClientCommand(ClientCommandType type, List<char[]> arguments) {

    public ClientCommand {
        if (type == null || arguments == null) {
            throw new IllegalArgumentException("Both type and arguments should have non-null values.");
        }
    }

    public static ClientCommand of(String line) throws InvalidCommandException {
        if (line == null || line.isBlank()) {
            throw new InvalidCommandException("Line cannot be neither null or blank.");
        }

        List<char[]> tokens = Arrays.stream(line.split(" "))
                .map(String::toCharArray)
                .toList();

        ClientCommandType commandType = ClientCommandType.UNKNOWN;
        List<char[]> arguments = new ArrayList<>();
        if (tokens.size() > 0) {
            String commandName = String.valueOf(tokens.get(0)).toLowerCase();
            arguments = tokens.stream()
                    .skip(1)
                    .toList();

            commandType = switch (commandName) {
                case "help" -> ClientCommandType.HELP;
                case "register" -> ClientCommandType.REGISTER;
                case "delete-account" -> ClientCommandType.DELETE_ACCOUNT;
                case "login" -> ClientCommandType.LOGIN;
                case "change-password" -> ClientCommandType.CHANGE_PASSWORD;
                case "retrieve-credentials" -> ClientCommandType.RETRIEVE_CREDENTIALS;
                case "logout" -> ClientCommandType.LOGOUT;
                case "add-password" -> ClientCommandType.ADD_PASSWORD;
                case "update-password" -> ClientCommandType.UPDATE_PASSWORD;
                case "generate-password" -> ClientCommandType.GENERATE_PASSWORD;
                case "generate-password-words" -> ClientCommandType.GENERATE_PASSWORD_WORDS;
                case "remove-password" -> ClientCommandType.REMOVE_PASSWORD;
                case "disconnect" -> ClientCommandType.DISCONNECT;
                case "connect" -> ClientCommandType.CONNECT;
                case "quit" -> ClientCommandType.QUIT;
                default -> ClientCommandType.UNKNOWN;
            };

            if (commandType.argumentsCount != arguments.size() && commandType != ClientCommandType.UNKNOWN) {
                throw new InvalidArgumentsCountException("Arguments count is not correct");
            }

        }

        return new ClientCommand(commandType, arguments);
    }

}