/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.model;

/**
 *
 * @author ashie
 */


import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String label;      // nom du service (ex: "Netflix")
    private String username; 
    private String PlainPassword ;// login (ex: "martin@gmail.com")
    private String url;
    private String category;
    private String notes;

    // Données chiffrées stockées en base — JAMAIS les valeurs en clair
    @Lob
    private byte[] encryptedPassword;  // mot de passe chiffré en AES-256-GCM

    @Lob
    private byte[] iv;                 // vecteur d'initialisation unique

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; };
    
    public String getPlainPassword() { return PlainPassword; }
    public void setPlainPassword(String plainPassword) { this.PlainPassword = plainPassword; }

    public byte[] getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(byte[] encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public byte[] getIv() { return iv; }
    public void setIv(byte[] iv) { this.iv = iv; }
}