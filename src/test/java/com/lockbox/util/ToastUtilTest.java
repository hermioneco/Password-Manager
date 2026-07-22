/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.util;

//import javafx.application.Platform;
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
            javafx.application.Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        boolean started = latch.await(5, TimeUnit.SECONDS);
        assertTrue(started, "JavaFX n'a pas démarré dans les temps");
    }

    @BeforeEach
    void cleanup() throws InterruptedException {
        runOnJavaFXThread(ToastUtil::dismiss);
    }

    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<ToastUtil> constructor = ToastUtil.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null), "Le constructeur doit être private");
        constructor.setAccessible(true);
        assertThrows(Exception.class, () -> constructor.newInstance(), "L'instanciation doit être interdite");
    }

    @Test
    void isShowingReturnsFalseInitially() {
        assertFalse(ToastUtil.isShowing(), "Aucun toast ne doit être visible au démarrage");
    }

    @Test
    void dismissWithNoActiveToastDoesNotThrow() {
        assertDoesNotThrow(() -> ToastUtil.dismiss(), "dismiss() sans toast actif ne doit pas lever d'exception");
    }

    @Test
    void showCopiedMakesToastVisible() throws InterruptedException {
        runOnJavaFXThread(() -> ToastUtil.showCopied(null));
        assertTrue(ToastUtil.isShowing(), "showCopied() doit rendre le toast visible");
    }

    @Test
    void dismissClosesActiveToast() throws InterruptedException {
        runOnJavaFXThread(() -> ToastUtil.showCopied(null));
        assertTrue(ToastUtil.isShowing());

        runOnJavaFXThread(ToastUtil::dismiss);
        assertFalse(ToastUtil.isShowing(), "Après dismiss(), isShowing() doit retourner false");
    }

    @Test
    void twoSuccessiveShowsKeepOnlyOneToast() throws InterruptedException {
        runOnJavaFXThread(() -> {
            ToastUtil.showCopied(null);
            ToastUtil.showCopied(null);
        });
        assertTrue(ToastUtil.isShowing(), "Un toast doit être visible");
    }

    @Test
    void showMessageDoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() -> runOnJavaFXThread(() -> ToastUtil.showMessage(null, "Test message")));
        assertTrue(ToastUtil.isShowing());
    }

    @Test
    void showSuccessDoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() -> runOnJavaFXThread(() -> ToastUtil.showSuccess(null, "Opération réussie")));
    }

    @Test
    void showErrorDoesNotThrow() throws InterruptedException {
        assertDoesNotThrow(() -> runOnJavaFXThread(() -> ToastUtil.showError(null, "Une erreur est survenue")));
    }

    private void runOnJavaFXThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        boolean done = latch.await(5, TimeUnit.SECONDS);
        assertTrue(done, "L'action JavaFX n'a pas été exécutée dans les temps");
    }
}
