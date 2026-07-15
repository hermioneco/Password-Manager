/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.repository;

/**
 *
 * @author ashie
 */


import com.lockbox.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialRepository
        extends JpaRepository<Credential, Long> {

    // Spring génère automatiquement :
    // SELECT * FROM credentials WHERE user_id = ?
    List<Credential> findAllByUserId(Long userId);

    // Pour supprimer tous les credentials d'un utilisateur
    void deleteAllByUserId(Long userId);
}
