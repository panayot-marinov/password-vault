package bg.sofia.uni.fmi.mjt.password.vault.request;

import bg.sofia.uni.fmi.mjt.password.vault.client.command.ClientCommand;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UnsupportedCommandException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserAlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.client.nio.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.session.Session;

public interface RequestCreator {

    NioRequest create(ClientCommand command, Session session)
            throws UserAlreadyLoggedInException, UserNotLoggedInException, UnsupportedCommandException;

}