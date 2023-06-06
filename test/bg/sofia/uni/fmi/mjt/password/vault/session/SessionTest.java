package bg.sofia.uni.fmi.mjt.password.vault.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionTest {

    @Test
    void testSessionConstructorSetsIsLoggedInFlagToTrue() {
        Session session = new Session("USERNAME", null);
        assertTrue(session.isLoggedIn(), "isLoggedIn() should be true after creating session.");
    }

    @Test
    void testEndSetsIsLoggedInFlagToFalse() {
        Session session = new Session("USERNAME", null);
        session.end();
        assertFalse(session.isLoggedIn(), "isLoggedIn() should be false after ending session.");
    }

}
