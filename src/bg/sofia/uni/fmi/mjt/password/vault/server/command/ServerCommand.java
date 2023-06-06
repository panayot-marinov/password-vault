package bg.sofia.uni.fmi.mjt.password.vault.server.command;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidArgumentsCountException;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;

import java.util.Arrays;
import java.util.List;

public record ServerCommand(ServerCommandType type, int argument) {

    public ServerCommand {
        if (type == null) {
            throw new IllegalArgumentException("Type should have a non-null value.");
        }
    }

    public static ServerCommand of(String line) throws InvalidCommandException {
        if (line == null || line.isBlank()) {
            throw new InvalidCommandException("Line cannot be neither null or blank.");
        }

        List<char[]> tokens = Arrays.stream(line.split(" "))
                .map(elem -> elem.toCharArray())
                .toList();

        ServerCommandType commandType = ServerCommandType.UNKNOWN;
        int argument = -1;
        if (tokens.size() > 0) {
            String commandName = String.valueOf(tokens.get(0)).toLowerCase();

            commandType = switch (commandName) {
                case "help" -> ServerCommandType.HELP;
                case "stop" -> ServerCommandType.STOP;
                case "last-logs" -> ServerCommandType.LAST_LOGS;
                default -> ServerCommandType.UNKNOWN;
            };

            if (commandType.argumentsCount != (tokens.size() - 1) && commandType != ServerCommandType.UNKNOWN) {
                throw new InvalidArgumentsCountException("Arguments count is not correct");
            }
            if (commandType == ServerCommandType.LAST_LOGS && tokens.size() > 1) {
                try {
                    argument = Integer.parseInt(String.valueOf(tokens.get(1)));
                } catch (NumberFormatException e) {
                    throw new InvalidCommandException("Argument should be an integer.", e);
                }
            }
        }

        return new ServerCommand(commandType, argument);
    }

}