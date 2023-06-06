package bg.sofia.uni.fmi.mjt.password.vault.configuration;

import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.ConfigurationDataException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ConfigurationDataTest {

    private static final String CONFIGURATION_DATA_FILE_NAME = "testConfig.txt";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7515;

    private static final ConfigurationData CONFIGURATION_DATA =
            new ConfigurationData(SERVER_HOST, SERVER_PORT);

    private static final String CONFIGURATION_DATA_STRING = "{\"serverHost\":\"localhost\",\"serverPort\":7515}";
    private static final String CONFIGURATION_DATA_INPUT = "localhost 7515";

    private static Writer writer;

    @BeforeAll
    public static void setUpAll() {
        writer = new StringWriter();
    }

    @AfterAll
    public static void tearDownAll() {
        try {
            writer.close();
        } catch (IOException e) {
            fail("Closing stringWriter has thrown an IOException.");
        }
    }

    @Test
    public void testOfReadsDataCorrectlyFromPath() throws ConfigurationDataException {
        try(PrintWriter printWriter = new PrintWriter(new BufferedWriter(
                new FileWriter(CONFIGURATION_DATA_FILE_NAME, false)), true)) {
            printWriter.println(CONFIGURATION_DATA_STRING);
            ConfigurationData configurationData = ConfigurationData.of(Path.of(CONFIGURATION_DATA_FILE_NAME));
            assertEquals(CONFIGURATION_DATA, configurationData,
                    "Method has not created a correct compromisedApiKeyData.");

        } catch (IOException e) {
            fail("Method has thrown an IOException.", e);
        }

        try {
            Files.deleteIfExists(Path.of(CONFIGURATION_DATA_FILE_NAME));
        } catch (IOException e) {
            fail("Method has thrown an IOException.", e);
        }
    }

    @Test
    public void testOfReadsDataCorrectlyFromInputStream() {
        try(InputStream inputStream = new ByteArrayInputStream(
                CONFIGURATION_DATA_INPUT.getBytes(StandardCharsets.UTF_8))) {
            ConfigurationData configurationData = ConfigurationData.of(inputStream);
            assertEquals(CONFIGURATION_DATA, configurationData,
                    "Method has not created a correct configurationData.");
        } catch (IOException e) {
            fail("Method has thrown an IOException.", e);
        }
    }

    @Test
    public void testWriteConfigurationThrowsIllegalArgumentExceptionWhenStreamIsNull() {
        assertThrows(IllegalArgumentException.class, () -> CONFIGURATION_DATA.writeConfiguration(null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testWriteConfigurationWritesDataCorrectlyWhenWriterIsNotNull() {
        CONFIGURATION_DATA.writeConfiguration(writer);
        assertEquals(CONFIGURATION_DATA_STRING + System.lineSeparator(), writer.toString(),
                "Configuration data has not been written correctly.");
    }

}