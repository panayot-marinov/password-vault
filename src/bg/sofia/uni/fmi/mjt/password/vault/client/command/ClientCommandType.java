package bg.sofia.uni.fmi.mjt.password.vault.client.command;

public enum ClientCommandType {

    HELP(0, "help"),
    REGISTER(3, "register <user> <password> <password-repeat>"),
    LOGIN(2, "login <user> <password>"),
    LOGOUT(0, "logout"),
    CHANGE_PASSWORD(3,
            "change-password <old-password> <new-password> <new-password-repeat>"),
    RETRIEVE_CREDENTIALS(2, "retrieve-credentials <applicationName> <user>"),
    GENERATE_PASSWORD(2, "generate-password <applicationName> <user>"),
    GENERATE_PASSWORD_OPTIONS(111, ""),
    GENERATE_PASSWORD_WORDS(2, "generate-password-words <applicationName> <user>"),
    GENERATE_PASSWORD_WORDS_OPTIONS(111, ""),
    ADD_PASSWORD(3, "add-password <applicationName> <user> <password>"),
    UPDATE_PASSWORD(3, "update-password <applicationName> <user> <password>"),
    REMOVE_PASSWORD(2, "remove-password <applicationName> <user>"),
    CONNECT(0, "connect"),
    DISCONNECT(0, "disconnect"),
    DELETE_ACCOUNT(2, "delete-account <password> <password-repeat>"),
    UNKNOWN(0, ""),
    QUIT(0, "quit");

    public final int argumentsCount;
    public final String helpMessage;

    ClientCommandType(int argumentsCount, String helpMessage) {
        this.argumentsCount = argumentsCount;
        this.helpMessage = helpMessage;
    }

}