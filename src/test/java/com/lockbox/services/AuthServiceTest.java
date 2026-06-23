/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;



/**
 *
 * @author ashie
 */



import com.lockbox.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.lockbox.model.User;
import com.lockbox.repository.UserRepository;
import javax.crypto.SecretKey;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository mockUserRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mockUserRepository = mock(UserRepository.class);
        CryptoService cryptoService = new CryptoService();
        authService = new AuthService(cryptoService, mockUserRepository);
    }

    @Test
    void registerStoresArgon2idHash() {
        String email = "test@lockbox.com";
        authService.register(email, "MotDePasseSolide123!".toCharArray());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(captor.capture());

        assertTrue(captor.getValue().getPasswordHash().startsWith("$argon2id$"));
    }

    @Test
    void loginWithCorrectPasswordReturnsKey() throws Exception {
        String email = "test@lockbox.com";
        authService.register(email, "MotDePasseSolide123!".toCharArray());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(captor.capture());
        when(mockUserRepository.findByEmail(email))
            .thenReturn(Optional.of(captor.getValue()));

        SecretKey key = authService.login(email, "MotDePasseSolide123!".toCharArray());
        assertNotNull(key);
    }

    @Test
    void loginWithWrongPasswordThrowsException() {
        String email = "test@lockbox.com";
        authService.register(email, "BonMotDePasse123!".toCharArray());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(captor.capture());
        when(mockUserRepository.findByEmail(email))
            .thenReturn(Optional.of(captor.getValue()));

        assertThrows(InvalidCredentialsException.class, () ->
            authService.login(email, "MauvaisMotDePasse".toCharArray()));
    }
}