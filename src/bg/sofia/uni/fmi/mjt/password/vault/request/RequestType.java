package bg.sofia.uni.fmi.mjt.password.vault.request;

public enum RequestType {

    REGISTER(3),
    LOGIN(2),
    LOGOUT(1),
    CHANGE_ACCOUNT_PASSWORD(3),
    GET_PASSWORD(2),
    STORE_PASSWORD(3),
    UPDATE_PASSWORD(3),
    REMOVE_PASSWORD(2),
    DELETE_ACCOUNT(2);

    public final int argumentsCount;

    RequestType(int argumentsCount) {
        this.argumentsCount = argumentsCount;
    }

}