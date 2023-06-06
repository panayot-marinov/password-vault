package bg.sofia.uni.fmi.mjt.password.vault.response;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class NioResponse {

    private final ResponseType type;
    private final String body;

    public static NioResponse of(byte[] nioResponseBytes) {
        if (nioResponseBytes == null) {
            throw new IllegalArgumentException("bytes array should not be null.");
        }

        Gson gson = new Gson();
        String nioResponseString = new String(nioResponseBytes, StandardCharsets.UTF_8);

        return gson.fromJson(nioResponseString, NioResponse.class);
    }

    public NioResponse(ResponseType responseType, String body) {
        this.type = responseType;
        this.body = body;
    }

    public ResponseType getType() {
        return this.type;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NioResponse response = (NioResponse) o;
        return type == response.type && Objects.equals(body, response.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, body);
    }

}