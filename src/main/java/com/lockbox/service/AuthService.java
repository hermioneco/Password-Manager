package com.lockbox.service;

import com.lockbox.dao.UserRepository;
import com.lockbox.model.User;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service d'authentification et de gestion de session pour LockBox.
 *
 * <h3>Responsabilités :</h3>
 * <ul>
 *   <li>Inscription : hachage Argon2id du mot de passe maître + génération du sel PBKDF2</li>
 *   <li>Connexion : vérification Argon2id + dérivation de la clé AES en mémoire</li>
 *   <li>Session : stockage de la clé AES active ({@link #sessionKey})</li>
 *   <li>Déconnexion : effacement sécurisé de la clé AES ({@link CryptoService#destroyKey})</li>
 * </ul>
 *
 * <h3>Paramètres Argon2id (selon cahier des charges) :</h3>
 * <ul>
 *   <li>Memory cost : {@value #ARGON2_MEMORY} KB (64 MB)</li>
 *   <li>Time cost   : {@value #ARGON2_ITERATIONS} itérations</li>
 *   <li>Parallelism : {@value #ARGON2_PARALLELISM} threads</li>
 * </ul>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 * @see CryptoService
 */
@Service
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    // ── Paramètres Argon2id ──────────────────────────────────────
    /** Mémoire allouée à Argon2id en KB (65 536 KB = 64 MB). */
    private static final int ARGON2_MEMORY = 65_536;
    /** Nombre d'itérations Argon2id (time cost). */
    private static final int ARGON2_ITERATIONS = 3;
    /** Parallélisme Argon2id (nombre de threads). */
    private static final int ARGON2_PARALLELISM = 4;

    // ── Dépendances injectées par Spring ─────────────────────────
    private final UserRepository userRepository;
    private final CryptoService  cryptoService;

    /** Instance Argon2id (thread-safe). */
    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // ── État de session (non persisté) ───────────────────────────

    /**
     * Clé AES-256 active pour la session courante.
     *
     * <p>Stockée uniquement en mémoire vive. Effacée à la déconnexion
     * ou au verrouillage automatique via {@link #logout()}.</p>
     */
    private volatile SecretKey sessionKey;

    /**
     * Utilisateur actuellement connecté.
     * Null si aucune session active.
     */
    private volatile User currentUser;

    /**
     * Constructeur pour l'injection de dépendances Spring.
     *
     * @param userRepository repository JPA pour les utilisateurs
     * @param cryptoService  service de chiffrement/dérivation de clé
     */
    public AuthService(UserRepository userRepository, CryptoService cryptoService) {
        this.userRepository = userRepository;
        this.cryptoService  = cryptoService;
    }

    // ── Inscription ──────────────────────────────────────────────

    /**
     * Inscrit un nouvel utilisateur avec son mot de passe maître.
     *
     * <p>Opérations effectuées :</p>
     * <ol>
     *   <li>Vérifie que l'email n'est pas déjà utilisé</li>
     *   <li>Génère un sel aléatoire via {@link CryptoService#generateSalt()}</li>
     *   <li>Hache le mot de passe avec Argon2id</li>
     *   <li>Persiste l'utilisateur dans SQLite (hash + sel uniquement)</li>
     *   <li>Efface le mot de passe du tableau {@code char[]}</li>
     * </ol>
     *
     * @param email    adresse email (identifiant de connexion)
     * @param password mot de passe maître en clair (effacé après usage)
     * @return l'utilisateur créé et persisté
     * @throws AuthException si l'email est déjà utilisé ou si la validation échoue
     */

    public User register(String email, char[] password) throws AuthException {
        // Validation basique
        if (email == null || email.isBlank()) {
            throw new AuthException("L'adresse email ne peut pas être vide.");
        }
        if (password == null || password.length < 8) {
            throw new AuthException("Le mot de passe maître doit contenir au moins 8 caractères.");
        }

        // Vérification unicité de l'email
        if (userRepository.existsByEmail(email.toLowerCase())) {
            throw new AuthException("Un profil avec cet email existe déjà.");
        }

        try {
            // Génération du sel PBKDF2
            String salt = cryptoService.generateSalt();

            // Hachage Argon2id du mot de passe maître
            String passwordHash = argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY,
                                              ARGON2_PARALLELISM, password);

            // Création et persistance de l'utilisateur
            User user = new User(email.toLowerCase(), passwordHash, salt);
            User saved = userRepository.save(user);

            LOG.info("[AuthService] Nouvel utilisateur inscrit : " + email);
            return saved;

        } finally {
            // Effacement immédiat du mot de passe en clair — sécurité
            Arrays.fill(password, '\0');
        }
    }

    // ── Connexion ────────────────────────────────────────────────

    /**
     * Authentifie un utilisateur et ouvre une session sécurisée.
     *
     * <p>Opérations effectuées :</p>
     * <ol>
     *   <li>Recherche l'utilisateur par email</li>
     *   <li>Vérifie le mot de passe contre le hash Argon2id</li>
     *   <li>Dérive la clé AES-256 via PBKDF2 et la stocke en {@link #sessionKey}</li>
     *   <li>Met à jour {@code lastLoginAt} dans SQLite</li>
     *   <li>Efface le mot de passe du tableau {@code char[]}</li>
     * </ol>
     *
     * @param email    adresse email de l'utilisateur
     * @param password mot de passe maître en clair (effacé après usage)
     * @return l'utilisateur authentifié
     * @throws AuthException si l'email est inconnu ou le mot de passe incorrect
     */

    public User login(String email, char[] password) throws AuthException {
        if (email == null || email.isBlank() || password == null) {
            throw new AuthException("Email et mot de passe requis.");
        }

        try {
            // Recherche de l'utilisateur
            Optional<User> optUser = userRepository.findByEmail(email.toLowerCase());
            if (optUser.isEmpty()) {
                // Même message que pour mot de passe incorrect — évite l'énumération d'emails
                throw new AuthException("Email ou mot de passe incorrect.");
            }

            User user = optUser.get();

            // Vérification Argon2id
            boolean valid = argon2.verify(user.getPasswordHash(), password);
            if (!valid) {
                LOG.warning("[AuthService] Tentative de connexion échouée pour : " + email);
                throw new AuthException("Email ou mot de passe incorrect.");
            }

            // Dérivation de la clé AES-256 en mémoire
            sessionKey  = cryptoService.deriveKey(password, user.getSalt());
            currentUser = user;

            // Mise à jour de la dernière connexion
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            LOG.info("[AuthService] Connexion réussie : " + email);
            return user;

        } finally {
            // Effacement immédiat du mot de passe en clair — sécurité
            Arrays.fill(password, '\0');
        }
    }

    // ── Déconnexion / Verrouillage ────────────────────────────────

    /**
     * Ferme la session courante et efface la clé AES de la mémoire.
     *
     * <p>Appelé lors de la déconnexion explicite ou du verrouillage
     * automatique après inactivité (géré par Person B via {@code Timeline} JavaFX).</p>
     */
    public void logout() {
        cryptoService.destroyKey(sessionKey);
        sessionKey  = null;
        currentUser = null;
        LOG.info("[AuthService] Session fermée — clé AES effacée.");
    }

    // ── Accesseurs de session ────────────────────────────────────

    /**
     * Retourne la clé AES-256 active pour la session courante.
     *
     * <p>Utilisé par {@code CredentialService} (Person B) pour
     * chiffrer/déchiffrer les credentials.</p>
     *
     * @return clé AES active, ou {@code null} si aucune session
     */
    public SecretKey getSessionKey() {
        return sessionKey;
    }

    /**
     * Retourne l'utilisateur actuellement connecté.
     *
     * @return utilisateur connecté, ou {@code null} si aucune session
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si une session est active.
     *
     * @return {@code true} si un utilisateur est connecté et la clé disponible
     */
    public boolean isLoggedIn() {
        return sessionKey != null && currentUser != null;
    }

    /**
     * Vérifie si un premier utilisateur existe dans la base.
     *
     * <p>Utilisé au démarrage pour décider d'afficher
     * la vue "Inscription" ou "Connexion".</p>
     *
     * @return {@code true} si au moins un profil est enregistré
     */
    public boolean hasExistingUser() {
        return userRepository.count() > 0;
    }

    // ── Exception dédiée ─────────────────────────────────────────

    /**
     * Exception vérifiée levée lors d'une erreur d'authentification.
     */
    public static class AuthException extends Exception {
        /**
         * @param message message d'erreur affiché à l'utilisateur
         */
        public AuthException(String message) {
            super(message);
        }
    }
}
