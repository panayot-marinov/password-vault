package bg.sofia.uni.fmi.mjt.password.vault.request;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioRequestTest {

    @Test
    public void testOfThrowsIllegalArgumentExceptionIfBytesArrayIsNull() {
        assertThrows(IllegalArgumentException.class, () -> NioRequest.of(null),
                "Method should throw an IllegalArgumentException when bytes are null.");
    }

    @Test
    public void testOfCreatesCorrectNioRequestObject() {
        NioRequest expectedRequest = NioRequest.builder("username")
                .setType(RequestType.REGISTER)
                .build();
        Gson gson = new Gson();
        byte[] requestBytes = gson.toJson(expectedRequest).getBytes(StandardCharsets.UTF_8);

        assertEquals(expectedRequest, NioRequest.of(requestBytes),
                "Method does not return correct NioRequest");
    }

}