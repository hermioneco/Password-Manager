/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */


import org.springframework.stereotype.Service;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GeneratorService {

    // ─────────────────────────────────────────────────
    // Les 4 charsets disponibles
    // ─────────────────────────────────────────────────
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    // Version "sans ambigus" — retire les caractères visuellement confondables
    // (ex: 0 et O, l et 1 et I, | et l)
    private static final String UPPERCASE_NO_AMBIGUOUS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE_NO_AMBIGUOUS = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS_NO_AMBIGUOUS    = "23456789";

    // SecureRandom est cryptographiquement sûr (contrairement à Math.random())
    // Il est déclaré ici en attribut de classe pour ne pas en créer un nouveau
    // à chaque appel — c'est coûteux en entropie système
    private final SecureRandom secureRandom = new SecureRandom();
    private final Zxcvbn zxcvbn = new Zxcvbn();

    // ─────────────────────────────────────────────────
    // Méthode principale
    // ─────────────────────────────────────────────────
    public String generate(int length,
                       boolean useSymbols,
                       boolean excludeAmbiguous) {

    // Étape 1 — Vérification de base
    if (length < 8 || length > 64) {
        throw new IllegalArgumentException(
            "La longueur doit être entre 8 et 64 caractères");
    }

    // Étape 2 — Construire le pool global de caractères disponibles
    StringBuilder poolBuilder = new StringBuilder();
    poolBuilder.append(excludeAmbiguous ? UPPERCASE_NO_AMBIGUOUS : UPPERCASE);
    poolBuilder.append(excludeAmbiguous ? LOWERCASE_NO_AMBIGUOUS : LOWERCASE);
    poolBuilder.append(excludeAmbiguous ? DIGITS_NO_AMBIGUOUS : DIGITS);
    if (useSymbols) {
        poolBuilder.append(SYMBOLS);
    }
    String pool = poolBuilder.toString();

    // Étape 3 — Garantir au moins 1 caractère de chaque type obligatoire
    List<Character> password = new ArrayList<>();
    password.add(randomCharFrom(excludeAmbiguous ? UPPERCASE_NO_AMBIGUOUS : UPPERCASE));
    password.add(randomCharFrom(excludeAmbiguous ? LOWERCASE_NO_AMBIGUOUS : LOWERCASE));
    password.add(randomCharFrom(excludeAmbiguous ? DIGITS_NO_AMBIGUOUS : DIGITS));
    if (useSymbols) {
        password.add(randomCharFrom(SYMBOLS));
    }

    // Étape 4 — Compléter jusqu'à la longueur demandée
    int remaining = length - password.size();
    for (int i = 0; i < remaining; i++) {
        password.add(randomCharFrom(pool));
    }

    // Étape 5 — Mélanger
    Collections.shuffle(password, secureRandom);

    // Étape 6 — Convertir en String
    StringBuilder result = new StringBuilder();
    for (char c : password) {
        result.append(c);
    }
    return result.toString();
}

    // ─────────────────────────────────────────────────
    // Méthode utilitaire privée
    // ─────────────────────────────────────────────────

    // Pioche un caractère aléatoire dans une chaîne donnée
    private char randomCharFrom(String charset) {
        int index = secureRandom.nextInt(charset.length());
        return charset.charAt(index);
    }
    
    public int getScore(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        Strength strength = zxcvbn.measure(password);
        return strength.getScore();
    }
    
    public String getFeedback(String password) {
        if (password == null || password.isEmpty()) {
            return "Entrez un mot de passe";
        }
        Strength strength = zxcvbn.measure(password);

        // zxcvbn donne un "warning" si le mdp est trop prévisible,
        // et des "suggestions" pour l'améliorer
        String warning = strength.getFeedback().getWarning();
        if (warning != null && !warning.isEmpty()) {
            return warning;
        }

        // Sinon on retourne un label basé sur le score
        return switch (strength.getScore()) {
            case 0 -> "Très faible — à éviter absolument";
            case 1 -> "Faible — trop prévisible";
            case 2 -> "Moyen — peut être amélioré";
            case 3 -> "Fort — bon mot de passe";
            case 4 -> "Très fort — excellent";
            default -> "";
        };
    }    
}
