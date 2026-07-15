/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

import com.lockbox.exceptions.ClosedSessionException;
import com.lockbox.model.Credential;
import com.lockbox.repository.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 *
 * @author ashie
 */
@Service
public class CredentialService {

    private SessionManager sessionManager;
    private CredentialRepository credentialRepo;
    private CryptoService cryptoService;

    public Credential save(Credential credential) throws Exception {
        // 1. récupérer la clé

        try {
            SecretKey key = sessionManager.getKey();
            // 2. vérifier qu'elle n'est pas null
            if (key == null) {
                throw new ClosedSessionException();
            }
            credential.setEncryptedPassword(cryptoService.encrypt(credential.getPlainPassword(), key).toString());
            credentialRepo.save(credential);

        } catch (Exception e) {
            throw e;
        } finally {
            credential.setPlainPassword("");
        }
        return credential;

    }

    public List<Credential> findAll(Long id) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        List<Credential> credentials = credentialRepo.findAllByUserId(id);
        for (Credential credential : credentials) {
            credential.setPlainPassword(cryptoService.decrypt(credential.getEncryptedPassword(), sessionManager.getKey()).toString());
        }
        return credentials;
    }

}
