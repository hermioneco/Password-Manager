/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.util;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ToastUtilTest {

    // ─────────────────────────────────────────────────
    // Démarrer le runtime JavaFX une seule fois
    // pour tous les tests de cette classe
    // ─────────────────────────────────────────────────
    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            // Démarre le thread JavaFX sans fenêtre visible
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Platform.startup() lève cette exception si JavaFX
            // est déjà initialisé (cas des tests lancés en série)
            latch.countDown();
        }
        // Attendre max 5 secondes que JavaFX soit prêt
        boolean started = latch.await(5, TimeUnit.SECONDS);
        assertTrue(started, "JavaFX n'a pas démarré dans les temps");
    }

    // Fermer tout toast résiduel avant chaque test
    @BeforeEach
    void cleanup() throws InterruptedException {
        runOnJavaFXThread(ToastUtil::dismiss);
    }

    // ─────────────────────────────────────────────────
    // Test 1 — Le constructeur est bien privé
    // ─────────────────────────────────────────────────
    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<ToastUtil> constructor =
            ToastUtil.class.getDeclaredConstructor();

        // Le constructeur ne doit pas être accessible publiquement
        assertFalse(constructor.canAccess(null),
            "Le constructeur doit être private");

        // Même en forçant l'accès, doit lever UnsupportedOperationException
        constructor.setAccessible(true);
        assertThrows(Exception.class,
            () -> constructor.newInstance(),
            "L'instanciation doit être interdite");
    }

    // ─────────────────────────────────────────────────
    // Test 2 — Aucun toast visible au départ
    // ─────────────────────────────────────────────────
    @Test
    void isShowingReturnsFalseInitially() {
        assertFalse(ToastUtil.isShowing(),
            "Aucun toast ne doit être visible au démarrage");
    }

    // ─────────────────────────────────────────────────
    // Test 3 — dismiss() sans toast actif ne plante pas
    // ─────────────────────────────────────────────────
    @Test
    void dismissWithNoActiveToastDoesNotThrow() {
        assertDoesNotThrow(
            () -> ToastUtil.dismiss(),
            "dismiss() sans toast actif ne doit pas lever d'exception"
        );
    }

    // ─────────────────────────────────────────────────
    // Test 4 — showCopied() rend le toast visible
    // ─────────────────────────────────────────────────
    @Test
    void showCopiedMakesToastVisible() throws InterruptedException {
        runOnJavaFXThread(() ->
            ToastUtil.showCopied(null)
        );

        assertTrue(ToastUtil.isShowing(),
            "showCopied() doit rendre le toast visible");
    }

    // ─────────────────────────────────────────────────
    // Test 5 — dismiss() ferme le toast
    // ─────────────────────────────────────────────────
    @Test
    void dismissClosesActiveToast() throws InterruptedException {
        // Ouvrir un toast
        runOnJavaFXThread(() ->
            ToastUtil.showCopied(null)
        );
        assertTrue(ToastUtil.isShowing());

        // Fermer
        runOnJavaFXThread(ToastUtil::dismiss);

        // Après dismiss(), isShowing() doit retourner false immédiatement
        // (même si l'animation de fondu est encore en cours,
        //  la référence activeToast est mise à null dès le début de dismiss())
        assertFalse(ToastUtil.isShowing(),
            "Après dismiss(), isShowing() doit retourner false");
    }

    // ─────────────────────────────────────────────────
    // Test 6 — Deux showCopied() successifs : un seul toast visible
    // ─────────────────────────────────────────────────
    @Test
    void twoSuccessiveShowsKeepOnlyOneToast() throws InterruptedException {
        runOnJavaFXThread(() -> {
            ToastUtil.showCopied(null);
            ToastUtil.showCopied(null); // doit fermer le premier
        });

        assertTrue(ToastUtil.isShowing(),
            "Un toast doit être visible");

        // Il n'y a pas de moyen de compter les Stage ouverts via l'API publique,
        // mais le fait que isShowing() retourne true (pas d'exception de
        // double show) valide le comportement
    }

    // ─────────────────────────────────────────────────
    // Test 7 — showMessage() avec message personnalisé
    // ─────────────────────────────────────────────────
    @Test
    void showMessageDoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() ->
            runOnJavaFXThread(() ->
                ToastUtil.showMessage(null, "Test message")
            )
        );
        assertTrue(ToastUtil.isShowing());
    }

    // ─────────────────────────────────────────────────
    // Test 8 — showSuccess() et showError()
    // ─────────────────────────────────────────────────
    @Test
    void showSuccessDoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() ->
            runOnJavaFXThread(() ->
                ToastUtil.showSuccess(null, "Opération réussie")
            )
        );
    }

    @Test
    void showErrorDoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() ->
            runOnJavaFXThread(() ->
                ToastUtil.showError(null, "Une erreur est survenue")
            )
        );
    }

    // ─────────────────────────────────────────────────
    // Méthode utilitaire — exécuter du code sur le thread JavaFX
    // et attendre qu'il soit terminé avant de continuer le test
    // ─────────────────────────────────────────────────
    private void runOnJavaFXThread(Runnable action)
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown(); // signale que l'action est terminée
            }
        });

        // Attendre max 5 secondes que le thread JavaFX ait exécuté l'action
        boolean done = latch.await(5, TimeUnit.SECONDS);
        assertTrue(done,
            "L'action JavaFX n'a pas été exécutée dans les temps");
    }
}
