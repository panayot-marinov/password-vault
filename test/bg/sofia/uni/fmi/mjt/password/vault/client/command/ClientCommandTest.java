package bg.sofia.uni.fmi.mjt.password.vault.client.command;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ClientCommandTest {

    @Test
    public void testOfThrowsInvalidCommandExceptionWhenCommandIsNullOrBlank() {
        assertThrows(InvalidCommandException.class, () -> ClientCommand.of(null),
                "Method does not throw InvalidCommandException when line is null.");
        assertThrows(InvalidCommandException.class, () -> ClientCommand.of(" "),
                "Method does not throw InvalidCommandException when line is blank.");
    }

    @Test
    public void testOfThrowsInvalidArgumentsCountExceptionWhenCommandHasQuitCommandHasArguments() {
        assertThrows(InvalidCommandException.class, () -> ClientCommand.of("quit 1"),
                "Method does not throw InvalidCommandException when line command is 'quit 1'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeUnknownWhenCommandIsUnknown() {
        String commandLine = "quitt abc";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.UNKNOWN, command.type(),
                "CommandType should be UNKNOWN when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeHelpWhenCommandWhenLineIsAValidHelpCommand() {
        String commandLine = "help";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.HELP, command.type(),
                "CommandType should be HELP when command is '" + commandLine + "'.");
        assertEquals(0, command.arguments().size(),
                "CommandArguments should be 0 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeRegisterCommandWhenLineIsAValidRegisterCommand() {
        String commandLine = "register test 123 123";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.REGISTER, command.type(),
                "CommandType should be REGISTER when command is '" + commandLine + "'.");
        assertEquals(3, command.arguments().size(),
                "CommandArguments should be 3 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeLoginCommandWhenLineIsAValidLoginCommand() {
        String commandLine = "login test 123";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.LOGIN, command.type(),
                "CommandType should be LOGIN when command is '" + commandLine + "'.");
        assertEquals(2, command.arguments().size(),
                "CommandArguments should be 2 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeLogoutCommandWhenLineIsAValidLogoutCommand() {
        String commandLine = "logout";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.LOGOUT, command.type(),
                "CommandType should be LOGOUT when command is '" + commandLine + "'.");
        assertEquals(0, command.arguments().size(),
                "CommandArguments should be 0 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeChangePasswordCommandWhenLineIsAValidChangePasswordCommand() {
        String commandLine = "change-password 123 1234 1234";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.CHANGE_PASSWORD, command.type(),
                "CommandType should be CHANGE_PASSWORD when command is '" + commandLine + "'.");
        assertEquals(3, command.arguments().size(),
                "CommandArguments should be 3 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeRetrieveCredentialsWhenLineIsAValidRetrieveCredentialsCommand() {
        String commandLine = "retrieve-credentials exampleApp exampleUser";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.RETRIEVE_CREDENTIALS, command.type(),
                "CommandType should be RETRIEVE_CREDENTIALS when command is '" + commandLine + "'.");
        assertEquals(2, command.arguments().size(),
                "CommandArguments should be 2 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeGeneratePasswordWhenLineIsAValidGeneratePasswordCommand() {
        String commandLine = "generate-password exampleApp exampleUser";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.GENERATE_PASSWORD, command.type(),
                "CommandType should be GENERATE_PASSWORD when command is '" + commandLine + "'.");
        assertEquals(2, command.arguments().size(),
                "CommandArguments should be 2 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeGeneratePasswordWordsWhenLineIsAValidGeneratePasswordWordsCommand() {
        String commandLine = "generate-password-words exampleApp exampleUser";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.GENERATE_PASSWORD_WORDS, command.type(),
                "CommandType should be GENERATE_PASSWORD_WORDS when command is '" + commandLine + "'.");
        assertEquals(2, command.arguments().size(),
                "CommandArguments should be 2 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeAddPasswordWhenLineIsAValidAddPasswordCommand() {
        String commandLine = "add-password exampleApp exampleUser examplePassword";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.ADD_PASSWORD, command.type(),
                "CommandType should be ADD_PASSWORD when command is '" + commandLine + "'.");
        assertEquals(3, command.arguments().size(),
                "CommandArguments should be 3 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeUpdatePasswordWhenLineIsAValidUpdatePasswordCommand() {
        String commandLine = "update-password exampleApp exampleUser newPassword";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.UPDATE_PASSWORD, command.type(),
                "CommandType should be UPDATE_PASSWORD when command is '" + commandLine + "'.");
        assertEquals(3, command.arguments().size(),
                "CommandArguments should be 3 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeRemovePasswordWhenLineIsAValidRemovePasswordCommand() {
        String commandLine = "remove-password exampleApp exampleUser";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.REMOVE_PASSWORD, command.type(),
                "CommandType should be REMOVE_PASSWORD when command is '" + commandLine + "'.");
        assertEquals(2, command.arguments().size(),
                "CommandArguments should be 2 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeConnectWhenLineIsAValidConnectCommand() {
        String commandLine = "connect";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.CONNECT, command.type(),
                "CommandType should be CONNECT when command is '" + commandLine + "'.");
        assertEquals(0, command.arguments().size(),
                "CommandArguments should be 0 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeDisconnectWhenLineIsAValidDisconnectCommand() {
        String commandLine = "Disconnect ";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.DISCONNECT, command.type(),
                "CommandType should be DISCONNECT when command is '" + commandLine + "'.");
        assertEquals(0, command.arguments().size(),
                "CommandArguments should be 0 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeDeleteAccountWhenLineIsAValidDeleteAccountCommand() {
        String commandLine = "delete-accoUnt 123 123";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.DELETE_ACCOUNT, command.type(),
                "CommandType should be DELETE_ACCOUNT when command is '" + commandLine + "'.");
        assertEquals(2, command.arguments().size(),
                "CommandArguments should be 2 when command is '" + commandLine + "'.");
    }

    @Test
    public void testOfReturnsCommandWithTypeQuitWhenLineIsAValidQuitCommand() {
        String commandLine = "qUiT";
        ClientCommand command = null;
        try {
            command = ClientCommand.of(commandLine);
        } catch (InvalidCommandException e) {
            fail("Of method has thrown InvalidCommandException.");
        }

        assertEquals(ClientCommandType.QUIT, command.type(),
                "CommandType should be QUIT when command is '" + commandLine + "'.");
        assertEquals(0, command.arguments().size(),
                "CommandArguments should be 0 when command is '" + commandLine + "'.");
    }

}