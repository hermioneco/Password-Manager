package com.lockbox.controller;

import com.lockbox.LockBoxApp;
import com.lockbox.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService;

    @Autowired
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            authService.login(email, password);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockbox/vault-view.fxml"));
            loader.setControllerFactory(type -> {
                if (LockBoxApp.getSpringContext() != null && LockBoxApp.getSpringContext().getBeanNamesForType(type).length > 0) {
                    return LockBoxApp.getSpringContext().getBean(type);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Scene scene = new Scene(loader.load(), 860, 620);
            scene.getStylesheets().add(getClass().getResource("/com/lockbox/styles.css").toExternalForm());

            VaultController vaultController = loader.getController();
            if (vaultController != null) {
                vaultController.setUserEmail(email);
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(860);
            stage.setHeight(620);
            stage.centerOnScreen();
        } catch (Exception e) {
            errorLabel.setText("Email ou mot de passe incorrect.");
        }
    }

    @FXML
    private void handleGoToRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockbox/register-view.fxml"));
        loader.setControllerFactory(type -> {
            if (LockBoxApp.getSpringContext() != null && LockBoxApp.getSpringContext().getBeanNamesForType(type).length > 0) {
                return LockBoxApp.getSpringContext().getBean(type);
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(loader.load(), 420, 580);
        scene.getStylesheets().add(getClass().getResource("/com/lockbox/styles.css").toExternalForm());

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);
    }
}