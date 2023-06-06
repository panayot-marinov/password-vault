package bg.sofia.uni.fmi.mjt.password.vault.server.nio.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioRequestHandlerTest {

    @Test
    public void testOfThrowsIllegalArgumentExceptionWhenArgumentsAreNull() {
        assertThrows(IllegalArgumentException.class,
                () -> NioRequestHandler.of(null, null, null, null),
                "Method should throw an IllegalArgumentException when arguments are null.");
    }

}
