/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */
import com.lockbox.model.User;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoServiceTest {

    private final CryptoService crypto = new CryptoService();
    private final User user1 = new User();
    private final User user2 = new User();

    @Test
    public void encryption() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        SecretKey key = crypto.generateKey("MotdePasse123!@#$", user1.getSalt());
        String hashed = crypto.encrypt("MotdePasse123!@#$", key);
        String unhashed = crypto.decrypt(hashed, key);

        assertEquals(unhashed, "MotdePasse123!@#$", "Pas de correspondance entre les mots de passe");
    }

    @Test
    void encryptedTextIsDifferentFromOriginal() throws Exception {

        SecretKey key = crypto.generateKey("motDePasseMaitre", user1.getSalt());

        String original = "monMotDePasseSecret123!";
        String encrypted = crypto.encrypt(original, key);

        // Le texte chiffré ne doit jamais être en clair
        assertNotEquals(original, encrypted);
    }
    @Test
    public void SameGeneratedKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
       
        SecretKey key1 = crypto.generateKey("MotdePasse123!@#$", user1.getSalt());
        SecretKey key2 = crypto.generateKey("MotdePasse123!@#$", user1.getSalt());

        assertEquals(key1, key2, "Les cles generees avec le meme mot de passe et le meme sel ne sont pas identiques");
    }
    @Test
    public void RandomIV() {
        byte[] iv1 = crypto.generateIV();
        byte[] iv2 = crypto.generateIV();

        assertArrayEquals(iv1, iv2, "Deux iv sont identiques");
    }

    @Test
    void encryptSameTextTwiceProducesDifferentResults() throws Exception {

        SecretKey key = crypto.generateKey("motDePasseMaitre", user1.getSalt());

        String original = "monMotDePasseSecret123!";
        String encrypted1 = crypto.encrypt(original, key);
        String encrypted2 = crypto.encrypt(original, key);

        // Deux chiffrements du même texte doivent donner des résultats différents
        // car l'IV est régénéré aléatoirement à chaque appel
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void decryptWithWrongKeyThrowsException() throws Exception {

        SecretKey correctKey = crypto.generateKey("bonMotDePasse", user1.getSalt());
        SecretKey wrongKey = crypto.generateKey("mauvaisMotDePasse", user1.getSalt());

        String encrypted = crypto.encrypt("secret", correctKey);

        // Déchiffrer avec la mauvaise clé doit lever une exception
        assertThrows(Exception.class, ()
                -> crypto.decrypt(encrypted, wrongKey));
    }

    @Test
    void differentSaltsProduceDifferentKeys() throws Exception {
        byte[] salt1 = user1.getSalt();
        byte[] salt2 = user2.getSalt();

        SecretKey key1 = crypto.generateKey("motDePasseMaitre", salt1);
        SecretKey key2 = crypto.generateKey("motDePasseMaitre", salt2);

        // Même mot de passe, sels différents → clés différentes
        // C'est le rôle du sel : éviter les rainbow tables
        assertFalse(java.util.Arrays.equals(
                key1.getEncoded(), key2.getEncoded()));
    }
}

