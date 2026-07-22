package com.lockbox.services;

import org.junit.jupiter.api.Test;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    @Test
    public void testSessionKeyManagement() throws Exception {
        SessionManager sessionManager = new SessionManager();
        assertNull(sessionManager.getKey(), "Key should be null initially");

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();

        sessionManager.setKey(key);
        assertEquals(key, sessionManager.getKey(), "Set key should be retrievable");

        sessionManager.clearSession();
        assertNull(sessionManager.getKey(), "Key should be null after clearing session");
    }
}
