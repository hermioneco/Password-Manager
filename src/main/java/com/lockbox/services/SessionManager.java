/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

import java.util.Arrays;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 *
 * @author ashie
 */
@Component
public class SessionManager {
    private SecretKey key ;
    
    public void setKey(SecretKey key) {
        this.key = key;
    }
    
    public SecretKey getKey() {
        return this.key ;
    }
    
    public void clearSession() {
        if(key != null) {
            Arrays.fill(key.getEncoded(), (byte) 0);
        key = null;
        }
        
    }
}
