package bg.sofia.uni.fmi.mjt.password.vault.server.compromised;

import bg.sofia.uni.fmi.mjt.password.vault.client.HashedPassword;
import bg.sofia.uni.fmi.mjt.password.vault.server.compromised.exceptions.CompromisedPasswordsClientException;

public interface CompromisedPasswordsClient {

    boolean isCompromised(HashedPassword password)
            throws CompromisedPasswordsClientException;

}