/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

import com.lockbox.exceptions.InvalidCredentialsException;
import com.lockbox.model.User;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lockbox.repository.UserRepository;

/**
 *
 * @author ashie
 */
@Service
public class AuthService {

    

    

    private final Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id);

    
    private final UserRepository userRepo;
    
    private final CryptoService cryptoService;

    public AuthService(CryptoService cryptoService, UserRepository userRepository) {
        this.cryptoService = cryptoService;
        this.userRepo = userRepository;
    }
    // INSCRIPTION — crée un compte
    public void register(String email, char[] password) {
        // Hash Argon2id — le mot de passe est haché, jamais stocké
        String hash = argon2.hash(
                3, // timeCost : nb d'itérations
                65536, // memoryCost : 64 MB de RAM utilisés (résiste aux GPU)
                4, // parallelism : nb de threads
                password
        );
        // Effacer le mot de passe de la mémoire immédiatement
        Arrays.fill(password, '\0');

        User user = new User();
        // register() — UNE SEULE FOIS, à la création du compte
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);   // génère le sel
        user.setSalt(salt);                   // le stocke en DB, pour toujours
        user.setEmail(email);
        user.setPasswordHash(hash); // HASH seulement, jamais le mdp
        userRepo.save(user);
    }

    // CONNEXION — vérifie le mot de passe
    public SecretKey login(String email, char[] password) throws Exception {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // Vérification sans jamais "déhacher" le mot de passe
        boolean valid = argon2.verify(
                user.getPasswordHash(), password);
        Arrays.fill(password, '\0'); // Effacer de la mémoire

        if (!valid) {
            throw new InvalidCredentialsException("Email ou mot de passe incorrect");
        }

        // Dériver la clé AES à partir du mot de passe
        return cryptoService.deriveKey(password, user.getSalt());
    }

    

    
}
