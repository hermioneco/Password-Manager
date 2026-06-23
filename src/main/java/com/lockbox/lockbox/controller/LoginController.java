package com.lockbox.lockbox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

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
    private void handleGoToRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/lockbox/lockbox/register-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 420, 520);
        scene.getStylesheets().add(getClass().getResource("/com/lockbox/lockbox/styles.css").toExternalForm());

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);

    }


}