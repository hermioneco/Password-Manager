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
import java.util.Optional;
import javax.crypto.SecretKey;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ashie
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepo;
    private CryptoService crypto = new CryptoService();
     AuthService auth;

    @BeforeEach
    public void setUp() {
        User user = new User();
        auth = new AuthService(userRepo, crypto, new SessionManager());

    }
    @Test
    public void HashCreated() {

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        auth.register("mama@gmail.com", "MotdePasse123!@#$");
        verify(userRepo).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertTrue(user.getPasswordHash().startsWith("$argon2id$"), "Le hash du mot de passe ne commence pas par $argon2id$");
    }
    @Test
    public void ReturnKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        auth.register("mama@gmail.com", "MotdePasse123!@#$");
        verify(userRepo).save(userCaptor.capture());
        User user = userCaptor.getValue();
        when(userRepo.findByEmail("mama@gmail.com")).thenReturn(Optional.of(user));
        SecretKey key = auth.login("mama@gmail.com", "MotdePasse123!@#$");
        
        assertNotNull(key, "La cle retournee par le login est nulle");
    }
    @Test
    public void BadPassword() {
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        auth.register("mama@gmail.com", "MotdePasse123!@#$");
        verify(userRepo).save(userCaptor.capture());
        User user = userCaptor.getValue();
        when(userRepo.findByEmail("mama@gmail.com")).thenReturn(Optional.of(user));
        assertThrows(InvalidCredentialsException.class, () -> auth.login("mama@gmail.com", "mauvaisMdp"));
    }

}

