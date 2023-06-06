package bg.sofia.uni.fmi.mjt.password.vault.configuration;

import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.ConfigurationDataException;
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

public class ConfigurationData {

    private final String serverHost;
    private final int serverPort;

    public ConfigurationData(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public static ConfigurationData of(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream should not be null.");
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

        String hostname = null;
        int port = 0;
        try {
            String[] configTokens = bufferedReader.readLine().split(" ");
            if (configTokens.length != 2) {
                throw new IllegalArgumentException("Invalid arguments count.");
            }
            hostname = configTokens[0];
            port = Integer.parseInt(configTokens[1]);

        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read configuration from reader.", e);
        }

        return new ConfigurationData(hostname, port);
    }

    public static ConfigurationData of(Path configFilePath)
            throws ConfigurationDataException {
        if (configFilePath == null) {
            throw new IllegalArgumentException("configFilePath should not be null.");
        }

        try {
            List<String> configLines = Files.readAllLines(configFilePath);
            if (configLines.size() == 0) {
                throw new ConfigurationDataException("Configuration file is empty.");
            }

            Gson gson = new Gson();
            ConfigurationData data = gson.fromJson(configLines.get(0), ConfigurationData.class);

            return data;
        } catch (IOException e) {
            throw new ConfigurationDataException("Cannot read configuration file.", e);
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
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
        ConfigurationData that = (ConfigurationData) o;
        return serverPort == that.serverPort && Objects.equals(serverHost, that.serverHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverHost, serverPort);
    }
}