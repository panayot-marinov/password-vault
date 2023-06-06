package bg.sofia.uni.fmi.mjt.password.vault.configuration;

import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.CompromisedApiKeyDataException;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class CompromisedApiKeyData {

    private final String apiKey;
    private final String secret;

    public CompromisedApiKeyData(String apiKey, String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
    }

    public static CompromisedApiKeyData of(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream should not be null.");
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

        String apiKey = null;
        String secret = null;
        try {
            String[] configTokens = bufferedReader.readLine().split(" ");
            if (configTokens.length != 2) {
                throw new IllegalArgumentException("Invalid arguments count.");
            }
            apiKey = configTokens[0];
            secret = configTokens[1];

        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read configuration from reader.", e);
        }

        return new CompromisedApiKeyData(apiKey, secret);
    }

    public static CompromisedApiKeyData of(Path configFilePath)
            throws CompromisedApiKeyDataException {
        if (configFilePath == null) {
            throw new IllegalArgumentException("configFilePath should not be null.");
        }

        try {
            List<String> configLines = Files.readAllLines(configFilePath);
            if (configLines.size() == 0) {
                throw new CompromisedApiKeyDataException("Key file is empty.");
            }

            Gson gson = new Gson();
            CompromisedApiKeyData data = gson.fromJson(configLines.get(0), CompromisedApiKeyData.class);

            return data;
        } catch (IOException e) {
            throw new CompromisedApiKeyDataException("Cannot read key file.", e);
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecret() {
        return secret;
    }

    public void writeConfiguration(Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("Writer should not be null.");
        }

        Gson gson = new Gson();
        String jsonData = gson.toJson(this);

        PrintWriter printWriter = new PrintWriter(new BufferedWriter(writer), true);
        printWriter.println(jsonData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompromisedApiKeyData that = (CompromisedApiKeyData) o;
        return Objects.equals(apiKey, that.apiKey) && Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, secret);
    }

}