/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ashie
 */
class PasswordUtilTest {

    private final PasswordUtil passwordUtil = new PasswordUtil();

    // ─── getScore ─────────────────────────────────────

    @Test
    void scoreIsZeroForNullPassword() {
        assertEquals(0, passwordUtil.getScore(null));
    }

    @Test
    void scoreIsZeroForEmptyPassword() {
        assertEquals(0, passwordUtil.getScore(""));
    }

    @Test
    void scoreIsZeroForVeryWeakPassword() {
        // "password" est dans tous les dictionnaires zxcvbn
        assertEquals(0, passwordUtil.getScore("password"));
    }

    @Test
    void scoreIsFourForStrongPassword() {
        // Long, aléatoire, aucun pattern connu
        assertEquals(4, passwordUtil.getScore("xK#9mP$qL2@nR7!w"));
    }

    @Test
    void scoreIsBetweenZeroAndFour() {
        // Propriété fondamentale — le score ne sort jamais de [0, 4]
        String[] passwords = {"abc", "password123", "MonMotDePasse1!", "xK#9mP$qL2@nR7!w"};
        for (String p : passwords) {
            int score = passwordUtil.getScore(p);
            assertTrue(score >= 0 && score <= 4,
                "Score hors limites pour : " + p);
        }
    }

    // ─── getFeedback ──────────────────────────────────

    @Test
    void feedbackIsNeverNull() {
        assertNotNull(passwordUtil.getFeedback("abc"));
        assertNotNull(passwordUtil.getFeedback("xK#9mP$qL2@nR7!w"));
    }

    @Test
    void feedbackForWeakPasswordIsNotTresFort() {
        String feedback = passwordUtil.getFeedback("password");
        assertNotEquals("Très fort", feedback);
    }

    @Test
    void feedbackForStrongPasswordIsTresFort() {
        String feedback = passwordUtil.getFeedback("xK#9mP$qL2@nR7!w");
        assertEquals("Très fort", feedback);
    }
}
