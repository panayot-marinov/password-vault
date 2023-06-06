package bg.sofia.uni.fmi.mjt.password.vault.configuration;

import bg.sofia.uni.fmi.mjt.password.vault.configuration.exceptions.CompromisedApiKeyDataException;
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

public class CompromisedApiKeyDataTest {

    private static final String COMPROMISED_API_KEY_DATA_FILE_NAME = "testApiKeyData.txt";
    private static final String API_KEY = "API_KEY";
    private static final String SECRET = "SECRET";
    private static final CompromisedApiKeyData COMPROMISED_API_KEY_DATA =
            new CompromisedApiKeyData(API_KEY, SECRET);
    private static final String COMPROMISED_API_KEY_DATA_STRING =
            "{\"apiKey\":\"API_KEY\",\"secret\":\"SECRET\"}";
    private static final String COMPROMISED_API_KEY_DATA_INPUT = "API_KEY SECRET";

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
    public void testOfReadsDataCorrectlyFromInputStream() {
        try(InputStream inputStream = new ByteArrayInputStream(
                COMPROMISED_API_KEY_DATA_INPUT.getBytes(StandardCharsets.UTF_8))) {
            CompromisedApiKeyData compromisedApiKeyData = CompromisedApiKeyData.of(inputStream);
            assertEquals(COMPROMISED_API_KEY_DATA, compromisedApiKeyData,
                    "Method has not created a correct compromisedApiKeyData.");
        } catch (IOException e) {
            fail("Method has thrown an IOException.", e);
        }
    }

    @Test
    public void testOfReadsDataCorrectlyFromPath() throws CompromisedApiKeyDataException {
        try(PrintWriter printWriter = new PrintWriter(new BufferedWriter(
                new FileWriter(COMPROMISED_API_KEY_DATA_FILE_NAME, false)), true)) {
            printWriter.println(COMPROMISED_API_KEY_DATA_STRING);
            CompromisedApiKeyData compromisedApiKeyData = CompromisedApiKeyData.of(Path.of(COMPROMISED_API_KEY_DATA_FILE_NAME));
            assertEquals(COMPROMISED_API_KEY_DATA, compromisedApiKeyData,
                    "Method has not created a correct compromisedApiKeyData.");

        } catch (IOException e) {
            fail("Method has thrown an IOException.", e);
        }

        try {
            Files.deleteIfExists(Path.of(COMPROMISED_API_KEY_DATA_FILE_NAME));
        } catch (IOException e) {
            fail("Method has thrown an IOException.", e);
        }
    }

    @Test
    public void testWriteConfigurationThrowsIllegalArgumentExceptionWhenStreamIsNull() {
        assertThrows(IllegalArgumentException.class, () -> COMPROMISED_API_KEY_DATA.writeConfiguration(null),
                "Method should throw an IllegalArgumentException when writer is null.");
    }

    @Test
    public void testWriteConfigurationWritesDataCorrectlyWhenWriterIsNotNull() {
        COMPROMISED_API_KEY_DATA.writeConfiguration(writer);
        assertEquals(COMPROMISED_API_KEY_DATA_STRING + System.lineSeparator(), writer.toString(),
                "Configuration data has not been written correctly.");
    }

}