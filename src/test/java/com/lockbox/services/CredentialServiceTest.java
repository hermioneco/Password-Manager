package com.lockbox.services;

import com.lockbox.model.Credential;
import com.lockbox.model.User;
import com.lockbox.repository.CredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepo;

    private final CryptoService cryptoService = new CryptoService();
    private final SessionManager sessionManager = new SessionManager();
    private CredentialService credentialService;
    private SecretKey key;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        credentialService = new CredentialService(sessionManager, credentialRepo, cryptoService);
        user = new User();
        key = cryptoService.generateKey("MasterPassword123!", user.getSalt());
        sessionManager.setKey(key);
    }

    @Test
    public void testSaveEncryptsLoginAndPassword() throws Exception {
        Credential credential = new Credential(user, "Google", "myusername", null, "mypassword");
        credential.setNotes("some secret notes");

        when(credentialRepo.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Credential saved = credentialService.save(credential);

        assertNotNull(saved);
        assertNotEquals("myusername", saved.getLogin());
        assertNotEquals("mypassword", saved.getEncryptedPassword());
        assertNotEquals("some secret notes", saved.getNotes());

        // Verify we can decrypt them
        assertEquals("myusername", cryptoService.decrypt(saved.getLogin(), key));
        assertEquals("mypassword", cryptoService.decrypt(saved.getEncryptedPassword(), key));
        assertEquals("some secret notes", cryptoService.decrypt(saved.getNotes(), key));
    }

    @Test
    public void testFindAllDecryptsLoginAndPassword() throws Exception {
        Credential credential = new Credential(user, "Google", "myusername", null, "mypassword");
        credential.setNotes("some secret notes");

        when(credentialRepo.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Credential saved = credentialService.save(credential);

        when(credentialRepo.findAllByUserId(anyLong())).thenReturn(Collections.singletonList(saved));

        List<Credential> list = credentialService.findAll(1L);
        assertEquals(1, list.size());

        Credential retrieved = list.get(0);
        assertEquals("myusername", retrieved.getLogin());
        assertEquals("mypassword", retrieved.getPlainPassword());
        assertEquals("some secret notes", retrieved.getNotes());
    }
}
