/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;





class GeneratorServiceTest {

    private final GeneratorService generatorService = new GeneratorService();
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    // ─── Test 1 : longueur respectée ──────────────────
    @Test
    void generatedPasswordHasCorrectLength() {
        String password = generatorService.generate(16, true, false);
        assertEquals(16, password.length());
    }

    // ─── Test 2 : contient toujours au moins 1 majuscule ───
    @Test
    void generatedPasswordContainsUppercase() {
        String password = generatorService.generate(12, false, false);
        assertTrue(
            password.chars().anyMatch(Character::isUpperCase),
            "Doit contenir au moins une majuscule");
    }

    // ─── Test 3 : contient toujours au moins 1 minuscule ───
    @Test
    void generatedPasswordContainsLowercase() {
        String password = generatorService.generate(12, false, false);
        assertTrue(
            password.chars().anyMatch(Character::isLowerCase),
            "Doit contenir au moins une minuscule");
    }

    // ─── Test 4 : contient toujours au moins 1 chiffre ────
    @Test
    void generatedPasswordContainsDigit() {
        String password = generatorService.generate(12, false, false);
        assertTrue(
            password.chars().anyMatch(Character::isDigit),
            "Doit contenir au moins un chiffre");
    }

    // ─── Test 5 : useSymbols=true → contient 1 symbole ────
    @Test
    void generatedPasswordContainsSymbolWhenEnabled() {
        String password = generatorService.generate(12, true, false);
        boolean hasSymbol = password.chars()
            .anyMatch(c -> SYMBOLS.indexOf(c) >= 0);
        assertTrue(hasSymbol, "Doit contenir au moins un symbole");
    }

    // ─── Test 6 : useSymbols=false → jamais de symbole ────
    @Test
    void generatedPasswordNeverContainsSymbolWhenDisabled() {
        for (int i = 0; i < 20; i++) {
            String password = generatorService.generate(12, false, false);
            boolean hasSymbol = password.chars()
                .anyMatch(c -> SYMBOLS.indexOf(c) >= 0);
            assertFalse(hasSymbol,
                "Ne doit contenir aucun symbole — itération " + i);
        }
    }

    // ─── Test 7 : tous les types obligatoires + symboles ──
    @Test
    void generatedPasswordContainsAllTypes() {
        // Répéter 20 fois pour éliminer la chance
        for (int i = 0; i < 20; i++) {
            String password = generatorService.generate(20, true, false);

            boolean hasUpper  = password.chars()
                .anyMatch(Character::isUpperCase);
            boolean hasLower  = password.chars()
                .anyMatch(Character::isLowerCase);
            boolean hasDigit  = password.chars()
                .anyMatch(Character::isDigit);
            boolean hasSymbol = password.chars()
                .anyMatch(c -> SYMBOLS.indexOf(c) >= 0);

            assertTrue(hasUpper,  "Manque une majuscule — itération " + i);
            assertTrue(hasLower,  "Manque une minuscule — itération " + i);
            assertTrue(hasDigit,  "Manque un chiffre — itération " + i);
            assertTrue(hasSymbol, "Manque un symbole — itération " + i);
        }
    }

    // ─── Test 8 : unicité sur 1000 tirages ────────────
    @Test
    void generatedPasswordsAreUnique() {
        java.util.Set<String> passwords = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            passwords.add(generatorService.generate(16, true, false));
        }
        assertEquals(1000, passwords.size(),
            "Chaque mot de passe généré doit être unique");
    }

    // ─── Test 9 : sans ambigus, pas de 0/O/l/1/I ─────
    @Test
    void excludeAmbiguousRemovesConfusingChars() {
        String ambiguous = "0Oil1|";
        for (int i = 0; i < 100; i++) {
            String password = generatorService.generate(20, false, true);
            for (char c : ambiguous.toCharArray()) {
                assertFalse(password.contains(String.valueOf(c)),
                    "Le caractère '" + c + "' ne doit pas apparaître");
            }
        }
    }

    // ─── Test 10 : longueur invalide → exception ───────
    @Test
    void invalidLengthThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            generatorService.generate(2, true, false));
        assertThrows(IllegalArgumentException.class, () ->
            generatorService.generate(65, true, false));
    }
    
    @Test
void commonPasswordHasLowScore() {
    // "password" est dans le top-10 des mots de passe les plus courants
    int score = generatorService.getScore("password");
    assertTrue(score <= 1,
        "Un mot de passe commun doit avoir un score faible (0 ou 1)");
}

@Test
void strongPasswordHasHighScore() {
    // Un mot de passe généré aléatoirement doit être fort
    String generated = generatorService.generate(
        20,  true, false);
    int score = generatorService.getScore(generated);
    assertTrue(score >= 3,
        "Un mot de passe généré aléatoirement doit avoir un score ≥ 3");
}

@Test
void emptyPasswordReturnsZero() {
    assertEquals(0, generatorService.getScore(""));
    assertEquals(0, generatorService.getScore(null));
}
}