package bg.sofia.uni.fmi.mjt.password.vault.request;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.EncryptionData;
import bg.sofia.uni.fmi.mjt.password.vault.server.ServerPassword;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class NioRequest {

    //required
    private final RequestType type;
    private final String username;

    //optional
    private final String credentialsUsername;
    private final String applicationName;
    private final ServerPassword password;
    private final ServerPassword passwordRepeated;
    private final ServerPassword oldPassword;
    private final EncryptionData encryptionData;

    public static NioRequest of(byte[] nioRequestBytes) {
        if (nioRequestBytes == null) {
            throw new IllegalArgumentException("bytes array should not be null.");
        }

        Gson gson = new Gson();
        String nioRequestString = new String(nioRequestBytes, StandardCharsets.UTF_8);

        return gson.fromJson(nioRequestString, NioRequest.class);
    }

    public static NioRequestBuilder builder() {
        return new NioRequestBuilder();
    }

    public static NioRequestBuilder builder(String username) {
        return new NioRequestBuilder(username);
    }

    public RequestType getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getCredentialsUsername() { return credentialsUsername; }

    public String getApplicationName() {
        return applicationName;
    }

    public ServerPassword getPassword() {
        return password;
    }

    public ServerPassword getPasswordRepeated() {
        return passwordRepeated;
    }

    public ServerPassword getOldPassword() {
        return oldPassword;
    }

    public EncryptionData getEncryptionData() {
        return encryptionData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NioRequest request = (NioRequest) o;
        return type == request.type && Objects.equals(username, request.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, username);
    }

    private NioRequest(NioRequestBuilder builder) {
        this.type = builder.type;
        this.username = builder.username;
        this.credentialsUsername = builder.credentialsUsername;
        this.applicationName = builder.applicationName;
        this.password = builder.password;
        this.passwordRepeated = builder.passwordRepeated;
        this.oldPassword = builder.oldPassword;
        this.encryptionData = builder.encryptionData;
    }

    public static class NioRequestBuilder {

        //required
        private RequestType type;
        private String username;
        //optional
        private String credentialsUsername;
        private String applicationName;
        private ServerPassword password;
        private ServerPassword passwordRepeated;
        private ServerPassword oldPassword;
        private EncryptionData encryptionData;

        private NioRequestBuilder() {
        }

        private NioRequestBuilder(String username) {
            setUsername(username);
        }

        public NioRequestBuilder setType(RequestType type) {
            this.type = type;
            return this;
        }

        public NioRequestBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public NioRequestBuilder setCredentialsUsername(String credentialsUsername) {
            this.credentialsUsername = credentialsUsername;
            return this;
        }

        public NioRequestBuilder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public NioRequestBuilder setPassword(ServerPassword password) {
            this.password = password;
            return this;
        }

        public NioRequestBuilder setPasswordRepeated(ServerPassword passwordRepeated) {
            this.passwordRepeated = passwordRepeated;
            return this;
        }

        public NioRequestBuilder setOldPassword(ServerPassword oldPassword) {
            this.oldPassword = oldPassword;
            return this;
        }

        public NioRequestBuilder setEncryptionData(EncryptionData encryptionData) {
            this.encryptionData = encryptionData;
            return this;
        }

        public NioRequest build() {
            if (username == null || type == null) {
                throw new IllegalStateException(
                        "Username and requestType are required parameters and can't have null values.");
            }

            return new NioRequest(this);
        }

    }

}