package com.lockbox.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant un utilisateur de LockBox.
 *
 * <p>Un seul utilisateur par base de données locale.
 * Le mot de passe maître n'est <strong>jamais</strong> stocké en clair :
 * seul le hash Argon2id est persisté.</p>
 *
 * <p>Le sel ({@code salt}) est généré avec {@link java.security.SecureRandom}
 * lors de l'inscription et utilisé pour la dérivation de clé AES (PBKDF2).</p>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 * @see Credential
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Identifiant unique auto-généré.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Adresse email de l'utilisateur — utilisée comme identifiant de connexion.
     * Doit être unique dans la base.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Hash Argon2id du mot de passe maître.
     *
     * <p>Format : {@code $argon2id$v=19$m=65536,t=3,p=4$<sel_base64>$<hash_base64>}</p>
     * <p>Le mot de passe brut est effacé de la mémoire immédiatement après le hachage
     * via {@code Arrays.fill(password, '\0')}.</p>
     */
    @Column(name = "password_hash", nullable = false, length = 512)
    private String passwordHash;

    /**
     * Sel cryptographique aléatoire (16 bytes, encodé en Base64).
     *
     * <p>Généré une seule fois à l'inscription via {@link java.security.SecureRandom}.
     * Utilisé par {@code CryptoService.deriveKey()} pour dériver la clé AES-256
     * via PBKDF2WithHmacSHA256.</p>
     */
    @Column(name = "salt", nullable = false, length = 64)
    private String salt;

    /**
     * Horodatage de création du profil.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Horodatage de la dernière connexion réussie.
     * Mis à jour par {@code AuthService} à chaque login.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // ── Constructeurs ──────────────────────────────────────────

    /** Constructeur par défaut requis par JPA/Hibernate. */
    protected User() {}

    /**
     * Crée un nouvel utilisateur avec ses données d'authentification.
     *
     * @param email        adresse email (identifiant)
     * @param passwordHash hash Argon2id du mot de passe maître
     * @param salt         sel Base64 pour PBKDF2
     */
    public User(String email, String passwordHash, String salt) {
        this.email        = email;
        this.passwordHash = passwordHash;
        this.salt         = salt;
        this.createdAt    = LocalDateTime.now();
    }

    // ── Callbacks JPA ─────────────────────────────────────────

    /** Initialise {@code createdAt} avant la première persistance. */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ── Getters / Setters ─────────────────────────────────────

    /** @return identifiant auto-généré */
    public Long getId() { return id; }

    /** @return adresse email */
    public String getEmail() { return email; }

    /** @param email nouvel email */
    public void setEmail(String email) { this.email = email; }

    /** @return hash Argon2id du mot de passe maître */
    public String getPasswordHash() { return passwordHash; }

    /** @param passwordHash nouveau hash Argon2id */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** @return sel Base64 pour PBKDF2 */
    public String getSalt() { return salt; }

    /** @param salt nouveau sel Base64 */
    public void setSalt(String salt) { this.salt = salt; }

    /** @return date/heure de création du profil */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return date/heure de la dernière connexion */
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    /** @param lastLoginAt horodatage de la connexion courante */
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @Override
    public String toString() {
        // NE PAS inclure passwordHash ni salt dans le toString() — sécurité
        return "User{id=" + id + ", email='" + email + "', createdAt=" + createdAt + "}";
    }
}
