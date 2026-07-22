package com.lockbox.controller;

import com.lockbox.LockBoxApp;
import com.lockbox.model.Credential;
import com.lockbox.model.User;
import com.lockbox.repository.UserRepository;
import com.lockbox.services.*;
import com.lockbox.util.ToastUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class VaultController {

    @FXML private TextField searchField;
    @FXML private Button settingsButton;
    @FXML private Label userEmailLabel;
    @FXML private Label entryCountLabel;
    @FXML private Button addButton;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    @FXML private TableView<Credential> passwordTable;
    @FXML private TableColumn<Credential, String> colTitle;
    @FXML private TableColumn<Credential, String> colUsername;
    @FXML private TableColumn<Credential, String> colUrl;
    @FXML private TableColumn<Credential, String> colPassword;
    @FXML private TableColumn<Credential, String> colActions;

    @FXML private Button logoutButton;

    private final CredentialService credentialService;
    private final GeneratorService generatorService;
    private final ClipboardService clipboardService;
    private final ToastService toastService;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    private final ObservableList<Credential> entries = FXCollections.observableArrayList();
    private FilteredList<Credential> filteredEntries;
    private User currentUser;

    @Autowired
    public VaultController(CredentialService credentialService,
                           GeneratorService generatorService,
                           ClipboardService clipboardService,
                           ToastService toastService,
                           SessionManager sessionManager,
                           UserRepository userRepository) {
        this.credentialService = credentialService;
        this.generatorService = generatorService;
        this.clipboardService = clipboardService;
        this.toastService = toastService;
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
    }

    public void setUserEmail(String email) {
        if (email != null && !email.isEmpty()) {
            userEmailLabel.setText(email);
            userRepository.findByEmail(email).ifPresent(user -> {
                this.currentUser = user;
                loadUserData();
            });
        }
    }

    private void loadUserData() {
        if (currentUser == null) return;
        try {
            entries.clear();
            entries.addAll(credentialService.findAll(currentUser.getId()));
            updateCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getServiceName() != null ? data.getValue().getServiceName() : ""));

        colUsername.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLogin() != null ? data.getValue().getLogin() : ""));

        colUrl.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUrl() != null ? data.getValue().getUrl() : ""));

        colPassword.setCellValueFactory(data -> new SimpleStringProperty("••••••••"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnCopy   = new Button("📋");
            private final Button btnView   = new Button("👁");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(4, btnCopy, btnView, btnDelete);

            {
                String btnStyle = "-fx-background-color: #313244; -fx-text-fill: #cdd6f4;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 8;";
                btnCopy.setStyle(btnStyle);
                btnView.setStyle(btnStyle);
                btnDelete.setStyle("-fx-background-color: #3d1a1a; -fx-text-fill: #f38ba8;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 8;");

                btnCopy.setOnAction(e -> handleCopy(getTableView().getItems().get(getIndex())));
                btnView.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        filteredEntries = new FilteredList<>(entries, p -> true);
        passwordTable.setItems(filteredEntries);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredEntries.setPredicate(credential -> {
                if (newVal == null || newVal.isBlank()) return true;
                String filter = newVal.toLowerCase();
                if (credential.getServiceName() != null && credential.getServiceName().toLowerCase().contains(filter)) return true;
                if (credential.getLogin() != null && credential.getLogin().toLowerCase().contains(filter)) return true;
                return credential.getUrl() != null && credential.getUrl().toLowerCase().contains(filter);
            });
            updateCount();
        });
    }

    private void updateCount() {
        entryCountLabel.setText(filteredEntries.size() + " entrée(s)");
    }

    private void handleCopy(Credential credential) {
        if (credential == null) return;
        String pass = credential.getPlainPassword();
        if (pass != null && !pass.isEmpty()) {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            clipboardService.copyWithAutoClear(pass, () -> ToastUtil.dismiss());
            ToastUtil.showCopied(stage);
        }
    }

    private void handleView(Credential credential) {
        if (credential == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'identifiant");
        alert.setHeaderText(credential.getServiceName());
        alert.setContentText("Service: " + credential.getServiceName() +
                "\nURL: " + credential.getUrl() +
                "\nIdentifiant: " + credential.getLogin() +
                "\nMot de passe: " + (credential.getPlainPassword() != null ? credential.getPlainPassword() : "[Chiffré]"));
        alert.showAndWait();
    }

    private void handleDelete(Credential credential) {
        if (credential == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("Supprimer " + credential.getServiceName() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (credential.getId() != null) {
                    credentialService.deleteById(credential.getId());
                }
                entries.remove(credential);
                updateCount();
            }
        });
    }

    @FXML
    private void handleAdd() {
        if (currentUser == null) return;

        Dialog<Credential> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un identifiant");
        dialog.setHeaderText("Saisissez les informations de l'identifiant");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serviceField = new TextField();
        serviceField.setPromptText("Nom du service (ex: Gmail)");
        TextField urlField = new TextField();
        urlField.setPromptText("https://...");
        TextField loginField = new TextField();
        loginField.setPromptText("Email / Pseudo");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        Button btnGen = new Button("🔑 Générer");
        btnGen.setOnAction(e -> passwordField.setText(generatorService.generate(true, 16)));
        HBox passBox = new HBox(5, passwordField, btnGen);

        grid.add(new Label("Service:"), 0, 0);
        grid.add(serviceField, 1, 0);
        grid.add(new Label("URL:"), 0, 1);
        grid.add(urlField, 1, 1);
        grid.add(new Label("Identifiant:"), 0, 2);
        grid.add(loginField, 1, 2);
        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(passBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Credential c = new Credential(
                        currentUser,
                        serviceField.getText().trim(),
                        loginField.getText().trim(),
                        "",
                        passwordField.getText()
                );
                c.setUrl(urlField.getText().trim());
                return c;
            }
            return null;
        });

        Optional<Credential> result = dialog.showAndWait();
        result.ifPresent(cred -> {
            try {
                Credential saved = credentialService.save(cred);
                saved.setPlainPassword(cred.getPlainPassword());
                entries.add(saved);
                updateCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleGenerate() {
        String generated = generatorService.generate(true, 16);
        int score = generatorService.getScore(generated);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Générateur de mot de passe");
        alert.setHeaderText("Mot de passe fort généré");
        alert.setContentText("Mot de passe : " + generated + "\nScore zxcvbn : " + score + "/4 (Très fort)");

        ButtonType copyBtn = new ButtonType("Copier", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(copyBtn, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(type -> {
            if (type == copyBtn) {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                clipboardService.copyWithAutoClear(generated, () -> ToastUtil.dismiss());
                ToastUtil.showCopied(stage);
            }
        });
    }

    @FXML
    private void handleExport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportation");
        alert.setHeaderText("Exportation du coffre");
        alert.setContentText("Vos " + entries.size() + " identifiants sont chiffrés en base de données vault.db.");
        alert.showAndWait();
    }

    @FXML
    private void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres");
        alert.setHeaderText("Coffre-fort LockBox v1.0");
        alert.setContentText("Chiffrement AES-256-GCM + Argon2id actif.\nBase de données SQLite : ~/.lockbox/vault.db");
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() throws IOException {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Se déconnecter ?");
        confirm.setContentText("La clé de chiffrement sera effacée de la mémoire.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sessionManager.clearSession();
                    clipboardService.clearNow();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockbox/login-view.fxml"));
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

                    Scene scene = new Scene(loader.load(), 420, 520);
                    scene.getStylesheets().add(getClass().getResource("/com/lockbox/styles.css").toExternalForm());
                    Stage stage = (Stage) logoutButton.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setWidth(420);
                    stage.setHeight(520);
                    stage.centerOnScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
