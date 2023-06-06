package bg.sofia.uni.fmi.mjt.password.vault.response;

import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioResponseTest {

    @Test
    public void testOfThrowsIllegalArgumentExceptionIfBytesArrayIsNull() {
        assertThrows(IllegalArgumentException.class, () -> NioRequest.of(null),
                "Method should throw an IllegalArgumentException when bytes are null.");
    }

    @Test
    public void testOfCreatesCorrectNioResponseObject() {
        NioResponse expectedResponse = new NioResponse(ResponseType.REGISTER_SUCCESSFUL, null);
        Gson gson = new Gson();
        byte[] responseBytes = gson.toJson(expectedResponse).getBytes(StandardCharsets.UTF_8);

        assertEquals(expectedResponse, NioResponse.of(responseBytes),
                "Method does not return correct NioResponse");
    }

}