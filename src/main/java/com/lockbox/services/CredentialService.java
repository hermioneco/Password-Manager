package com.lockbox.services;

import com.lockbox.exceptions.ClosedSessionException;
import com.lockbox.model.Credential;
import com.lockbox.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;

@Service
public class CredentialService {

    private final SessionManager sessionManager;
    private final CredentialRepository credentialRepo;
    private final CryptoService cryptoService;

    @Autowired
    public CredentialService(SessionManager sessionManager, CredentialRepository credentialRepo, CryptoService cryptoService) {
        this.sessionManager = sessionManager;
        this.credentialRepo = credentialRepo;
        this.cryptoService = cryptoService;
    }

    public Credential save(Credential credential) throws Exception {
        SecretKey key = sessionManager.getKey();
        if (key == null) {
            throw new ClosedSessionException();
        }

        if (credential.getPlainPassword() != null && !credential.getPlainPassword().isEmpty()) {
            String encryptedPass = cryptoService.encrypt(credential.getPlainPassword(), key);
            credential.setEncryptedPassword(encryptedPass);
        }

        if (credential.getLogin() != null && !credential.getLogin().isEmpty()) {
            String encryptedLogin = cryptoService.encrypt(credential.getLogin(), key);
            credential.setLogin(encryptedLogin);
        }

        if (credential.getNotes() != null && !credential.getNotes().isEmpty()) {
            String encryptedNotes = cryptoService.encrypt(credential.getNotes(), key);
            credential.setNotes(encryptedNotes);
        }

        return credentialRepo.save(credential);
    }

    public List<Credential> findAll(Long userId) throws Exception {
        SecretKey key = sessionManager.getKey();
        if (key == null) {
            throw new ClosedSessionException();
        }

        List<Credential> credentials = credentialRepo.findAllByUserId(userId);
        for (Credential c : credentials) {
            try {
                if (c.getEncryptedPassword() != null && !c.getEncryptedPassword().isEmpty()) {
                    c.setPlainPassword(cryptoService.decrypt(c.getEncryptedPassword(), key));
                }
                if (c.getLogin() != null && !c.getLogin().isEmpty()) {
                    c.setLogin(cryptoService.decrypt(c.getLogin(), key));
                }
                if (c.getNotes() != null && !c.getNotes().isEmpty()) {
                    c.setNotes(cryptoService.decrypt(c.getNotes(), key));
                }
            } catch (Exception e) {
                c.setPlainPassword("Error decrypting");
            }
        }
        return credentials;
    }

    public void deleteById(Long id) {
        credentialRepo.deleteById(id);
    }

    private boolean isBase64(String str) {
        if (str == null || str.length() % 4 != 0) return false;
        try {
            java.util.Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
