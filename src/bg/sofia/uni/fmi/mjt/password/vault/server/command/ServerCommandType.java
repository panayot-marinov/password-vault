package bg.sofia.uni.fmi.mjt.password.vault.server.command;

public enum ServerCommandType {

    HELP(0, "help"),
    STOP(0, "stop"),
    LAST_LOGS(1, "last-logs <count>"),
    UNKNOWN(0, "");

    public final int argumentsCount;
    public final String helpMessage;

    ServerCommandType(int argumentsCount, String helpMessage) {
        this.argumentsCount = argumentsCount;
        this.helpMessage = helpMessage;
    }

}