package bg.sofia.uni.fmi.mjt.password.vault.server.compromised;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.dto.CompromisedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.dto.CompromisedPasswordCandidates;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.exceptions.CompromisedPasswordsClientException;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DefaultCompromisedPasswordsClient implements CompromisedPasswordsClient {

    private static final int PARTIAL_HASHES_LENGTH = 10;

    private static final Gson GSON = new Gson();

    //Required;
    private final HttpClient httpClient;
    private final String base64EncodedKeyAndSecret;

    //optional
    private final String apiEndpointScheme;
    private final String apiEndpointHost;
    private final String apiEndpointPath;

    public static DefaultCompromisedPasswordsClientBuilder builder() {
        return new DefaultCompromisedPasswordsClientBuilder();
    }

    public static DefaultCompromisedPasswordsClientBuilder builder(HttpClient httpClient) {
        return new DefaultCompromisedPasswordsClientBuilder(httpClient);
    }

    public boolean isCompromised(HashedPassword password)
            throws CompromisedPasswordsClientException {
        if (password == null) {
            throw new IllegalArgumentException("Password argument cannot be null.");
        }

        Collection<CompromisedPassword> candidates = getCompromisedPasswordCandidates(password);

        for (CompromisedPassword candidate : candidates) {
            if ((candidate.getMd5() != null && candidate.getMd5().equals(password.getMd5())) ||
                    (candidate.getSha1() != null && candidate.getSha1().equals(password.getSha1())) ||
                    (candidate.getSha256() != null && candidate.getSha256().equals(password.getSha256()))) {
                return true;
            }
        }

        return false;
    }

    private Collection<CompromisedPassword> getCompromisedPasswordCandidates(HashedPassword password)
            throws CompromisedPasswordsClientException {
        try {
            return sendApiRequest(password)
                    .thenApply(response -> parseResponse(response))
                    .get();
        } catch (InterruptedException e) {
            throw new CompromisedPasswordsClientException("Sending request was interrupted.", e);
        } catch (ExecutionException e) {
            throw new CompromisedPasswordsClientException("A problem occurred while sending request", e);
        }
    }

    private CompletableFuture<HttpResponse<String>> sendApiRequest(HashedPassword password)
            throws CompromisedPasswordsClientException {
        try {
            URI uri = new URI(apiEndpointScheme, apiEndpointHost,
                    apiEndpointPath, null, null);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-type", "application/json")
                    .header("authorization", "basic " + base64EncodedKeyAndSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{ " +
                                    "\"partialSHA1\": \"" +
                                    password.getMd5().substring(0, PARTIAL_HASHES_LENGTH) +
                                    "\", \"partialMD5\": \"" +
                                    password.getSha1().substring(0, PARTIAL_HASHES_LENGTH) +
                                    "\", \"partialSHA256\": \"" +
                                    password.getSha256().substring(0, PARTIAL_HASHES_LENGTH)
                                    + "\" }"))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new CompromisedPasswordsClientException("Cannot retrieve compromised passwords data.", e);
        }
    }

    private Collection<CompromisedPassword> parseResponse(HttpResponse<String> response) {
        String responseBody = response.body();

        if (responseBody == null || responseBody.isBlank()) {
            return List.of();
        }

        CompromisedPasswordCandidates candidates =
                GSON.fromJson(responseBody, CompromisedPasswordCandidates.class);

        return candidates.getAsCollection();
    }

    private DefaultCompromisedPasswordsClient(DefaultCompromisedPasswordsClientBuilder builder) {
        this.httpClient = builder.httpClient;
        this.base64EncodedKeyAndSecret =
                Base64.getEncoder().encodeToString(
                        (builder.apiKey + ":" + builder.secret).getBytes(StandardCharsets.UTF_8));
        this.apiEndpointScheme = builder().apiEndpointScheme;
        this.apiEndpointHost = builder().apiEndpointHost;
        this.apiEndpointPath = builder().apiEndpointPath;
    }

    public static class DefaultCompromisedPasswordsClientBuilder {

        private static final String API_ENDPOINT_SCHEME = "https";
        private static final String API_ENDPOINT_HOST = "api.enzoic.com";
        private static final String API_ENDPOINT_PATH = "/v1/passwords";

        //required parameters
        private HttpClient httpClient;
        private String apiKey;
        private String secret;

        //optional
        private String apiEndpointScheme;
        private String apiEndpointHost;
        private String apiEndpointPath;

        private DefaultCompromisedPasswordsClientBuilder() {
            this.apiEndpointScheme = API_ENDPOINT_SCHEME;
            this.apiEndpointHost = API_ENDPOINT_HOST;
            this.apiEndpointPath = API_ENDPOINT_PATH;
        }

        public DefaultCompromisedPasswordsClientBuilder(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public DefaultCompromisedPasswordsClientBuilder setHttpClient(HttpClient httpClient) {
            if (httpClient == null) {
                throw new IllegalArgumentException("HttpClient cannot be null.");
            }

            this.httpClient = httpClient;
            return this;
        }

        public DefaultCompromisedPasswordsClientBuilder setApiKey(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("ApiKey cannot be neither null or blank.");
            }

            this.apiKey = apiKey;
            return this;
        }

        public DefaultCompromisedPasswordsClientBuilder setSecret(String secret) {
            if (secret == null || secret.isBlank()) {
                throw new IllegalArgumentException("Secret cannot be neither null or blank.");
            }

            this.secret = secret;
            return this;
        }

        public DefaultCompromisedPasswordsClientBuilder setApiEndpointScheme(String apiEndpointScheme) {
            if (apiEndpointScheme == null || apiEndpointScheme.isBlank()) {
                throw new IllegalArgumentException("ApiEndpointScheme cannot be neither null or blank.");
            }

            this.apiEndpointScheme = apiEndpointScheme;
            return this;
        }

        public DefaultCompromisedPasswordsClientBuilder setApiEndpointHost(String apiEndpointHost) {
            if (apiEndpointHost == null || apiEndpointHost.isBlank()) {
                throw new IllegalArgumentException("ApiEndpointHost cannot be neither null or blank.");
            }

            this.apiEndpointHost = apiEndpointHost;
            return this;
        }

        public DefaultCompromisedPasswordsClientBuilder setApiEndpointPath(String apiEndpointPath) {
            if (apiEndpointPath == null || apiEndpointPath.isBlank()) {
                throw new IllegalArgumentException("ApiEndpointPath cannot be neither null or blank.");
            }

            this.apiEndpointPath = apiEndpointPath;
            return this;
        }

        public DefaultCompromisedPasswordsClient build() {
            if (httpClient == null || apiKey == null || secret == null) {
                throw new IllegalStateException("Http client, apiKey and secret cannot be null.");
            }

            return new DefaultCompromisedPasswordsClient(this);
        }

    }

}