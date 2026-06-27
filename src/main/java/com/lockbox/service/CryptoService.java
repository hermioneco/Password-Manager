package com.lockbox.service;

import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Destroyable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Service de chiffrement et déchiffrement AES-256-GCM pour LockBox.
 *
 * <h3>Algorithmes utilisés :</h3>
 * <ul>
 *   <li><b>Chiffrement</b> : AES-256-GCM (authentifié, intégrité garantie)</li>
 *   <li><b>Dérivation de clé</b> : PBKDF2WithHmacSHA256, 100 000 itérations</li>
 *   <li><b>IV</b> : 12 bytes aléatoires via {@link SecureRandom} — unique par opération</li>
 *   <li><b>Tag GCM</b> : 128 bits — détecte toute falsification du ciphertext</li>
 * </ul>
 *
 * <h3>Principe zero-knowledge :</h3>
 * <p>La clé AES ({@link SecretKey}) n'est jamais persistée sur le disque.
 * Elle est dérivée du mot de passe maître à chaque connexion et effacée
 * de la mémoire à la déconnexion via {@link #destroyKey(SecretKey)}.</p>
 *
 * <h3>Compatibilité NIST :</h3>
 * <p>Les paramètres AES-GCM respectent les recommandations NIST SP 800-38D.
 * Les vecteurs de test NIST peuvent être validés via {@code CryptoServiceTest}.</p>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 * @see AuthService
 */
@Service
public class CryptoService {

    private static final Logger LOG = Logger.getLogger(CryptoService.class.getName());

    // ── Constantes cryptographiques ─────────────────────────────

    /** Algorithme de chiffrement : AES en mode Galois/Counter avec NoPadding. */
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    /** Algorithme de dérivation de clé : PBKDF2 avec HMAC-SHA256. */
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";

    /** Taille de la clé AES en bits (256 bits = 32 bytes). */
    private static final int AES_KEY_SIZE_BITS = 256;

    /** Taille du tag d'authentification GCM en bits (128 bits recommandé NIST). */
    private static final int GCM_TAG_LENGTH_BITS = 128;

    /** Taille du vecteur d'initialisation (IV) en bytes pour GCM (12 bytes = 96 bits). */
    public static final int GCM_IV_LENGTH_BYTES = 12;

    /** Nombre d'itérations PBKDF2 (OWASP recommande >= 600 000 pour SHA-256 en 2024,
     *  le cahier des charges fixe 100 000 comme minimum acceptable pour ce projet). */
    private static final int PBKDF2_ITERATIONS = 100_000;

    /** Taille du sel pour PBKDF2 en bytes (16 bytes = 128 bits). */
    public static final int SALT_LENGTH_BYTES = 16;

    /** Instance partagée de SecureRandom — thread-safe et CSPRNG. */
    private final SecureRandom secureRandom = new SecureRandom();

    // ── Dérivation de clé ────────────────────────────────────────

    /**
     * Dérive une clé AES-256 à partir du mot de passe maître et d'un sel.
     *
     * <p>Utilise PBKDF2WithHmacSHA256 avec {@value #PBKDF2_ITERATIONS} itérations.
     * La clé résultante est utilisée pour toutes les opérations de chiffrement
     * de la session courante.</p>
     *
     * <p><b>Important :</b> le tableau {@code password} doit être effacé par l'appelant
     * immédiatement après l'appel via {@code Arrays.fill(password, '\0')}.</p>
     *
     * @param password mot de passe maître en clair (char[] pour permettre l'effacement)
     * @param saltBase64 sel encodé en Base64 (stocké dans {@code User.salt})
     * @return clé AES-256 en mémoire ({@link SecretKey}) — jamais persistée
     * @throws RuntimeException si l'algorithme PBKDF2 n'est pas disponible
     */
    public SecretKey deriveKey(char[] password, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, AES_KEY_SIZE_BITS);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();

            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            // Effacement immédiat des bytes de clé intermédiaires
            Arrays.fill(keyBytes, (byte) 0);
            // Effacement de la spec (contient le mot de passe en clair)
            spec.clearPassword();

            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erreur de dérivation de clé PBKDF2 : " + e.getMessage(), e);
        } finally {
            Arrays.fill(salt, (byte) 0);
        }
    }

    /**
     * Génère un sel aléatoire cryptographiquement sûr encodé en Base64.
     *
     * <p>Appelé une seule fois lors de la création du profil utilisateur.
     * Le sel est ensuite stocké dans {@code User.salt} en clair (c'est normal :
     * le sel n'est pas un secret, il sert uniquement à rendre chaque hash unique).</p>
     *
     * @return sel de {@value #SALT_LENGTH_BYTES} bytes encodé en Base64
     */
    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        String encoded = Base64.getEncoder().encodeToString(salt);
        Arrays.fill(salt, (byte) 0);
        return encoded;
    }

    // ── Chiffrement ──────────────────────────────────────────────

    /**
     * Chiffre des données en AES-256-GCM avec un IV aléatoire unique.
     *
     * <p>Un IV de {@value #GCM_IV_LENGTH_BYTES} bytes est généré à chaque appel
     * via {@link SecureRandom}. L'IV doit être stocké séparément avec le ciphertext
     * pour permettre le déchiffrement ultérieur.</p>
     *
     * @param plaintext données à chiffrer (non null, non vide)
     * @param key       clé AES-256 dérivée du mot de passe maître
     * @return tableau de 2 éléments : {@code [0]} = IV (12 bytes), {@code [1]} = ciphertext
     * @throws LockBoxCryptoException si le chiffrement échoue (algorithme, clé invalide...)
     * @throws IllegalArgumentException si {@code plaintext} ou {@code key} est null
     */
    public byte[][] encrypt(byte[] plaintext, SecretKey key) throws LockBoxCryptoException {
        if (plaintext == null || plaintext.length == 0) {
            throw new IllegalArgumentException("Les données à chiffrer ne peuvent pas être vides.");
        }
        if (key == null) {
            throw new IllegalArgumentException("La clé AES ne peut pas être null.");
        }

        try {
            // Génération d'un IV unique (12 bytes = 96 bits — recommandation NIST GCM)
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            // Configuration du cipher AES-GCM
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // Chiffrement
            byte[] ciphertext = cipher.doFinal(plaintext);

            return new byte[][] { iv, ciphertext };

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new LockBoxCryptoException("Algorithme AES-GCM non disponible : " + e.getMessage(), e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new LockBoxCryptoException("Clé ou paramètres AES invalides : " + e.getMessage(), e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new LockBoxCryptoException("Erreur de chiffrement AES-GCM : " + e.getMessage(), e);
        }
    }

    /**
     * Surcharge pratique : chiffre une chaîne de caractères UTF-8.
     *
     * @param plaintext texte à chiffrer
     * @param key       clé AES-256
     * @return tableau {@code [iv, ciphertext]}
     * @throws LockBoxCryptoException si le chiffrement échoue
     */
    public byte[][] encrypt(String plaintext, SecretKey key) throws LockBoxCryptoException {
        if (plaintext == null) {
            throw new IllegalArgumentException("Le texte à chiffrer ne peut pas être null.");
        }
        return encrypt(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8), key);
    }

    // ── Déchiffrement ────────────────────────────────────────────

    /**
     * Déchiffre un ciphertext AES-256-GCM.
     *
     * <p>Le tag GCM est vérifié automatiquement par {@link Cipher#doFinal}.
     * Si le ciphertext a été falsifié, une {@link LockBoxCryptoException}
     * est levée (encapsulant une {@link BadPaddingException}).</p>
     *
     * @param iv         vecteur d'initialisation utilisé lors du chiffrement (12 bytes)
     * @param ciphertext données chiffrées (avec tag GCM intégré)
     * @param key        clé AES-256 dérivée du mot de passe maître
     * @return données déchiffrées en clair
     * @throws LockBoxCryptoException si le déchiffrement ou la vérification d'intégrité échoue
     * @throws IllegalArgumentException si un paramètre est null ou invalide
     */
    public byte[] decrypt(byte[] iv, byte[] ciphertext, SecretKey key) throws LockBoxCryptoException {
        if (iv == null || iv.length != GCM_IV_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                "IV invalide : doit être exactement " + GCM_IV_LENGTH_BYTES + " bytes.");
        }
        if (ciphertext == null || ciphertext.length == 0) {
            throw new IllegalArgumentException("Le ciphertext ne peut pas être vide.");
        }
        if (key == null) {
            throw new IllegalArgumentException("La clé AES ne peut pas être null.");
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            return cipher.doFinal(ciphertext);

        } catch (BadPaddingException e) {
            // Tag GCM invalide = données corrompues ou falsifiées
            throw new LockBoxCryptoException(
                "Vérification d'intégrité GCM échouée — données corrompues ou clé incorrecte.", e);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new LockBoxCryptoException("Algorithme AES-GCM non disponible : " + e.getMessage(), e);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new LockBoxCryptoException("Clé ou IV invalides : " + e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            throw new LockBoxCryptoException("Taille de bloc invalide : " + e.getMessage(), e);
        }
    }

    /**
     * Surcharge pratique : déchiffre et retourne une chaîne UTF-8.
     *
     * @param iv         IV du chiffrement (12 bytes)
     * @param ciphertext données chiffrées
     * @param key        clé AES-256
     * @return texte déchiffré en UTF-8
     * @throws LockBoxCryptoException si le déchiffrement échoue
     */
    public String decryptToString(byte[] iv, byte[] ciphertext, SecretKey key)
            throws LockBoxCryptoException {
        byte[] plaintext = decrypt(iv, ciphertext, key);
        String result = new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
        Arrays.fill(plaintext, (byte) 0);
        return result;
    }

    // ── Gestion du cycle de vie de la clé ────────────────────────

    /**
     * Efface la clé AES de la mémoire après déconnexion ou verrouillage.
     *
     * <p>Tente d'appeler {@link Destroyable#destroy()} sur la clé pour effacer
     * les bytes en mémoire. Si non supporté (implémentation JVM dépendante),
     * log un avertissement.</p>
     *
     * @param key clé à détruire (peut être null — ignoré dans ce cas)
     */
    public void destroyKey(SecretKey key) {
        if (key == null) return;
        try {
            if (key instanceof Destroyable destroyable) {
                destroyable.destroy();
                LOG.info("[CryptoService] Clé AES effacée de la mémoire.");
            } else {
                // Fallback : effacer les bytes si possible
                byte[] encoded = key.getEncoded();
                if (encoded != null) {
                    Arrays.fill(encoded, (byte) 0);
                }
                LOG.warning("[CryptoService] destroy() non supporté — effacement manuel des bytes.");
            }
        } catch (Exception e) {
            LOG.warning("[CryptoService] Erreur lors de l'effacement de la clé : " + e.getMessage());
        }
    }

    // ── Exception dédiée ─────────────────────────────────────────

    /**
     * Exception vérifiée levée par {@link CryptoService} lors d'une erreur
     * de chiffrement, déchiffrement ou vérification d'intégrité.
     */
    public static class LockBoxCryptoException extends Exception {
        /**
         * @param message description de l'erreur
         * @param cause   exception d'origine
         */
        public LockBoxCryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
