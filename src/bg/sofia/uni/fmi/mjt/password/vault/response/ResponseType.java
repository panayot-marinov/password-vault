package bg.sofia.uni.fmi.mjt.password.vault.response;

public enum ResponseType {

    USERNAME_ALREADY_EXISTS("Username already exists."),
    PASSWORDS_DO_NOT_MATCH("Passwords do not match."),
    REGISTER_SUCCESSFUL("Successfully registered."),
    INVALID_USERNAME_OR_PASSWORD("Invalid username or password."),
    LOGIN_SUCCESSFUL("Successfully logged in."),
    USER_NOT_LOGGED_IN("User is not logged in."),
    USER_NOT_FOUND("User with such an username is not found."),
    CREDENTIALS_NOT_FOUND("Credentials password with such an combination of " +
            "application name and credentials username is not found"),
    CREDENTIALS_FOUND("Credentials password found."),
    CREDENTIALS_ALREADY_EXIST("Credentials already exist."),
    LOGOUT_SUCCESSFUL("User logged out successfully."),
    PASSWORD_COMPROMISED("Password is compromised."),
    PASSWORD_STORED_SUCCESSFULLY("Password is not compromised and stored successfully."),
    PASSWORD_UPDATED_SUCCESSFULLY("Credentials updated successfully"),
    ACCOUNT_PASSWORD_CHANGED_SUCCESSFULLY("Password changed successfully"),
    PASSWORD_DOES_NOT_EXIST("Password does not exist."),
    PASSWORD_REMOVED_SUCCESSFULLY("Password removed successfully."),
    ACCOUNT_DELETED_SUCCESSFULLY("Account deleted successfully."),
    INTERNAL_SERVER_ERROR("Internal server error."),
    REQUEST_NOT_SUPPORTED("Request not supported."),
    EQUAL_OLD_AND_NEW_PASSWORDS("New password cannot be the same as the old password.");

    public String responseTypeMessage;

    ResponseType(String responseTypeMessage) {
        this.responseTypeMessage = responseTypeMessage;
    }

}