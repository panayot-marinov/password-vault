package bg.sofia.uni.fmi.mjt.password.vault.client.nio;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.exceptions.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequestSender;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.request.RequestType;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;

public class AutoLogoutService {

    private final long delay;
    private final NioRequestSender nioRequestSender;
    private long maxTime;

    public AutoLogoutService(
            long delay, NioRequestSender nioRequestSender) {
        this.delay = delay;
        this.nioRequestSender = nioRequestSender;
    }

    public void updateMaxTime() {
        maxTime = System.currentTimeMillis() + delay;
    }

    public Session logoutAfterDelay(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("Session should not be null.");
        }

        maxTime = System.currentTimeMillis() + delay;
        while (System.currentTimeMillis() < maxTime) {
            try {
                Thread.sleep(maxTime - System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException("AutoLogoutService thread was interrupted.");
            }
        }

        if (session.isLoggedIn()) {
            ClientCommand command = null;
            try {
                command = ClientCommand.of("logout");
            } catch (InvalidCommandException e) {
                System.out.println("Cannot auto-logout user with username " + session.getUsername());
            }

            String username = session.getUsername();
            NioRequest request = NioRequest.builder()
                    .setType(RequestType.LOGOUT)
                    .setUsername(username)
                    .build();

            return nioRequestSender.sendRequest(request, command, session);
        }

        throw new IllegalStateException("User should be logged in before logging out.");
    }
}