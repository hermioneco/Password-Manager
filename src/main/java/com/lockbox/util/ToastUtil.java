package com.lockbox.util;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Utilitaire statique pour afficher des notifications toast dans l'interface.
 * Pas un @Service Spring — contient du code JavaFX, appartient à la couche UI.
 * Pas besoin d'instanciation : toutes les méthodes sont statiques.
 */
public class ToastUtil {

    // ─────────────────────────────────────────────────
    // Constantes visuelles
    // ─────────────────────────────────────────────────
    private static final int    FADE_IN_MS      = 300;
    private static final int    FADE_OUT_MS     = 400;
    private static final double TOAST_WIDTH_EST = 300.0;
    private static final double TOAST_HEIGHT_EST = 48.0;
    private static final double MARGIN           = 20.0;

    // Un seul toast visible à la fois — référence statique partagée
    private static Stage activeToast;

    // Constructeur privé — interdit l'instanciation
    // ToastUtil.show() pas new ToastUtil().show()
    private ToastUtil() {
        throw new UnsupportedOperationException(
            "ToastUtil est une classe utilitaire, pas d'instanciation");
    }

    // ─────────────────────────────────────────────────
    // API publique — méthodes statiques appelables
    // depuis n'importe quel Controller
    // ─────────────────────────────────────────────────

    /**
     * Toast spécialisé pour la copie presse-papier.
     * Message fixe : "📋  Copié · effacement dans 30s"
     */
    public static void showCopied(Stage owner) {
        show(owner, "📋  Copié · effacement dans 30s");
    }

    /**
     * Toast de succès — fond vert foncé
     */
    public static void showSuccess(Stage owner, String message) {
        show(owner, "✅  " + message, "#14532d");
    }

    /**
     * Toast d'erreur — fond rouge foncé
     */
    public static void showError(Stage owner, String message) {
        show(owner, "⚠️  " + message, "#7f1d1d");
    }

    /**
     * Toast générique avec message personnalisé
     */
    public static void showMessage(Stage owner, String message) {
        show(owner, message, "#1e293b");
    }

    /**
     * Ferme le toast actif avec une animation de fondu sortant.
     * Appelé par ClipboardService après les 30 secondes,
     * via Platform.runLater() — ne jamais appeler depuis un thread non-JavaFX.
     */
    public static void dismiss() {
        if (activeToast == null || !activeToast.isShowing()) {
            return;
        }

        Stage toClose = activeToast;
        activeToast = null; // libérer la référence immédiatement

        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(FADE_OUT_MS),
            toClose.getScene().getRoot()
        );
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        // Fermer le Stage une fois l'animation terminée
        fadeOut.setOnFinished(event -> toClose.close());
        fadeOut.play();
    }

    /**
     * Vérifie si un toast est actuellement visible.
     * Utile pour les tests et pour le Controller.
     */
    public static boolean isShowing() {
        return activeToast != null && activeToast.isShowing();
    }

    // ─────────────────────────────────────────────────
    // Implémentation privée
    // ─────────────────────────────────────────────────

    private static void show(Stage owner, String message) {
        show(owner, message, "#1e293b");
    }

    private static void show(Stage owner, String message, String backgroundColor) {

        // Fermer le toast existant immédiatement, sans animation,
        // pour éviter deux toasts superposés
        if (activeToast != null && activeToast.isShowing()) {
            activeToast.close();
            activeToast = null;
        }

        // 1. Construire les composants visuels
        HBox container = buildContainer(message, backgroundColor);

        // 2. Construire le Stage transparent
        Stage toast = buildStage(container, owner);

        // 3. Afficher avant de positionner
        //    (nécessaire : les dimensions réelles du Stage
        //     ne sont connues qu'après show())
        toast.show();

        // 4. Positionner en bas à droite du owner
        position(toast, owner);

        // 5. Garder la référence
        activeToast = toast;

        // 6. Animation d'apparition
        FadeTransition fadeIn = new FadeTransition(
            Duration.millis(FADE_IN_MS), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private static HBox buildContainer(String message, String bgColor) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;"
        );

        HBox container = new HBox(messageLabel);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(12, 20, 12, 16));
        container.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian," +
                "rgba(0,0,0,0.35), 14, 0, 0, 4);"
        );
        return container;
    }

    private static Stage buildStage(HBox container, Stage owner) {
        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT); // fond de scène transparent

        Stage toast = new Stage();
        toast.initStyle(StageStyle.TRANSPARENT); // pas de barre de titre
        toast.setAlwaysOnTop(true);
        toast.setResizable(false);

        // Rattacher au owner — le toast suit la fenêtre principale
        // et disparaît si elle est minimisée
        if (owner != null) {
            toast.initOwner(owner);
        }

        toast.setScene(scene);
        return toast;
    }

    private static void position(Stage toast, Stage owner) {
        if (owner == null) {
            // Pas de owner : centrer en bas de l'écran
            toast.setX(400);
            toast.setY(600);
            return;
        }

        // Après show(), toast.getWidth() et toast.getHeight()
        // retournent les vraies dimensions rendues
        double toastW = toast.getWidth();
        double toastH = toast.getHeight();

        // Utiliser les estimations si les dimensions ne sont pas encore calculées
        if (toastW == 0) toastW = TOAST_WIDTH_EST;
        if (toastH == 0) toastH = TOAST_HEIGHT_EST;

        // Coin bas-droit de la fenêtre owner, avec une marge
        double x = owner.getX() + owner.getWidth()  - toastW  - MARGIN;
        double y = owner.getY() + owner.getHeight() - toastH  - MARGIN;

        toast.setX(x);
        toast.setY(y);
    }
}