/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.lockbox.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.springframework.stereotype.Service;

/**
 *
 * @author ashie
 */
@Service
public class CryptoService {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH  = 12;

    // CHIFFRER : texte lisible → bytes illisibles
    public byte[] encrypt(String plaintext, SecretKey key)
        throws Exception {

        // 1. Générer un IV unique et aléatoire
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // 2. Configurer le chiffrement AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key,
            new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        // 3. Chiffrer
        byte[] ciphertext = cipher.doFinal(
            plaintext.getBytes(StandardCharsets.UTF_8));

        // 4. Stocker IV + ciphertext ensemble (on a besoin des deux)
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result,
            iv.length, ciphertext.length);
        return result;
    }

    // DÉCHIFFRER : bytes illisibles → texte lisible
    public String decrypt(byte[] data, SecretKey key)
        throws Exception {

        // 1. Extraire l'IV (les 12 premiers bytes)
        byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(
            data, GCM_IV_LENGTH, data.length);

        // 2. Déchiffrer
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key,
            new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}