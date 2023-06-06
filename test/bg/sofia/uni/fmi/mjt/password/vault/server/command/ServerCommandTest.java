package bg.sofia.uni.fmi.mjt.password.vault.server.command;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ServerCommandTest {

    @Test
    public void testOfThrowsInvalidCommandExceptionWhenCommandIsNullOrBlank() {
        assertThrows(InvalidCommandException.class, () -> ServerCommand.of(null),
                "Method does not throw InvalidCommandException when line is null.");
        assertThrows(InvalidCommandException.class, () -> ServerCommand.of(" "),
                "Method does not throw InvalidCommandException when line is blank.");
    }

    @Test
    public void testOfThrowsInvalidCommandExceptionWhenCommandHasQuitCommandHasArguments() {
        String commandLine = "stop 1";
        assertThrows(InvalidCommandException.class, () -> ServerCommand.of(commandLine),
                "Method does not throw InvalidCommandException when line command is '" + commandLine + "'.");
    }

    @Test
    public void testOfThrowsInvalidCommandExceptionWhenCommandIsLastLogsAndArgumentIsNotInteger() {
        String commandLine = "last-logs a";
        assertThrows(InvalidCommandException.class, () -> ServerCommand.of("last-logs a"),
                "Method does not throw InvalidCommandException when line command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeUnknownWhenCommandIsUnknown() {
        String commandLine = "quit abc";
        ServerCommand command = null;
        try {
            command = ServerCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ServerCommandType.UNKNOWN, command.type(),
                "CommandType should be UNKNOWN when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeHelpWhenCommandWhenLineIsAValidHelpCommand() {
        String commandLine = "help";
        ServerCommand command = null;
        try {
            command = ServerCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ServerCommandType.HELP, command.type(),
                "CommandType should be HELP when command is '" + commandLine + "'.");
        assertEquals(-1, command.argument(),
                "CommandArgument argument should be -1 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeStopWhenCommandWhenLineIsAValidStopCommand() {
        String commandLine = "stop";
        ServerCommand command = null;
        try {
            command = ServerCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ServerCommandType.STOP, command.type(),
                "CommandType should be STOP when command is '" + commandLine + "'.");
        assertEquals(-1, command.argument(),
                "CommandArgument argument should be -1 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeLastLogsWhenCommandWhenLineIsAValidLastLogsCommand() {
        String commandLine = "last-logs 10";
        ServerCommand command = null;
        try {
            command = ServerCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ServerCommandType.LAST_LOGS, command.type(),
                "CommandType should be HELP when command is '" + commandLine + "'.");
        assertEquals(10, command.argument(),
                "CommandArgument argument should be 10 when command is '" + commandLine + "'.");
    }

}