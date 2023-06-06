package bg.sofia.uni.fmi.mjt.password.vault.request;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponseHandler;
import bg.sofia.uni.fmi.mjt.password.vault.response.NioResponse;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;

public class NioRequestSender {

    private static final Gson GSON = new Gson();

    private final BufferedReader reader;
    private final PrintWriter writer;

    public NioRequestSender(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public Session sendRequest(NioRequest request, ClientCommand command, Session session) {
        String jsonRequest = GSON.toJson(request);
        writer.println(jsonRequest); //send request to server

        String jsonResponse = null; //read response from server
        try {
            jsonResponse = reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("An error occured while reading response of the server.", e);

        }

        NioResponse response = GSON.fromJson(jsonResponse, NioResponse.class);
        NioResponseHandler responseHandler = new NioResponseHandler();

        Session newSession = responseHandler.handle(command, response, session);
        return newSession;
    }

}