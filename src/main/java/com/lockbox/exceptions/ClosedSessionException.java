/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.exceptions;

/**
 *
 * @author ashie
 */
public class ClosedSessionException extends RuntimeException {
    public ClosedSessionException() {
        super("Votre session a expire. Veuillez vous reconnecter");
    }
}
