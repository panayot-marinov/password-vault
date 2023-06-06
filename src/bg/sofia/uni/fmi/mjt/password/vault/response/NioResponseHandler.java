package bg.sofia.uni.fmi.mjt.password.vault.response;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.AesEncryptor;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordDerivedKey;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.Pbkdf2KeyGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import com.google.gson.Gson;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class NioResponseHandler {

    private static final Gson GSON = new Gson();

    public Session handle(ClientCommand command, NioResponse response, Session session) {
        if (command == null || response == null) {
            throw new IllegalArgumentException("Command and response should have non-null values");
        }

        ResponseType type = response.getType();
        switch (type) {
            case LOGIN_SUCCESSFUL: {
                session = handleLoginSuccessfulResponse(command, response);
                break;
            }
            case ACCOUNT_DELETED_SUCCESSFULLY:
            case LOGOUT_SUCCESSFUL: {
                session.end();
                break;
            }
            case CREDENTIALS_FOUND: {
                handleCredentialsFoundResponse(response, session);
                break;
            }
        }
        System.out.println(type.responseTypeMessage);

        return session;
    }

    private void handleCredentialsFoundResponse(NioResponse response, Session session) {
        String encryptedPassword = GSON.fromJson(response.getBody(), String.class);
        String decryptedPassword = null;
        try {
            SecretKey key = session.getPasswordDerivedKey().getKey();
            byte[] decryptedPasswordBytes = AesEncryptor.decryptData(encryptedPassword,
                    session.getPasswordDerivedKey().getKey());
            decryptedPassword = new String(decryptedPasswordBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Cannot decrypt the generated password.", e);
        }
        System.out.println("Decrypted password is : " + decryptedPassword);
    }

    private Session handleLoginSuccessfulResponse(ClientCommand command, NioResponse response) {
        Session session;
        session = GSON.fromJson(response.getBody(), Session.class);

        KeyGenerator keyGenerator = Pbkdf2KeyGenerator.builder()
                .setIterationsCount(session.getPasswordDerivedKey().getIterationsCount())
                .build();
        PasswordDerivedKey passwordDerivedKey = null;
        try {
            char[] password = command.arguments().get(1);
            passwordDerivedKey = keyGenerator.generateKey(password, session.getPasswordDerivedKey().getSalt());
            session.getPasswordDerivedKey().setKey(passwordDerivedKey.getKey());
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate key for master password encryption.", e);
        }
        return session;
    }

}