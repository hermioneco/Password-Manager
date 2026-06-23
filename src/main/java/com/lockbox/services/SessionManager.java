/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */


import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Arrays;

@Component
public class SessionManager {

    
    private SecretKey keyInMemory;

    public void setKey(SecretKey key) {
        this.keyInMemory = key;
    }

    public SecretKey getKey() {
        return keyInMemory;
    }

    public void clearSession() {
        if (keyInMemory != null) {
            byte[] encoded = keyInMemory.getEncoded();
            Arrays.fill(encoded, (byte) 0);
        }
        keyInMemory = null;
    }
}
