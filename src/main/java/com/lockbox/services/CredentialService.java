/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */

import com.lockbox.model.Credential;
import com.lockbox.repository.CredentialRepository;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

@Service
public class CredentialService {

    // Les trois dépendances injectées par constructeur
    private final CredentialRepository credentialRepository;
    private final CryptoService cryptoService;
    private final SessionManager sessionManager;

    // Constructeur — Spring injecte les trois automatiquement
    public CredentialService(CredentialRepository credentialRepository,
                             CryptoService cryptoService,
                             SessionManager sessionManager) {
        this.credentialRepository = credentialRepository;
        this.cryptoService = cryptoService;
        this.sessionManager = sessionManager;
    }

    // ─────────────────────────────────────────────────
    // SAVE — chiffre le mot de passe AVANT l'INSERT
    // ─────────────────────────────────────────────────
    public Credential save(Credential credential) throws Exception {

        // Récupère la clé AES de la session active
        SecretKey key = sessionManager.getKey();
        if (key == null) {
            throw new IllegalStateException(
                "Session verrouillée — aucune clé disponible");
        }

        // Le mot de passe en clair est temporairement dans plainPassword
        // (un champ transient, pas stocké en DB — voir note ci-dessous)
        String plainPassword = credential.getPlainPassword();

        if (plainPassword != null && !plainPassword.isEmpty()) {
            // Chiffrer — retourne IV (12 bytes) + ciphertext concaténés
            byte[] encrypted = cryptoService.encrypt(plainPassword, key);
            credential.setEncryptedPassword(encrypted);

            // Effacer la valeur en clair de l'objet avant de sauvegarder
            credential.setPlainPassword(null);
        }

        // À ce stade : encryptedPassword = BLOB chiffré, plainPassword = null
        // Hibernate va faire un INSERT avec des données illisibles en DB
        return credentialRepository.save(credential);
    }

    // ─────────────────────────────────────────────────
    // FIND ALL — déchiffre APRÈS le SELECT
    // ─────────────────────────────────────────────────
    public List<Credential> findAllByUserId(Long userId) throws Exception {

        SecretKey key = sessionManager.getKey();
        if (key == null) {
            throw new IllegalStateException(
                "Session verrouillée — aucune clé disponible");
        }

        // SELECT * FROM credentials WHERE user_id = ?
        // Retourne des Credential avec encryptedPassword = BLOB illisible
        List<Credential> encrypted = credentialRepository
            .findAllByUserId(userId);

        // Déchiffrer chaque credential avant de le retourner au Controller
        List<Credential> decrypted = new ArrayList<>();

        for (Credential c : encrypted) {
            if (c.getEncryptedPassword() != null) {
                String plainPassword = cryptoService.decrypt(
                    c.getEncryptedPassword(), key);

                // On met le mot de passe en clair dans plainPassword
                // (champ transient — jamais persisté en DB)
                c.setPlainPassword(plainPassword);
            }
            decrypted.add(c);
        }

        return decrypted;
    }

    // ─────────────────────────────────────────────────
    // UPDATE — re-chiffre si le mot de passe a changé
    // ─────────────────────────────────────────────────
    public Credential update(Credential credential) throws Exception {

        SecretKey key = sessionManager.getKey();
        if (key == null) {
            throw new IllegalStateException("Session verrouillée");
        }

        String plainPassword = credential.getPlainPassword();

        if (plainPassword != null && !plainPassword.isEmpty()) {
            // Nouveau chiffrement — génère un NOUVEL IV automatiquement
            byte[] encrypted = cryptoService.encrypt(plainPassword, key);
            credential.setEncryptedPassword(encrypted);
            credential.setPlainPassword(null);
        }

        return credentialRepository.save(credential);
    }

    // ─────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────
    public void deleteById(Long id) {
        credentialRepository.deleteById(id);
    }
}
