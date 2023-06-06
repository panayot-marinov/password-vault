package bg.sofia.uni.fmi.mjt.password.vault.request;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.AesEncryptor;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UnsupportedCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserAlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;

import java.util.Arrays;
import java.util.List;

public class DefaultNioRequestCreator implements RequestCreator {

    private final KeyGenerator keyGenerator;
    private final PasswordGenerator defaultPasswordGenerator;
    private final PasswordGenerator wordListPasswordGenerator;

    public DefaultNioRequestCreator(KeyGenerator keyGenerator,
                                    PasswordGenerator passwordGenerator, PasswordGenerator wordListPasswordGenerator) {
        this.defaultPasswordGenerator = passwordGenerator;
        this.wordListPasswordGenerator = wordListPasswordGenerator;
        this.keyGenerator = keyGenerator;
    }

    @Override
    public NioRequest create(ClientCommand command, Session session)
            throws UserAlreadyLoggedInException, UserNotLoggedInException, UnsupportedCommandException {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot have null value.");
        }

        ClientCommandType type = command.type();
        return switch (type) {
            case REGISTER -> getRegisterNioRequest(command, session);
            case DELETE_ACCOUNT -> getDeleteNioRequest(command, session);
            case LOGIN -> getLoginNioRequest(command, session);
            case CHANGE_PASSWORD -> getChangePasswordNioRequest(command, session);
            case LOGOUT -> getLogoutNioRequest(command, session);
            case GENERATE_PASSWORD -> getGeneratePasswordNioRequest(command, session);
            case GENERATE_PASSWORD_WORDS -> getGeneratePasswordWordsNioRequest(command, session);
            case ADD_PASSWORD -> getAddPasswordNioRequest(command, session);
            case UPDATE_PASSWORD -> getUpdatePasswordNioRequest(command, session);
            case RETRIEVE_CREDENTIALS -> getRetrieveCredentialsNioRequest(command, session);
            case REMOVE_PASSWORD -> getRemovePasswordNioRequest(command, session);
            default -> throw new UnsupportedCommandException("Command is not supported.");
        };
    }

    private NioRequest getRemovePasswordNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before removing a password.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String applicationName = String.valueOf(arguments.get(0));
        String username = String.valueOf(arguments.get(1));

        return NioRequest.builder()
                .setType(RequestType.REMOVE_PASSWORD)
                .setUsername(session.getUsername())
                .setCredentialsUsername(username)
                .setApplicationName(applicationName)
                .build();
    }

    private NioRequest getRetrieveCredentialsNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before retrieving credentials.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String applicationName = String.valueOf(arguments.get(0));
        String username = String.valueOf(arguments.get(1));

        return NioRequest.builder()
                .setType(RequestType.GET_PASSWORD)
                .setUsername(session.getUsername())
                .setCredentialsUsername(username)
                .setApplicationName(applicationName)
                .build();
    }

    private NioRequest getUpdatePasswordNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before updating a password.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String applicationName = String.valueOf(arguments.get(0));
        String username = String.valueOf(arguments.get(1));
        char[] passwordArray = arguments.get(2);

        byte[] passwordBytes = toByteArray(passwordArray);
        String encryptedPassword = null;
        try {
            encryptedPassword =
                    AesEncryptor.encryptData(passwordBytes, session.getPasswordDerivedKey().getKey());
        } catch (Exception e) {
            throw new RuntimeException("Cannot encrypt the generated password.", e);
        }

        ServerPassword password =
                new ServerPassword(new HashedPassword(passwordArray), encryptedPassword);
        Arrays.fill(passwordArray, '0');
        Arrays.fill(passwordBytes, (byte) 0);

        return NioRequest.builder()
                .setType(RequestType.UPDATE_PASSWORD)
                .setUsername(session.getUsername())
                .setCredentialsUsername(username)
                .setApplicationName(applicationName)
                .setPassword(password)
                .build();
    }

    private NioRequest getAddPasswordNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before adding a password.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String applicationName = String.valueOf(arguments.get(0));
        String username = String.valueOf(arguments.get(1));
        char[] passwordArray = arguments.get(2);

        byte[] passwordBytes = toByteArray(passwordArray);
        String encryptedPassword = null;
        try {
            encryptedPassword =
                    AesEncryptor.encryptData(passwordBytes, session.getPasswordDerivedKey().getKey());
        } catch (Exception e) {
            throw new RuntimeException("Cannot encrypt the generated password.", e);
        }

        ServerPassword password =
                new ServerPassword(new HashedPassword(passwordArray), encryptedPassword);
        Arrays.fill(passwordArray, '0');
        Arrays.fill(passwordBytes, (byte) 0);

        return NioRequest.builder()
                .setType(RequestType.STORE_PASSWORD)
                .setUsername(session.getUsername())
                .setCredentialsUsername(username)
                .setApplicationName(applicationName)
                .setPassword(password)
                .build();
    }

    private NioRequest getGeneratePasswordWordsNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before generating a password.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String applicationName = String.valueOf(arguments.get(0));
        String username = String.valueOf(arguments.get(1));

        char[] generatedPassword = wordListPasswordGenerator.generatePassword();
        System.out.println("Generated: " + String.valueOf(generatedPassword));
        byte[] passwordBytes = toByteArray(generatedPassword);
        String encryptedPassword = null;
        try {
            encryptedPassword =
                    AesEncryptor.encryptData(passwordBytes, session.getPasswordDerivedKey().getKey());
        } catch (Exception e) {
            throw new RuntimeException("Cannot encrypt the generated password.", e);
        }

        ServerPassword password =
                new ServerPassword(new HashedPassword(generatedPassword), encryptedPassword);
        Arrays.fill(generatedPassword, '0');
        Arrays.fill(passwordBytes, (byte) 0);

        return NioRequest.builder()
                .setType(RequestType.STORE_PASSWORD)
                .setUsername(session.getUsername())
                .setCredentialsUsername(username)
                .setApplicationName(applicationName)
                .setPassword(password)
                .build();
    }

    private NioRequest getGeneratePasswordNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before generating a password.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String applicationName = String.valueOf(arguments.get(0));
        String username = String.valueOf(arguments.get(1));

        char[] generatedPassword = defaultPasswordGenerator.generatePassword();
        System.out.println("Generated: " + String.valueOf(generatedPassword));
        byte[] passwordBytes = toByteArray(generatedPassword);
        String encryptedPassword = null;
        try {
            encryptedPassword =
                    AesEncryptor.encryptData(passwordBytes, session.getPasswordDerivedKey().getKey());
        } catch (Exception e) {
            throw new RuntimeException("Cannot encrypt the generated password.", e);
        }

        ServerPassword password =
                new ServerPassword(new HashedPassword(generatedPassword), encryptedPassword);
        Arrays.fill(generatedPassword, '0');
        Arrays.fill(passwordBytes, (byte) 0);

        return NioRequest.builder()
                .setType(RequestType.STORE_PASSWORD)
                .setUsername(session.getUsername())
                .setCredentialsUsername(username)
                .setApplicationName(applicationName)
                .setPassword(password)
                .build();
    }

    private NioRequest getLogoutNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        NioRequest nioRequest;
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before logging out.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        nioRequest = NioRequest.builder()
                .setType(RequestType.LOGOUT)
                .setUsername(session.getUsername())
                .build();
        return nioRequest;
    }

    private NioRequest getChangePasswordNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before changing password.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        char[] currentPasswordArray = arguments.get(0);
        char[] newPasswordArray = arguments.get(1);
        char[] newPasswordRepeatedArray = arguments.get(2);

        ServerPassword oldPassword =
                new ServerPassword(new HashedPassword(currentPasswordArray), null);
        ServerPassword newPassword =
                new ServerPassword(new HashedPassword(newPasswordArray), null);
        ServerPassword newPasswordRepeated =
                new ServerPassword(new HashedPassword(newPasswordRepeatedArray), null);
        Arrays.fill(currentPasswordArray, '0');
        Arrays.fill(newPasswordArray, '0');
        Arrays.fill(newPasswordRepeatedArray, '0');

        return NioRequest.builder()
                .setType(RequestType.CHANGE_ACCOUNT_PASSWORD)
                .setUsername(session.getUsername())
                .setOldPassword(oldPassword)
                .setPassword(newPassword)
                .setPasswordRepeated(newPasswordRepeated)
                .build();
    }

    private NioRequest getLoginNioRequest(ClientCommand command, Session session)
            throws UserAlreadyLoggedInException {
        if (session != null && session.isLoggedIn()) {
            throw new UserAlreadyLoggedInException("User should logout before logging in again.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String username = String.valueOf(arguments.get(0));
        char[] passwordArray = arguments.get(1);
        ServerPassword password =
                new ServerPassword(new HashedPassword(passwordArray), null);
        Arrays.fill(passwordArray, '0');

        return NioRequest.builder()
                .setType(RequestType.LOGIN)
                .setUsername(username)
                .setPassword(password)
                .build();
    }

    private NioRequest getDeleteNioRequest(ClientCommand command, Session session)
            throws UserNotLoggedInException {
        if (session == null || !session.isLoggedIn()) {
            throw new UserNotLoggedInException("User should be logged in before removing its account.");
        }
        List<char[]> arguments = command.arguments();

        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        char[] passwordArray = arguments.get(0);
        char[] passwordRepeatedArray = arguments.get(1);

        ServerPassword password = new ServerPassword(
                new HashedPassword(passwordArray), null);
        ServerPassword passwordRepeated = new ServerPassword(
                new HashedPassword(passwordRepeatedArray), null);
        Arrays.fill(passwordArray, '0');
        Arrays.fill(passwordRepeatedArray, '0');

        return NioRequest.builder()
                .setType(RequestType.DELETE_ACCOUNT)
                .setUsername(session.getUsername())
                .setPassword(password)
                .setPasswordRepeated(passwordRepeated)
                .build();
    }

    private NioRequest getRegisterNioRequest(ClientCommand command, Session session)
            throws UserAlreadyLoggedInException {
        if (session != null && session.isLoggedIn()) {
            throw new UserAlreadyLoggedInException("User should logout before making a new registration.");
        }
        List<char[]> arguments = command.arguments();
        if (arguments.size() != command.type().argumentsCount) {
            throw new IllegalArgumentException(
                    "Arguments count should be "
                            + command.type().argumentsCount + ".");
        }

        String username = String.valueOf(arguments.get(0));
        char[] passwordArray = arguments.get(1);
        char[] passwordRepeatedArray = arguments.get(2);

        ServerPassword password =
                new ServerPassword(new HashedPassword(passwordArray), null);
        ServerPassword passwordRepeated =
                new ServerPassword(new HashedPassword(passwordRepeatedArray), null);
        Arrays.fill(passwordArray, '0');
        Arrays.fill(passwordRepeatedArray, '0');

        PasswordDerivedKey key = null;
        try {
            key = keyGenerator.generateKey(passwordArray);
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate key for master password encryption.", e);
        }
        EncryptionData encryptionData = new EncryptionData(key.getIterationsCount(), key.getSalt());

        return NioRequest.builder()
                .setType(RequestType.REGISTER)
                .setUsername(username)
                .setPassword(password)
                .setPasswordRepeated(passwordRepeated)
                .setEncryptionData(encryptionData)
                .build();
    }

    private byte[] toByteArray(char[] charArray) {
        if (charArray == null)
            return null;

        int charArrayLength = charArray.length;
        byte[] byteArray = new byte[charArrayLength];
        for (int i = 0; i < charArrayLength; i++)
            byteArray[i] = (byte) (charArray[i]);
        return byteArray;
    }

}