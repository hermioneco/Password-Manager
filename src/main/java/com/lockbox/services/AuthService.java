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

    private final UserRepository userRepo;
    private final CryptoService crypto;
    private final SessionManager sessionManager;

    @Autowired
    public AuthService(UserRepository userRepo, CryptoService crypto, SessionManager sessionManager) {
        this.userRepo = userRepo;
        this.crypto = crypto;
        this.sessionManager = sessionManager;
    }

    public void register(String email, char[] plainPassword) {
        try {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(crypto.hashingPassword(plainPassword));
            userRepo.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'inscription", e);
        }
    }

    public void register(String email, String plainPassword) {
        register(email, plainPassword != null ? plainPassword.toCharArray() : new char[0]);
    }

    public SecretKey login(String email, char[] enteredPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        User user = userRepo.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (crypto.comparePassword(user.getPasswordHash(), enteredPassword)) {
            SecretKey key = crypto.generateKey(enteredPassword, user.getSalt());
            if (sessionManager != null) {
                sessionManager.setKey(key);
            }
            return key;
        }
        throw new InvalidCredentialsException();
    }

    public SecretKey login(String email, String enteredPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return login(email, enteredPassword != null ? enteredPassword.toCharArray() : new char[0]);
    }
}

