package com.lockbox.services;

import com.lockbox.model.User;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoServiceTest {

    private final CryptoService crypto = new CryptoService();
    private final User user1 = new User();
    private final User user2 = new User();

    @Test
    public void encryption() throws Exception {
        SecretKey key = crypto.generateKey("MotdePasse123!@#$", user1.getSalt());
        String hashed = crypto.encrypt("MotdePasse123!@#$", key);
        String unhashed = crypto.decrypt(hashed, key);

        assertEquals("MotdePasse123!@#$", unhashed, "Pas de correspondance entre les mots de passe");
    }

    @Test
    void encryptedTextIsDifferentFromOriginal() throws Exception {
        SecretKey key = crypto.generateKey("motDePasseMaitre", user1.getSalt());
        String original = "monMotDePasseSecret123!";
        String encrypted = crypto.encrypt(original, key);

        assertNotEquals(original, encrypted);
    }

    @Test
    public void SameGeneratedKey() throws Exception {
        SecretKey key1 = crypto.generateKey("MotdePasse123!@#$", user1.getSalt());
        SecretKey key2 = crypto.generateKey("MotdePasse123!@#$", user1.getSalt());

        assertArrayEquals(key1.getEncoded(), key2.getEncoded(), "Les cles generees avec le meme mot de passe et le meme sel ne sont pas identiques");
    }

    @Test
    public void RandomIV() {
        byte[] iv1 = crypto.generateIV();
        byte[] iv2 = crypto.generateIV();

        assertFalse(java.util.Arrays.equals(iv1, iv2), "Deux IV sont identiques");
    }

    @Test
    void encryptSameTextTwiceProducesDifferentResults() throws Exception {
        SecretKey key = crypto.generateKey("motDePasseMaitre", user1.getSalt());
        String original = "monMotDePasseSecret123!";
        String encrypted1 = crypto.encrypt(original, key);
        String encrypted2 = crypto.encrypt(original, key);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void decryptWithWrongKeyThrowsException() throws Exception {
        SecretKey correctKey = crypto.generateKey("bonMotDePasse", user1.getSalt());
        SecretKey wrongKey = crypto.generateKey("mauvaisMotDePasse", user1.getSalt());

        String encrypted = crypto.encrypt("secret", correctKey);

        assertThrows(Exception.class, () -> crypto.decrypt(encrypted, wrongKey));
    }

    @Test
    void differentSaltsProduceDifferentKeys() throws Exception {
        byte[] salt1 = user1.getSalt();
        byte[] salt2 = user2.getSalt();

        SecretKey key1 = crypto.generateKey("motDePasseMaitre", salt1);
        SecretKey key2 = crypto.generateKey("motDePasseMaitre", salt2);

        assertFalse(java.util.Arrays.equals(key1.getEncoded(), key2.getEncoded()));
    }
}

