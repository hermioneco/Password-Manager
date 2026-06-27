package com.lockbox.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Composant Spring chargé de préparer l'environnement de stockage local
 * avant le démarrage de l'application.
 *
 * <p>Crée le répertoire {@code ~/.lockbox/} s'il n'existe pas et lui applique
 * des permissions restrictives (700 sur Linux/macOS) pour que seul
 * l'utilisateur courant puisse lire le coffre SQLite.</p>
 *
 * <p>Chemin du répertoire selon la plateforme :</p>
 * <ul>
 *   <li>Linux   : {@code /home/<user>/.lockbox/}</li>
 *   <li>macOS   : {@code /Users/<user>/.lockbox/}</li>
 *   <li>Windows : {@code C:\Users\<user>\.lockbox\}</li>
 * </ul>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 */
@Component
public class DatabaseConfig {

    private static final Logger LOG = Logger.getLogger(DatabaseConfig.class.getName());

    /** Nom du répertoire de données de l'application. */
    public static final String APP_DIR_NAME = ".lockbox";

    /**
     * Retourne le chemin absolu du répertoire de données LockBox.
     *
     * @return chemin vers {@code ~/.lockbox/}
     */
    public static Path getAppDataPath() {
        return Paths.get(System.getProperty("user.home"), APP_DIR_NAME);
    }

    /**
     * Retourne le chemin complet vers la base de données SQLite.
     *
     * @return chemin vers {@code ~/.lockbox/vault.db}
     */
    public static Path getDatabasePath() {
        return getAppDataPath().resolve("vault.db");
    }

    /**
     * Initialise le répertoire de données au démarrage de l'application.
     *
     * <p>Appelé automatiquement par Spring après l'injection des dépendances
     * grâce à l'annotation {@link PostConstruct}.</p>
     *
     * <p>Actions effectuées :</p>
     * <ol>
     *   <li>Crée {@code ~/.lockbox/} si inexistant</li>
     *   <li>Applique les permissions 700 (Linux/macOS) — ignoré sur Windows</li>
     *   <li>Log le chemin de la base utilisée</li>
     * </ol>
     *
     * @throws RuntimeException si la création du répertoire échoue
     */
    @PostConstruct
    public void initializeDataDirectory() {
        Path appDataPath = getAppDataPath();

        try {
            if (!Files.exists(appDataPath)) {
                // Crée le répertoire (et ses parents si nécessaire)
                Files.createDirectories(appDataPath);
                LOG.info("[LockBox] Répertoire de données créé : " + appDataPath.toAbsolutePath());

                // Applique les permissions 700 sur Linux/macOS (lecture/écriture/exécution
                // uniquement pour le propriétaire)
                applySecurePermissions(appDataPath);
            } else {
                LOG.info("[LockBox] Répertoire de données existant : " + appDataPath.toAbsolutePath());
            }

            LOG.info("[LockBox] Base de données SQLite : " + getDatabasePath().toAbsolutePath());

        } catch (IOException e) {
            // Erreur critique — impossible de démarrer sans le répertoire de données
            throw new RuntimeException(
                "Impossible de créer le répertoire de données LockBox : " + appDataPath, e);
        }
    }

    /**
     * Applique des permissions restrictives (700) au répertoire de données.
     *
     * <p>Sur Windows, les permissions POSIX ne sont pas disponibles.
     * L'exception {@link UnsupportedOperationException} est ignorée
     * silencieusement car la sécurité Windows est gérée via les ACL.</p>
     *
     * @param directory chemin du répertoire à sécuriser
     */
    private void applySecurePermissions(Path directory) {
        try {
            // Permissions POSIX : rwx------ (700)
            Set<PosixFilePermission> permissions =
                PosixFilePermissions.fromString("rwx------");
            Files.setPosixFilePermissions(directory, permissions);
            LOG.info("[LockBox] Permissions 700 appliquées sur : " + directory);
        } catch (UnsupportedOperationException e) {
            // Windows ne supporte pas POSIX — normal, pas d'erreur à remonter
            LOG.info("[LockBox] Permissions POSIX non supportées (Windows) — ignoré.");
        } catch (IOException e) {
            // Permissions non critiques — on log mais on ne bloque pas le démarrage
            LOG.warning("[LockBox] Impossible d'appliquer les permissions sur "
                        + directory + " : " + e.getMessage());
        }
    }
}
