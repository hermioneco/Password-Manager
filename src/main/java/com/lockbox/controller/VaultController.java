package com.lockbox.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class VaultController {

    // ── Header ──
    @FXML private TextField searchField;
    @FXML private Button settingsButton;

    // ── Body ──
    @FXML private Label userEmailLabel;
    @FXML private Label entryCountLabel;
    @FXML private Button addButton;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    // ── Table ──
    @FXML private TableView<String[]> passwordTable;
    @FXML private TableColumn<String[], String> colTitle;
    @FXML private TableColumn<String[], String> colUsername;
    @FXML private TableColumn<String[], String> colUrl;
    @FXML private TableColumn<String[], String> colPassword;
    @FXML private TableColumn<String[], String> colActions;

    // ── Footer ──
    @FXML private Button logoutButton;

    // Données temporaires (remplacées par PasswordService plus tard)
    private final ObservableList<String[]> entries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadDemoData();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[0]));
        colUsername.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[1]));
        colUrl.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[2]));
        colPassword.setCellValueFactory(data ->
                new SimpleStringProperty("••••••••"));

        // Colonne Actions avec boutons 👁 ✏ 🗑
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView   = new Button("👁");
            private final Button btnEdit   = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final javafx.scene.layout.HBox box =
                    new javafx.scene.layout.HBox(4, btnView, btnEdit, btnDelete);

            {
                String btnStyle = "-fx-background-color: #313244; -fx-text-fill: #cdd6f4;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 8;";
                btnView.setStyle(btnStyle);
                btnEdit.setStyle(btnStyle);
                btnDelete.setStyle("-fx-background-color: #3d1a1a; -fx-text-fill: #f38ba8;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 4 8;");

                btnView.setOnAction(e -> handleView(getIndex()));
                btnEdit.setOnAction(e -> handleEdit(getIndex()));
                btnDelete.setOnAction(e -> handleDelete(getIndex()));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        passwordTable.setItems(entries);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // TODO : filtrer avec PasswordService
            System.out.println("Recherche : " + newVal);
        });
    }

    private void loadDemoData() {
        // Données fictives pour tester l'affichage
        entries.addAll(
                new String[]{"Gmail",  "kef@gmail.com",  "gmail.com"},
                new String[]{"GitHub", "kef Davis",         "github.com"},
                new String[]{"Netflix","Kef@gmail.com",  "netflix.com"}
        );
        updateCount();
    }

    private void updateCount() {
        entryCountLabel.setText(entries.size() + " entrée(s)");
    }

    // ── Handlers ──

    private void handleView(int index) {
        if (index < 0 || index >= entries.size()) return;
        String[] entry = entries.get(index);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe");
        alert.setHeaderText(entry[0]);
        alert.setContentText("Identifiant : " + entry[1] +
                "\nMot de passe : [chiffré - TODO CryptoService]");
        alert.showAndWait();
    }

    private void handleEdit(int index) {
        // TODO : ouvrir dialogue d'édition
        System.out.println("Éditer : " + entries.get(index)[0]);
    }

    private void handleDelete(int index) {
        if (index < 0 || index >= entries.size()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer");
        confirm.setHeaderText("Supprimer " + entries.get(index)[0] + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                entries.remove(index);
                updateCount();
            }
        });
    }

    @FXML
    private void handleAdd() {
        // TODO : ouvrir dialogue d'ajout
        System.out.println("Ajouter une entrée...");
    }

    @FXML
    private void handleGenerate() {
        // TODO : ouvrir popup générateur
        System.out.println("Générer un mot de passe...");
    }

    @FXML
    private void handleExport() {
        // TODO : PasswordService.exportEntries()
        System.out.println("Exporter...");
    }

    @FXML
    private void handleSettings() {
        System.out.println("Paramètres...");
    }

    @FXML
    private void handleLogout() throws IOException {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Se déconnecter ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/lockbox/login-view.fxml")
                    );
                    Scene scene = new Scene(loader.load(), 420, 520);
                    scene.getStylesheets().add(
                            getClass().getResource("/com/lockbox/styles.css").toExternalForm()
                    );
                    Stage stage = (Stage) logoutButton.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setWidth(420);
                    stage.setHeight(520);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Appelé depuis LoginController après connexion réussie
    public void setUserEmail(String email) {
        userEmailLabel.setText(email);
    }
}
