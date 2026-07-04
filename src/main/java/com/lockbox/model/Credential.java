/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.model;

import javax.persistence.Transient;

/**
 *
 * @author ashie
 */

public class Credential {
    private String label;
    private String username;
    private String url;
    @Transient
    private String plainPassword;
    
}
 