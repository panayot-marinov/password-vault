package bg.sofia.uni.fmi.mjt.password.vault.client.nio;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommandType;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequestSender;
import bg.sofia.uni.fmi.mjt.password.vault.request.NioRequest;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import java.util.List;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AutoLogoutServiceTest {

    private static final int DELAY = 100;
    private static final int DELAY_DELTA = 50;
    private static final String USERNAME = "exampleUsername";

    @Mock
    private static final NioRequestSender nioRequestSenderMock = mock(NioRequestSender.class);
    @Mock
    private static final Session sessionMock = mock(Session.class);
    private final AutoLogoutService logoutService = new AutoLogoutService(DELAY, nioRequestSenderMock);

    @BeforeAll
    public static void setUpAll() {
        when(sessionMock.getUsername()).thenReturn(USERNAME);
    }

    @Test
    public void testLogoutAfterDelayThrowsIllegalArgumentExceptionWhenSessionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> logoutService.logoutAfterDelay(null),
                "Method should throw an IllegalArgumentException when session is null.");
        verify(nioRequestSenderMock, never()).sendRequest(
                Mockito.any(NioRequest.class), Mockito.any(ClientCommand.class), Mockito.any(Session.class));
    }

    @Test
    public void testLogoutAfterDelayThrowsIllegalStateExceptionWhenSessionIsNotLoggedIn() {
        when(sessionMock.isLoggedIn()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> logoutService.logoutAfterDelay(sessionMock),
                "Method should throw an IllegalStateException when session.isLoggedIn() is false.");
        verify(sessionMock, atLeastOnce()).isLoggedIn();
        verify(nioRequestSenderMock, never()).sendRequest(
                Mockito.any(NioRequest.class), Mockito.any(ClientCommand.class), Mockito.any(Session.class));
    }

    @Test
    public void testLogoutAfterDelaySendsRequestAfterDelayWithoutUpdatingTheDelay() {
        when(sessionMock.isLoggedIn()).thenReturn(true);

        assertTimeout(ofMillis(DELAY + DELAY_DELTA), () -> logoutService.logoutAfterDelay(sessionMock));

        verify(sessionMock, atLeastOnce()).isLoggedIn();
        verify(sessionMock, atLeastOnce()).getUsername();
        verify(nioRequestSenderMock, Mockito.times(1)).sendRequest(
                Mockito.any(NioRequest.class),
                eq(new ClientCommand(ClientCommandType.LOGOUT, List.of())),
                eq(sessionMock));
        //new DefaultCommand(CommandType.LOGOUT, List.of()), sessionMock);
    }

}