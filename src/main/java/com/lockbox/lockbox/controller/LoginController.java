package com.lockbox.lockbox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        // TODO : brancher sur AuthService (Semaine 2)
        System.out.println("Tentative de connexion : " + email);
        errorLabel.setText("Service d'authentification pas encore connecté.");
    }

    @FXML
    private void handleGoToRegister() {
        // TODO : navigation vers RegisterView (J4)
        System.out.println("Navigation vers l'inscription...");
    }
}