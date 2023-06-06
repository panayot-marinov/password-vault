package bg.sofia.uni.fmi.mjt.password.vault.server.compromised;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.exceptions.CompromisedPasswordsClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultCompromisedPasswordsClientTest {

    private static final int RESPONSE_CODE_COMPROMISED_PASSWORD = 200;
    private static final String RESPONSE_BODY_COMPROMISED_PASSWORD = "{\"candidates\":[{\"sha256\":\"ef797c8118f02dfb\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649607\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649607dd5d3f8c7623\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649607dd5d3f8c7623048c9\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532c\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"sha256\":\"ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1},{\"md5\":\"25d55ad283aa400af464c76d713c07ad\",\"sha1\":\"7c222fb2927d828af22f592134e8932480637c0d\",\"sha256\":\"ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f\",\"revealedInExposure\":true,\"relativeExposureFrequency\":21,\"exposureCount\":16263},{\"sha1\":\"25d55ad283aa400af464400af464c76d713c07ad\",\"revealedInExposure\":true,\"relativeExposureFrequency\":0,\"exposureCount\":1}]}";
    private static final int RESPONSE_CODE_NON_COMPROMISED_PASSWORD = 404;
    private static final String RESPONSE_BODY_NON_COMPROMISED_PASSWORD = "";
    @Mock
    private static final HttpResponse<String> responseMock = mock(HttpResponse.class);
    @Mock
    private static final HttpClient httpClientMock = mock(HttpClient.class);
    private static final HashedPassword HASHED_PASSWORD = new HashedPassword("12345678".toCharArray());

    private CompromisedPasswordsClient passwordsClient;

    @BeforeEach
    public void setUp() {
        passwordsClient = DefaultCompromisedPasswordsClient.builder()
                .setHttpClient(httpClientMock)
                .setApiKey("TEST_API_KEY")
                .setSecret("TEST_SECRET")
                .setApiEndpointScheme("https")
                .setApiEndpointHost("api.enzoic.com")
                .setApiEndpointPath("passwords")
                .build();

        Mockito.reset(responseMock);
        Mockito.reset(httpClientMock);
    }

    @Test
    public void testIsCompromisedThrowsIllegalArgumentExceptionWhenPasswordIsNull()
            throws IOException, InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> passwordsClient.isCompromised(null),
                "Method should throw an IllegalArgumentException when password is null.");

        verify(responseMock, never()).statusCode();
        verify(responseMock, never()).body();
        verify(httpClientMock, never())
                .send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    public void testIsCompromisedThrowsCompromisedPasswordsClientExceptionWhenHttpClientSendThrowsException() {
        when(httpClientMock.sendAsync(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(RuntimeException.class);

        assertThrows(CompromisedPasswordsClientException.class,
                () -> passwordsClient.isCompromised(HASHED_PASSWORD),
                "Method should throw a CompromisedPasswordsClientException " +
                        "when sending http request has thrown an exception");

        verify(responseMock, never()).body();
        verify(httpClientMock, times(1))
                .sendAsync(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    public void testIsCompromisedReturnsTrueWhenResponseSaysPasswordIsCompromised()
            throws IOException, InterruptedException {
        when(responseMock.statusCode()).thenReturn(RESPONSE_CODE_COMPROMISED_PASSWORD);
        when(responseMock.body()).thenReturn(RESPONSE_BODY_COMPROMISED_PASSWORD);
        when(httpClientMock.sendAsync(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> responseMock));

        try {
            assertTrue(passwordsClient.isCompromised(HASHED_PASSWORD),
                    "Method should return true when response body status code is 200" +
                            " and body says password is compromised.");
        } catch (CompromisedPasswordsClientException e) {
            fail("Method has thrown an CompromisedPasswordsClientException");
        }

        verify(responseMock, atLeastOnce()).body();
        verify(httpClientMock, times(1))
                .sendAsync(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    public void testIsCompromisedReturnsFalseWhenResponseSaysPasswordNotIsCompromised() throws IOException, InterruptedException {
        when(responseMock.statusCode()).thenReturn(RESPONSE_CODE_NON_COMPROMISED_PASSWORD);
        when(responseMock.body()).thenReturn(RESPONSE_BODY_NON_COMPROMISED_PASSWORD);
        when(httpClientMock.sendAsync(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> responseMock));

        try {
            assertFalse(passwordsClient.isCompromised(HASHED_PASSWORD),
                    "Method should return false when response body status code is 404" +
                            " and body is blank.");
        } catch (CompromisedPasswordsClientException e) {
            fail("Method has thrown an CompromisedPasswordsClientException");
        }

        verify(responseMock, atLeastOnce()).body();
        verify(httpClientMock, times(1))
                .sendAsync(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    public void testBuildThrowsIllegalStateExceptionWhenHttpClientApiKeyOrSecretAreNull()
            throws IOException, InterruptedException {
        assertThrows(IllegalStateException.class,
                () -> DefaultCompromisedPasswordsClient.builder()
                        .setApiKey("TEST_API_KEY")
                        .setSecret("TEST_SECRET").build(),
                "Method should throw an IllegalStateException when httpClient is null.");

        assertThrows(IllegalStateException.class,
                () -> DefaultCompromisedPasswordsClient.builder(httpClientMock)
                        .setSecret("TEST_SECRET").build(),
                "Method should throw an IllegalStateException when httpClient is null.");

        assertThrows(IllegalStateException.class,
                () -> DefaultCompromisedPasswordsClient.builder(httpClientMock)
                        .setApiKey("TEST_API_KEY")
                        .build(),
                "Method should throw an IllegalStateException when httpClient is null.");

        verify(responseMock, never()).statusCode();
        verify(responseMock, never()).body();
        verify(httpClientMock, never())
                .send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }
}