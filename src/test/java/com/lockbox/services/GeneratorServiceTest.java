/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeneratorServiceTest {

    private final GeneratorService generatorService = new GeneratorService();

    // ─── longueur ─────────────────────────────────────

    @Test
    void generatedPasswordHasExactRequestedLength() {
        // Ce test échoue avec le code actuel — prouve le bug de longueur
        String password = generatorService.generate(false, 12);
        assertEquals(12, password.length());
    }

    @Test
    void generatedPasswordHasExactLengthWithSymbols() {
        String password = generatorService.generate(true, 16);
        assertEquals(16, password.length());
    }

    // ─── types garantis ───────────────────────────────

    @Test
    void generatedPasswordAlwaysContainsUppercase() {
        for (int i = 0; i < 20; i++) {
            String password = generatorService.generate(false, 12);
            assertTrue(
                password.chars().anyMatch(Character::isUpperCase),
                "Manque une majuscule — itération " + i);
        }
    }

    @Test
    void generatedPasswordAlwaysContainsLowercase() {
        for (int i = 0; i < 20; i++) {
            String password = generatorService.generate(false, 12);
            assertTrue(
                password.chars().anyMatch(Character::isLowerCase),
                "Manque une minuscule — itération " + i);
        }
    }

    @Test
    void generatedPasswordAlwaysContainsDigit() {
        for (int i = 0; i < 20; i++) {
            String password = generatorService.generate(false, 12);
            assertTrue(
                password.chars().anyMatch(Character::isDigit),
                "Manque un chiffre — itération " + i);
        }
    }

    // ─── symboles ─────────────────────────────────────

    @Test
    void generatedPasswordContainsSymbolWhenEnabled() {
        String symbols = "!@#$%^&*{}[]|/<>()";
        for (int i = 0; i < 20; i++) {
            String password = generatorService.generate(true, 12);
            assertTrue(
                password.chars().anyMatch(c -> symbols.indexOf(c) >= 0),
                "Manque un symbole — itération " + i);
        }
    }

    @Test
    void generatedPasswordNeverContainsSymbolWhenDisabled() {
        // Ce test échoue avec le code actuel — prouve le bug du pool
        String symbols = "!@#$%^&*{}[]|/<>()";
        for (int i = 0; i < 50; i++) {
            String password = generatorService.generate(false, 12);
            assertFalse(
                password.chars().anyMatch(c -> symbols.indexOf(c) >= 0),
                "Symbole trouvé alors que désactivé — itération " + i);
        }
    }

    // ─── unicité ──────────────────────────────────────

    @Test
    void generatedPasswordsAreUnique() {
        Set<String> passwords = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            passwords.add(generatorService.generate(true, 16));
        }
        assertEquals(1000, passwords.size(),
            "Des doublons ont été générés");
    }
}