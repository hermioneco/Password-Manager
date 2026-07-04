package com.lockbox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
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

        // TODO : brancher AuthService
        // Pour l'instant on navigue directement
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/lockbox/vault-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 860, 620);
        scene.getStylesheets().add(
                getClass().getResource("/com/lockbox/styles.css").toExternalForm()
        );
        VaultController vaultController = loader.getController();
        vaultController.setUserEmail(email);

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);
        stage.setWidth(860);
        stage.setHeight(620);
    }

    @FXML
    private void handleGoToRegister() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/lockbox/register-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 420, 520);
        scene.getStylesheets().add(getClass().getResource("/com/lockbox/styles.css").toExternalForm());

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(scene);

    }


}