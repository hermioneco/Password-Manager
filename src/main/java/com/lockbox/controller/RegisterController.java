package com.lockbox.controller;

import com.lockbox.LockBoxApp;
import com.lockbox.services.AuthService;
import com.lockbox.services.GeneratorService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;

    private final AuthService authService;
    private final GeneratorService generatorService;

    @Autowired
    public RegisterController(AuthService authService, GeneratorService generatorService) {
        this.authService = authService;
        this.generatorService = generatorService;
    }

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateStrengthBar(newVal));
    }

    private void updateStrengthBar(String password) {
        int score = generatorService != null ? generatorService.getScore(password) : 0;
        switch (score) {
            case 0, 1 -> {
                strengthBar.setProgress(0.25);
                strengthBar.setStyle("-fx-accent: #f38ba8;");
                strengthLabel.setText("Très faible");
            }
            case 2 -> {
                strengthBar.setProgress(0.50);
                strengthBar.setStyle("-fx-accent: #fab387;");
                strengthLabel.setText("Moyen");
            }
            case 3 -> {
                strengthBar.setProgress(0.75);
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
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        String confirm = confirmField.getText() != null ? confirmField.getText() : "";

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

        try {
            authService.register(email, password);
            handleGoToLogin();
        } catch (Exception e) {
            errorLabel.setText("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockbox/login-view.fxml"));
        loader.setControllerFactory(type -> {
            if (LockBoxApp.getSpringContext() != null && LockBoxApp.getSpringContext().getBeanNamesForType(type).length > 0) {
                return LockBoxApp.getSpringContext().getBean(type);
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        Scene scene = new Scene(loader.load(), 420, 520);
        scene.getStylesheets().add(getClass().getResource("/com/lockbox/styles.css").toExternalForm());

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);
    }
}