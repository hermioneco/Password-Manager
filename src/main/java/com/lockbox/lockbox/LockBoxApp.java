package com.lockbox.lockbox;

import com.lockbox.lockbox.service.AuthService;
import com.lockbox.lockbox.service.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Point d'entrée principal de l'application LockBox Desktop.
 *
 * <p>Démarre le contexte Spring Boot avant l'initialisation de JavaFX.
 * Les beans Spring (services, repositories) sont accessibles via le contexte
 * et injectés dans les controllers JavaFX via
 * {@code fxmlLoader.setControllerFactory(springContext::getBean)}.</p>
 *
 * <h3>Séquence de démarrage :</h3>
 * <ol>
 *   <li>{@code main()} lance Spring Boot (IoC, JPA, SQLite, DatabaseConfig)</li>
 *   <li>JavaFX {@code start()} est appelé sur le thread JavaFX Application Thread</li>
 *   <li>{@link AuthService#hasExistingUser()} détermine la vue initiale</li>
 *   <li>La vue Login ou Register est affichée</li>
 * </ol>
 *
 * @author Personne A (interface) — intégration Spring par Personne C
 * @version 1.0
 */
@SpringBootApplication
public class LockBoxApp extends Application {

    /**
     * Contexte Spring partagé — accessible par les controllers JavaFX
     * pour récupérer les beans (AuthService, CryptoService...).
     */
    private static ConfigurableApplicationContext springContext;

    /**
     * Point d'entrée Java — démarre Spring Boot puis JavaFX.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // 0. CRITIQUE : créer ~/.lockbox/ AVANT que Spring Boot démarre.
        //    Hibernate/HikariCP initialisent la connexion JDBC très tôt —
        //    potentiellement avant que DatabaseConfig (@PostConstruct) s'exécute.
        //    SQLite refuse de créer le fichier .db si le dossier n'existe pas.
        ensureDataDirectoryExists();

        // 1. Démarrage du contexte Spring (SQLite init, JPA, services)
        springContext = SpringApplication.run(LockBoxApp.class, args);

        // 2. Démarrage de JavaFX
        launch(args);
    }

    private static void ensureDataDirectoryExists() {
        Path appDataPath = DatabaseConfig.getAppDataPath();
        try {
            if (!Files.exists(appDataPath)) {
                Files.createDirectories(appDataPath);
                System.out.println("[LockBox] Répertoire créé avant démarrage Spring : "
                        + appDataPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Impossible de créer le répertoire de données LockBox : " + appDataPath, e);
        }
    }

    /**
     * Retourne le contexte Spring (utilisé par les factories de controllers JavaFX).
     *
     * @return contexte Spring actif
     */
    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Charge la vue initiale selon l'état de la base de données :
     * si aucun utilisateur n'existe → RegisterView, sinon → LoginView.</p>
     */
    @Override
    public void start(Stage stage) throws IOException {
        AuthService authService = springContext.getBean(AuthService.class);

        // Choix de la vue initiale selon l'état de la base
        String fxmlFile = authService.hasExistingUser()
                ? "/com/lockbox/lockbox/login-view.fxml"
                : "/com/lockbox/lockbox/login-view.fxml"; // register-view.fxml en Phase 2

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));

        // Injection Spring dans les controllers JavaFX
        fxmlLoader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(fxmlLoader.load(), 420, 520);
        stage.setTitle("LockBox — Gestionnaire de mots de passe");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ferme proprement le contexte Spring à la fermeture de l'application.
     * Déclenche le logout (effacement de la clé AES) via les beans Spring.</p>
     */
    @Override
    public void stop() {
        // Déconnexion propre — efface la clé AES de la mémoire
        if (springContext != null) {
            try {
                AuthService authService = springContext.getBean(AuthService.class);
                authService.logout();
            } catch (Exception e) {
                // Ignoré — fermeture en cours
            }
            springContext.close();
        }
    }
}
