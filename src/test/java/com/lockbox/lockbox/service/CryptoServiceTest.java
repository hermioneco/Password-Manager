package com.lockbox.lockbox.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour {@link CryptoService}.
 *
 * <h3>Couverture :</h3>
 * <ul>
 *   <li>Dérivation de clé PBKDF2 (déterministe, unicité)</li>
 *   <li>Génération de sel (aléatoire, longueur)</li>
 *   <li>Chiffrement AES-256-GCM (IV unique par opération)</li>
 *   <li>Déchiffrement AES-256-GCM (round-trip, intégrité)</li>
 *   <li>Cas limites et erreurs (paramètres null, données falsifiées)</li>
 *   <li>Effacement de clé</li>
 * </ul>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 */
@DisplayName("CryptoService — Tests AES-256-GCM et PBKDF2")
class CryptoServiceTest {

    private CryptoService cryptoService;

    // Fixtures réutilisées entre les tests
    private static final char[] MASTER_PASSWORD = "MotDePasseM@itre2024!".toCharArray();
    private static final String TEST_PLAINTEXT   = "mon_identifiant_secret@gmail.com";

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
    }

    // ══════════════════════════════════════════════════════════════
    // Dérivation de clé (PBKDF2)
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deriveKey() retourne une clé AES-256 non null")
    void deriveKey_returnsNonNullAESKey() {
        String salt = cryptoService.generateSalt();
        char[] pwd  = Arrays.copyOf(MASTER_PASSWORD, MASTER_PASSWORD.length);

        SecretKey key = cryptoService.deriveKey(pwd, salt);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length); // 256 bits = 32 bytes
    }

    @Test
    @DisplayName("deriveKey() est déterministe — même mot de passe + sel → même clé")
    void deriveKey_isDeterministic() {
        String salt = cryptoService.generateSalt();
        char[] pwd1 = Arrays.copyOf(MASTER_PASSWORD, MASTER_PASSWORD.length);
        char[] pwd2 = Arrays.copyOf(MASTER_PASSWORD, MASTER_PASSWORD.length);

        SecretKey key1 = cryptoService.deriveKey(pwd1, salt);
        SecretKey key2 = cryptoService.deriveKey(pwd2, salt);

        assertArrayEquals(key1.getEncoded(), key2.getEncoded(),
            "La dérivation PBKDF2 doit être déterministe avec les mêmes paramètres.");
    }

    @Test
    @DisplayName("deriveKey() — mots de passe différents → clés différentes")
    void deriveKey_differentPasswords_produceDifferentKeys() {
        String salt = cryptoService.generateSalt();
        char[] pwd1 = "PasswordA!1".toCharArray();
        char[] pwd2 = "PasswordB!2".toCharArray();

        SecretKey key1 = cryptoService.deriveKey(pwd1, salt);
        SecretKey key2 = cryptoService.deriveKey(pwd2, salt);

        assertFalse(Arrays.equals(key1.getEncoded(), key2.getEncoded()),
            "Des mots de passe différents doivent produire des clés différentes.");
    }

    @Test
    @DisplayName("deriveKey() — même mot de passe, sels différents → clés différentes")
    void deriveKey_differentSalts_produceDifferentKeys() {
        String salt1 = cryptoService.generateSalt();
        String salt2 = cryptoService.generateSalt();
        char[] pwd1  = Arrays.copyOf(MASTER_PASSWORD, MASTER_PASSWORD.length);
        char[] pwd2  = Arrays.copyOf(MASTER_PASSWORD, MASTER_PASSWORD.length);

        SecretKey key1 = cryptoService.deriveKey(pwd1, salt1);
        SecretKey key2 = cryptoService.deriveKey(pwd2, salt2);

        assertFalse(Arrays.equals(key1.getEncoded(), key2.getEncoded()),
            "Des sels différents doivent produire des clés différentes (protection arc-en-ciel).");
    }

    // ══════════════════════════════════════════════════════════════
    // Génération de sel
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generateSalt() retourne un sel Base64 valide de 16 bytes")
    void generateSalt_returnsValidBase64Salt() {
        String salt = cryptoService.generateSalt();

        assertNotNull(salt);
        byte[] decoded = Base64.getDecoder().decode(salt);
        assertEquals(CryptoService.SALT_LENGTH_BYTES, decoded.length,
            "Le sel doit faire exactement " + CryptoService.SALT_LENGTH_BYTES + " bytes.");
    }

    @Test
    @DisplayName("generateSalt() produit des sels différents à chaque appel")
    void generateSalt_producesUniqueSalts() {
        String salt1 = cryptoService.generateSalt();
        String salt2 = cryptoService.generateSalt();
        String salt3 = cryptoService.generateSalt();

        assertNotEquals(salt1, salt2, "Deux sels consécutifs ne doivent pas être identiques.");
        assertNotEquals(salt2, salt3);
        assertNotEquals(salt1, salt3);
    }

    // ══════════════════════════════════════════════════════════════
    // Chiffrement (encrypt)
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("encrypt() retourne [IV, ciphertext] avec IV de 12 bytes")
    void encrypt_returnsIvAndCiphertext() throws Exception {
        SecretKey key = buildTestKey();
        byte[][] result = cryptoService.encrypt(TEST_PLAINTEXT, key);

        assertNotNull(result);
        assertEquals(2, result.length, "Le résultat doit contenir [IV, ciphertext].");
        assertEquals(CryptoService.GCM_IV_LENGTH_BYTES, result[0].length,
            "L'IV doit faire " + CryptoService.GCM_IV_LENGTH_BYTES + " bytes.");
        assertTrue(result[1].length > 0, "Le ciphertext ne doit pas être vide.");
    }

    @Test
    @DisplayName("encrypt() — deux appels identiques produisent des IV différents")
    void encrypt_sameInput_producesDifferentIVs() throws Exception {
        SecretKey key = buildTestKey();

        byte[][] result1 = cryptoService.encrypt(TEST_PLAINTEXT, key);
        byte[][] result2 = cryptoService.encrypt(TEST_PLAINTEXT, key);

        // L'IV doit être unique à chaque chiffrement (SecureRandom)
        assertFalse(Arrays.equals(result1[0], result2[0]),
            "Deux chiffrements du même texte doivent utiliser des IVs différents.");
    }

    @Test
    @DisplayName("encrypt() — le ciphertext est différent du plaintext")
    void encrypt_ciphertextDiffersFromPlaintext() throws Exception {
        SecretKey key = buildTestKey();
        byte[] plainBytes = TEST_PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[][] result = cryptoService.encrypt(plainBytes, key);

        assertFalse(Arrays.equals(plainBytes, result[1]),
            "Le ciphertext ne doit pas être identique au plaintext.");
    }

    @Test
    @DisplayName("encrypt() — lève IllegalArgumentException si plaintext null")
    void encrypt_nullPlaintext_throwsIllegalArgumentException() {
        SecretKey key = buildTestKey();
        assertThrows(IllegalArgumentException.class,
            () -> cryptoService.encrypt((byte[]) null, key));
    }

    @Test
    @DisplayName("encrypt() — lève IllegalArgumentException si clé null")
    void encrypt_nullKey_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> cryptoService.encrypt(TEST_PLAINTEXT, null));
    }

    // ══════════════════════════════════════════════════════════════
    // Déchiffrement (decrypt) — round-trip
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("decrypt() — round-trip : encrypt puis decrypt retourne le plaintext original")
    void decrypt_roundTrip_returnsOriginalPlaintext() throws Exception {
        SecretKey key = buildTestKey();

        byte[][] encrypted = cryptoService.encrypt(TEST_PLAINTEXT, key);
        byte[] decrypted   = cryptoService.decrypt(encrypted[0], encrypted[1], key);

        assertEquals(TEST_PLAINTEXT, new String(decrypted, StandardCharsets.UTF_8));
    }

    @ParameterizedTest(name = "Round-trip avec : \"{0}\"")
    @ValueSource(strings = {
        "simple",
        "Mòt dé pàssé @vec àccénts!",
        "password123456789",
        "   espaces   ",
        "🔒 emoji support 🔑",
        "a"
    })
    @DisplayName("decrypt() — round-trip avec différents types de chaînes")
    void decrypt_roundTrip_variousInputs(String input) throws Exception {
        SecretKey key = buildTestKey();

        byte[][] encrypted = cryptoService.encrypt(input, key);
        String   decrypted = cryptoService.decryptToString(encrypted[0], encrypted[1], key);

        assertEquals(input, decrypted,
            "Le round-trip doit préserver le texte original pour : " + input);
    }

    @Test
    @DisplayName("decrypt() — clé incorrecte lève LockBoxCryptoException (intégrité GCM)")
    void decrypt_wrongKey_throwsCryptoException() throws Exception {
        SecretKey correctKey = buildTestKey();
        SecretKey wrongKey   = buildTestKey("AutreMotDePasse!99".toCharArray());

        byte[][] encrypted = cryptoService.encrypt(TEST_PLAINTEXT, correctKey);

        assertThrows(CryptoService.LockBoxCryptoException.class,
            () -> cryptoService.decrypt(encrypted[0], encrypted[1], wrongKey),
            "Un déchiffrement avec une clé incorrecte doit lever LockBoxCryptoException.");
    }

    @Test
    @DisplayName("decrypt() — ciphertext falsifié lève LockBoxCryptoException")
    void decrypt_tamperedCiphertext_throwsCryptoException() throws Exception {
        SecretKey key = buildTestKey();
        byte[][] encrypted = cryptoService.encrypt(TEST_PLAINTEXT, key);

        // Falsification d'un byte du ciphertext
        byte[] tampered = Arrays.copyOf(encrypted[1], encrypted[1].length);
        tampered[0] ^= 0xFF; // flip tous les bits du premier byte

        assertThrows(CryptoService.LockBoxCryptoException.class,
            () -> cryptoService.decrypt(encrypted[0], tampered, key),
            "Un ciphertext falsifié doit être détecté par le tag GCM.");
    }

    @Test
    @DisplayName("decrypt() — IV invalide (longueur != 12) lève IllegalArgumentException")
    void decrypt_invalidIV_throwsIllegalArgumentException() throws Exception {
        SecretKey key = buildTestKey();
        byte[][] encrypted = cryptoService.encrypt(TEST_PLAINTEXT, key);

        byte[] badIV = new byte[8]; // IV trop court

        assertThrows(IllegalArgumentException.class,
            () -> cryptoService.decrypt(badIV, encrypted[1], key));
    }

    @Test
    @DisplayName("decrypt() — IV null lève IllegalArgumentException")
    void decrypt_nullIV_throwsIllegalArgumentException() throws Exception {
        SecretKey key = buildTestKey();
        byte[][] encrypted = cryptoService.encrypt(TEST_PLAINTEXT, key);

        assertThrows(IllegalArgumentException.class,
            () -> cryptoService.decrypt(null, encrypted[1], key));
    }

    // ══════════════════════════════════════════════════════════════
    // Effacement de clé
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("destroyKey() ne lève pas d'exception sur une clé valide")
    void destroyKey_validKey_noException() {
        SecretKey key = buildTestKey();
        assertDoesNotThrow(() -> cryptoService.destroyKey(key));
    }

    @Test
    @DisplayName("destroyKey() ne lève pas d'exception sur null")
    void destroyKey_null_noException() {
        assertDoesNotThrow(() -> cryptoService.destroyKey(null));
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    /** Construit une clé de test avec le mot de passe par défaut. */
    private SecretKey buildTestKey() {
        return buildTestKey(Arrays.copyOf(MASTER_PASSWORD, MASTER_PASSWORD.length));
    }

    /** Construit une clé de test avec un mot de passe fourni. */
    private SecretKey buildTestKey(char[] password) {
        String salt = cryptoService.generateSalt();
        return cryptoService.deriveKey(password, salt);
    }
}
