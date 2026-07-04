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
    public  byte[] encrypt(String Password, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] iv = generateIV();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] ciphertext = cipher.doFinal(Password.getBytes());

        byte[] combinedIvAndCipherText = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combinedIvAndCipherText, 0, iv.length);
        System.arraycopy(ciphertext, 0, combinedIvAndCipherText, iv.length, ciphertext.length);

        return Base64.getEncoder().encode(combinedIvAndCipherText);

    }

    public  byte[] decrypt(String cipherText, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] decodedCipherText = Base64.getDecoder().decode(cipherText);

        // Extract IV and encrypted text
        byte[] iv = new byte[12];
        System.arraycopy(decodedCipherText, 0, iv, 0, iv.length);
        byte[] encryptedText = new byte[decodedCipherText.length - iv.length];
        System.arraycopy(decodedCipherText, iv.length, encryptedText, 0, encryptedText.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedText);

        return decryptedBytes;

    }

    public  SecretKey generateKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
        }catch(Exception e){
            throw e;
        }finally{
           Arrays.fill(password, '\0');
        }
            
    }

    public  String hashingPassword(char[] PlainPassword) {

        int ITERATIONS = 3;
        int MEMORY_KB = 65536; 
        int PARALLELISM = 4;
        
         char[] password = PlainPassword;
        
        String HashedPassword = argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        argon2.wipeArray(password);
        return HashedPassword;

    }
    
    public boolean comparePassword(String HashedPassword, char[] EntredPassword) {
        return argon2.verify(HashedPassword,EntredPassword);
        
    }
    
    public byte[] generateIV() {
        byte [] iv = new byte[12];
        SecureRandom secureRadom = new SecureRandom();
        secureRadom.nextBytes(iv);
        return iv;
    }
    
    
}
