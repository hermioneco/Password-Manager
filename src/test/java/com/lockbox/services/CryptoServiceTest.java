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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    private final CryptoService cryptoService = new CryptoService();

    @Test
    void encryptDecryptRoundTrip() throws Exception {
        // Arrange — préparer les données du test
        String original = "mon-mot-de-passe-secret-123!";
        SecretKey key = generateTestKey();

        // Act — exécuter ce qu'on teste
        byte[] encrypted = cryptoService.encrypt(original, key);
        String decrypted = cryptoService.decrypt(encrypted, key);

        // Assert — vérifier le résultat
        assertEquals(original, decrypted,
            "Le texte déchiffré doit être identique à l'original");
    }

    @Test
    void encryptProducesUniqueIVs() throws Exception {
        // Arrange
        String text = "même texte à chiffrer deux fois";
        SecretKey key = generateTestKey();

        // Act — chiffrer DEUX FOIS le même texte
        byte[] encrypted1 = cryptoService.encrypt(text, key);
        byte[] encrypted2 = cryptoService.encrypt(text, key);

        // Assert
        assertFalse(Arrays.equals(encrypted1, encrypted2),
            "Deux chiffrements du même texte doivent produire des résultats différents (IV unique)");
    }

    // Méthode utilitaire — génère une clé AES valide juste pour le test,
    // sans passer par deriveKey() (pas besoin de mot de passe/sel ici,
    // on teste encrypt/decrypt isolément)
    private SecretKey generateTestKey() {
        byte[] keyBytes = new byte[32]; // 32 bytes = 256 bits
        new SecureRandom().nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
