/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 *
 * @author ashie
 */
//@Entity
@Entity          
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // colonne id, auto-incrémentée

    @Column(unique = true)
    private String email;       // colonne email, unique

    private String passwordHash; // hash Argon2id — JAMAIS le mdp en clair

    @Lob
    private byte[] salt;        // sel aléatoire pour la dérivation

    public Long getId() {
        return this.id;
    }
    public void setId(Long _id) {
        this.id = _id;
    }
    
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String _email) {
        this.email = _email;
    }
    
    public String getPasswordHash() {
        return this.passwordHash;
    }
    public void setPasswordHash(String _passwordHash) {
        this.passwordHash = _passwordHash;
    }
    
    public byte[] getSalt() {
        return this.salt;
    }
    public void setSalt(byte[] _salt) {
        this.salt = _salt;
    }
    
}