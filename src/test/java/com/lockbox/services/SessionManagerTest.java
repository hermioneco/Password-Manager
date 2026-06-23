/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */


import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @Test
    void getKeyReturnsNullAfterClearSession() {
        // Arrange
        SessionManager sessionManager = new SessionManager();
        SecretKey key = generateTestKey();
        sessionManager.setKey(key);

        // Vérification intermédiaire — s'assurer que setKey() a bien fonctionné
        assertNotNull(sessionManager.getKey());

        // Act
        sessionManager.clearSession();

        // Assert
        assertNull(sessionManager.getKey(),
            "getKey() doit retourner null après clearSession()");
    }

    private SecretKey generateTestKey() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
