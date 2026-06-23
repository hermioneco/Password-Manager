package com.lockbox.lockbox.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;

    @FXML
    public void initialize() {
        // Mise à jour de la barre de force en temps réel
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateStrengthBar(newVal);
        });
    }

    private void updateStrengthBar(String password) {
        int score = 0;
        if (password.length() >= 8)  score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*].*")) score++;

        switch (score) {
            case 0, 1 -> {
                strengthBar.setProgress(0.2);
                strengthBar.setStyle("-fx-accent: #f38ba8;");
                strengthLabel.setText("Très faible");
            }
            case 2 -> {
                strengthBar.setProgress(0.4);
                strengthBar.setStyle("-fx-accent: #fab387;");
                strengthLabel.setText("Faible");
            }
            case 3 -> {
                strengthBar.setProgress(0.6);
                strengthBar.setStyle("-fx-accent: #f9e2af;");
                strengthLabel.setText("Moyen");
            }
            case 4 -> {
                strengthBar.setProgress(0.8);
                strengthBar.setStyle("-fx-accent: #a6e3a1;");
                strengthLabel.setText("Fort");
            }
            default -> {
                strengthBar.setProgress(1.0);
                strengthBar.setStyle("-fx-accent: #a6e3a1;");
                strengthLabel.setText("Très fort");
            }
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }
        if (!email.contains("@")) {
            errorLabel.setText("Email invalide.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }
        if (password.length() < 8) {
            errorLabel.setText("Mot de passe trop court (8 caractères min).");
            return;
        }

        // TODO : brancher sur AuthService (Semaine 2)
        System.out.println("Inscription : " + email);
        errorLabel.setText("Service pas encore connecté.");
    }

    @FXML
    private void handleGoToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/lockbox/lockbox/login-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 420, 520);
        scene.getStylesheets().add(getClass().getResource("/com/lockbox/lockbox/styles.css").toExternalForm());

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);

    }
}