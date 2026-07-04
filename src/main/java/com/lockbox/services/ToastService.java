/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

/**
 *
 * @author ashie
 */


import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.springframework.stereotype.Service;

@Service
public class ToastService {

    // Référence au toast actif pour pouvoir le fermer
    private Stage currentToast;

    // ─────────────────────────────────────────────────
    // Afficher le toast "Copié · effacement dans 30s"
    // ─────────────────────────────────────────────────
    public void showCopiedToast(Stage ownerStage) {

        // Si un toast est déjà visible, le fermer d'abord
        if (currentToast != null && currentToast.isShowing()) {
            currentToast.close();
        }

        // Construire le contenu visuel
        Label icon    = new Label("📋");
        Label message = new Label("Copié · effacement dans 30s");
        message.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'DM Sans';"
        );

        HBox container = new HBox(10, icon, message);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(12, 20, 12, 16));
        container.setStyle(
            "-fx-background-color: #1e293b;" +   // fond sombre
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);"
        );

        // Scène avec fond transparent
        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT);

        // Stage sans décoration (pas de barre de titre)
        Stage toast = new Stage();
        toast.initStyle(StageStyle.TRANSPARENT);

        // Rattacher au owner pour que le toast
        // reste par-dessus la fenêtre principale
        if (ownerStage != null) {
            toast.initOwner(ownerStage);
        }

        toast.setScene(scene);
        toast.setAlwaysOnTop(true);

        // Positionner en bas à droite de la fenêtre owner
        if (ownerStage != null) {
            toast.setX(ownerStage.getX()
                + ownerStage.getWidth() - 320);
            toast.setY(ownerStage.getY()
                + ownerStage.getHeight() - 80);
        }

        // Apparition progressive — FadeTransition 0 → 1 en 300ms
        FadeTransition fadeIn = new FadeTransition(
            Duration.millis(300), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        toast.show();
        fadeIn.play();

        currentToast = toast;
    }

    // ─────────────────────────────────────────────────
    // Fermer le toast avec un fondu sortant
    // ─────────────────────────────────────────────────
    public void dismissToast() {
        if (currentToast == null || !currentToast.isShowing()) {
            return;
        }

        Stage toastToClose = currentToast;

        // Disparition progressive — FadeTransition 1 → 0 en 400ms
        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(400),
            toastToClose.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Fermer le Stage quand l'animation est terminée
        fadeOut.setOnFinished(e -> toastToClose.close());
        fadeOut.play();

        currentToast = null;
    }
}
