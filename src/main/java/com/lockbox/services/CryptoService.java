/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    public String encrypt(String plainText, SecretKey key) throws Exception {
        if (plainText == null) plainText = "";
        byte[] iv = generateIV();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String cipherText, SecretKey key) throws Exception {
        if (cipherText == null || cipherText.isEmpty()) return "";
        byte[] decoded = Base64.getDecoder().decode(cipherText);

        byte[] iv = new byte[12];
        System.arraycopy(decoded, 0, iv, 0, iv.length);
        byte[] encryptedText = new byte[decoded.length - iv.length];
        System.arraycopy(decoded, iv.length, encryptedText, 0, encryptedText.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedText);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public SecretKey generateKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return secret;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    public SecretKey generateKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return generateKey(password != null ? password.toCharArray() : new char[0], salt);
    }

    public String hashingPassword(char[] plainPassword) {
        int ITERATIONS = 3;
        int MEMORY_KB = 65536;
        int PARALLELISM = 4;

        char[] password = plainPassword != null ? plainPassword : new char[0];
        String hashedPassword = argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        argon2.wipeArray(password);
        return hashedPassword;
    }

    public String hashingPassword(String plainPassword) {
        return hashingPassword(plainPassword != null ? plainPassword.toCharArray() : new char[0]);
    }

    public boolean comparePassword(String hashedPassword, char[] enteredPassword) {
        if (hashedPassword == null || enteredPassword == null) return false;
        return argon2.verify(hashedPassword, enteredPassword);
    }

    public boolean comparePassword(String hashedPassword, String enteredPassword) {
        if (hashedPassword == null || enteredPassword == null) return false;
        return comparePassword(hashedPassword, enteredPassword.toCharArray());
    }

    public byte[] generateIV() {
        byte[] iv = new byte[12];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }
}
