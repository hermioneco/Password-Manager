/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ClipboardService {

    // Délai avant effacement (en secondes)
    private static final int CLEAR_DELAY_SECONDS = 30;

    // Un seul thread d'arrière-plan suffit —
    // on n'a besoin que d'une tâche planifiée à la fois
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "clipboard-cleaner");
            t.setDaemon(true); // le thread s'arrête avec l'application
            return t;
        });

    // Garde une référence à la tâche en cours pour pouvoir l'annuler
    // si l'utilisateur copie un second mot de passe avant les 30 secondes
    private ScheduledFuture<?> pendingClear;

    // Callback optionnel — appelé quand l'effacement a eu lieu
    // (le Controller s'en sert pour fermer le toast)
    private Runnable onCleared;

    // ─────────────────────────────────────────────────
    // Copier dans le presse-papier + programmer l'effacement
    // ─────────────────────────────────────────────────
    public void copyWithAutoClear(String text, Runnable onCleared) {
        this.onCleared = onCleared;

        // 1. Copier le texte dans le presse-papier
        //    On est appelé depuis le thread JavaFX (clic sur un bouton)
        //    donc on peut accéder directement au Clipboard
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);

        // 2. Si une tâche d'effacement précédente est en attente,
        //    l'annuler — l'utilisateur vient de copier quelque chose
        //    de nouveau, le compteur repart de zéro
        if (pendingClear != null && !pendingClear.isDone()) {
            pendingClear.cancel(false);
            // false = ne pas interrompre si la tâche est déjà en cours
        }

        // 3. Programmer l'effacement dans 30 secondes
        pendingClear = scheduler.schedule(() -> {

            // Cette lambda s'exécute sur le thread d'arrière-plan
            // → on doit passer par Platform.runLater() pour toucher JavaFX
            Platform.runLater(() -> {
                Clipboard.getSystemClipboard().clear();

                // Notifier le Controller pour qu'il ferme le toast
                if (this.onCleared != null) {
                    this.onCleared.run();
                }
            });

        }, CLEAR_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    // Effacement immédiat — appelé à la déconnexion / verrouillage
    public void clearNow() {
        if (pendingClear != null && !pendingClear.isDone()) {
            pendingClear.cancel(false);
        }
        Platform.runLater(() ->
            Clipboard.getSystemClipboard().clear()
        );
    }
}
