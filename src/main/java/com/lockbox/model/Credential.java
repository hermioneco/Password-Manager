package com.lockbox.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant un identifiant chiffré stocké dans le coffre.
 *
 * <p>Toutes les données sensibles (mot de passe, login, notes) sont stockées
 * exclusivement sous forme chiffrée (AES-256-GCM). Seuls le service, la catégorie,
 * l'URL et les horodatages sont en clair.</p>
 *
 * <h3>Schéma de chiffrement :</h3>
 * <pre>
 * données_claires → CryptoService.encrypt(données, cléAES) → [IV (12 bytes) | ciphertext]
 * </pre>
 * <p>Le vecteur d'initialisation (IV) est stocké séparément dans {@code iv}.
 * Il est unique par entrée et par chiffrement (généré via {@link java.security.SecureRandom}).</p>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 * @see User
 */
@Entity
@Table(name = "credentials", indexes = {
    @Index(name = "idx_credentials_user_id", columnList = "user_id"),
    @Index(name = "idx_credentials_category", columnList = "category")
})
public class Credential {

    /**
     * Identifiant unique auto-généré.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Propriétaire de cet identifiant.
     * Relation Many-to-One : plusieurs credentials par utilisateur.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Nom du service associé (ex. : "Gmail", "GitHub").
     * Stocké en clair pour permettre la recherche et le filtrage.
     */
    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    /**
     * Catégorie du credential (ex. : "Email", "Banque", "Réseaux sociaux").
     * Stocké en clair pour le filtrage par catégorie.
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * URL du service (ex. : "https://mail.google.com").
     * Stocké en clair — ne contient pas de données sensibles.
     */
    @Column(name = "url", length = 512)
    private String url;

    /**
     * Login/email chiffré en AES-256-GCM.
     *
     * <p>Type {@code @Lob} pour accepter des données binaires de taille variable
     * (ciphertext pouvant être plus long que le clair original).</p>
     */
    @Lob
    @Column(name = "encrypted_login", nullable = false)
    private byte[] encryptedLogin;

    /**
     * Vecteur d'initialisation (IV) utilisé pour chiffrer {@code encryptedLogin}.
     * 12 bytes — unique par entrée (généré via SecureRandom).
     */
    @Column(name = "iv_login", nullable = false, length = 32)
    private byte[] ivLogin;

    /**
     * Mot de passe chiffré en AES-256-GCM.
     *
     * <p>Jamais stocké en clair. Déchiffré uniquement en mémoire lors de
     * l'affichage ou de la copie presse-papier.</p>
     */
    @Lob
    @Column(name = "encrypted_password", nullable = false)
    private byte[] encryptedPassword;

    /**
     * Vecteur d'initialisation (IV) utilisé pour chiffrer {@code encryptedPassword}.
     * 12 bytes — unique et différent de {@code ivLogin}.
     */
    @Column(name = "iv_password", nullable = false, length = 32)
    private byte[] ivPassword;

    /**
     * Notes additionnelles chiffrées (peut être null si aucune note).
     */
    @Lob
    @Column(name = "encrypted_notes")
    private byte[] encryptedNotes;

    /**
     * IV pour les notes (null si pas de notes).
     */
    @Column(name = "iv_notes", length = 32)
    private byte[] ivNotes;

    /**
     * Horodatage de création de l'entrée.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Horodatage de dernière modification.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructeurs ──────────────────────────────────────────

    /** Constructeur par défaut requis par JPA/Hibernate. */
    protected Credential() {}

    /**
     * Crée un credential chiffré minimal (login + mot de passe).
     *
     * @param user              propriétaire
     * @param serviceName       nom du service
     * @param encryptedLogin    login chiffré (AES-GCM)
     * @param ivLogin           IV du login (12 bytes)
     * @param encryptedPassword mot de passe chiffré (AES-GCM)
     * @param ivPassword        IV du mot de passe (12 bytes)
     */
    public Credential(User user, String serviceName,
                      byte[] encryptedLogin, byte[] ivLogin,
                      byte[] encryptedPassword, byte[] ivPassword) {
        this.user              = user;
        this.serviceName       = serviceName;
        this.encryptedLogin    = encryptedLogin;
        this.ivLogin           = ivLogin;
        this.encryptedPassword = encryptedPassword;
        this.ivPassword        = ivPassword;
        this.createdAt         = LocalDateTime.now();
        this.updatedAt         = LocalDateTime.now();
    }

    // ── Callbacks JPA ─────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────────────────────

    /** @return identifiant auto-généré */
    public Long getId() { return id; }

    /** @return propriétaire du credential */
    public User getUser() { return user; }

    /** @param user propriétaire */
    public void setUser(User user) { this.user = user; }

    /** @return nom du service (clair) */
    public String getServiceName() { return serviceName; }

    /** @param serviceName nom du service */
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    /** @return catégorie (clair) */
    public String getCategory() { return category; }

    /** @param category catégorie */
    public void setCategory(String category) { this.category = category; }

    /** @return URL du service (clair) */
    public String getUrl() { return url; }

    /** @param url URL du service */
    public void setUrl(String url) { this.url = url; }

    /** @return login chiffré (bytes AES-GCM) */
    public byte[] getEncryptedLogin() { return encryptedLogin; }

    /** @param encryptedLogin login chiffré */
    public void setEncryptedLogin(byte[] encryptedLogin) { this.encryptedLogin = encryptedLogin; }

    /** @return IV du login (12 bytes) */
    public byte[] getIvLogin() { return ivLogin; }

    /** @param ivLogin IV du login */
    public void setIvLogin(byte[] ivLogin) { this.ivLogin = ivLogin; }

    /** @return mot de passe chiffré (bytes AES-GCM) */
    public byte[] getEncryptedPassword() { return encryptedPassword; }

    /** @param encryptedPassword mot de passe chiffré */
    public void setEncryptedPassword(byte[] encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    /** @return IV du mot de passe (12 bytes) */
    public byte[] getIvPassword() { return ivPassword; }

    /** @param ivPassword IV du mot de passe */
    public void setIvPassword(byte[] ivPassword) { this.ivPassword = ivPassword; }

    /** @return notes chiffrées (peut être null) */
    public byte[] getEncryptedNotes() { return encryptedNotes; }

    /** @param encryptedNotes notes chiffrées */
    public void setEncryptedNotes(byte[] encryptedNotes) { this.encryptedNotes = encryptedNotes; }

    /** @return IV des notes (peut être null) */
    public byte[] getIvNotes() { return ivNotes; }

    /** @param ivNotes IV des notes */
    public void setIvNotes(byte[] ivNotes) { this.ivNotes = ivNotes; }

    /** @return horodatage de création */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return horodatage de dernière modification */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        // NE PAS inclure les données chiffrées dans le toString()
        return "Credential{id=" + id + ", service='" + serviceName
                + "', category='" + category + "', createdAt=" + createdAt + "}";
    }
}
