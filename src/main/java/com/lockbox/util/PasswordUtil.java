/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.util;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import java.util.List;

/**
 *
 * @author ashie
 */
public class PasswordUtil {
    private static final Zxcvbn ZXCVBN = new Zxcvbn();
    
    public int getScore(String password) {
        if (password == null || password.isEmpty()) return 0;
        Strength strength = ZXCVBN.measure(password);
        return strength.getScore();
    }
    
    public String getStrengthLabel(String password) {
        switch (getScore(password)) {
            case 0: return "Très faible";
            case 1: return "Faible";
            case 2: return "Moyen";
            case 3: return "Fort";
            case 4: return "Très fort";
            default: return "Inconnu";
        }
    }
    public String getFeedback(String password) {
    Strength strength = ZXCVBN.measure(password);
    List<String> suggestions = strength.getFeedback().getSuggestions();
    if (!suggestions.isEmpty()) {
        return String.join(" ", suggestions);
    }
    // fallback sur le label si pas de suggestion
    return getStrengthLabel(password);
}
}
