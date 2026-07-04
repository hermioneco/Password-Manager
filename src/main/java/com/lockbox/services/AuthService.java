/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

import com.lockbox.exceptions.InvalidCredentialsException;
import com.lockbox.model.User;
import com.lockbox.repository.UserRepository;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    
    private  final UserRepository userRepo;
    private  final CryptoService crypto ;
    
    public AuthService(UserRepository userRepo, CryptoService crypto) {
        this.userRepo = userRepo;
        this.crypto = crypto;
    }
    
    public  void register(String email, char[] PlainPassword) {
        try {
            User user = new User();
         user.setEmail(email);
         user.setPasswordHash(crypto.hashingPassword(PlainPassword));
         userRepo.save(user);
        }catch (Exception e){
            throw new RuntimeException("Échec de l'inscription", e);
        }
        finally{
         PlainPassword = null;  }
    }
    
    public SecretKey login(String email, char[] EnteredPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        User user = userRepo.findByEmail(email).orElseThrow(() ->new InvalidCredentialsException());
        
        try {
            if(crypto.comparePassword(user.getPasswordHash(), EnteredPassword)) {
                return crypto.generateKey(EnteredPassword, user.getSalt());
            }
        }catch(InvalidCredentialsException e){
            throw e;
        }
        finally{
        EnteredPassword = null;
        }
       throw new InvalidCredentialsException();
    }
}

