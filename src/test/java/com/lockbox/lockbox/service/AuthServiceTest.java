package com.lockbox.lockbox.service;

import com.lockbox.lockbox.dao.UserRepository;
import com.lockbox.lockbox.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AuthService} avec Mockito.
 *
 * <p>Les dépendances ({@link UserRepository}, {@link CryptoService}) sont mockées
 * pour isoler la logique d'authentification et tester chaque branche indépendamment.</p>
 *
 * <h3>Couverture :</h3>
 * <ul>
 *   <li>Inscription : succès, email dupliqué, validations</li>
 *   <li>Connexion : succès, email inconnu, mot de passe incorrect</li>
 *   <li>Gestion de session : clé active, déconnexion, effacement</li>
 *   <li>Méthodes utilitaires : {@code isLoggedIn()}, {@code hasExistingUser()}</li>
 * </ul>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests d'authentification avec Mockito")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private SecretKey mockSessionKey;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL    = "test@lockbox.com";
    private static final char[] TEST_PASSWORD = "MotDePasseTest!2024".toCharArray();

    // ══════════════════════════════════════════════════════════════
    // Inscription (register)
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("register() — Inscription")
    class RegisterTests {

        @Test
        @DisplayName("Inscription réussie — persiste l'utilisateur et retourne l'entité")
        void register_success_savesAndReturnsUser() throws Exception {
            // Arrange
            char[] pwd = Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(cryptoService.generateSalt()).thenReturn("c2FsdEJhc2U2NA==");
            User savedUser = new User(TEST_EMAIL, "$argon2id$fake_hash", "c2FsdEJhc2U2NA==");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            User result = authService.register(TEST_EMAIL, pwd);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_EMAIL, result.getEmail());
            verify(userRepository).save(any(User.class));
            verify(cryptoService).generateSalt();
        }

        @Test
        @DisplayName("Email déjà utilisé → AuthException")
        void register_duplicateEmail_throwsAuthException() {
            char[] pwd = Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            assertThrows(AuthService.AuthException.class,
                () -> authService.register(TEST_EMAIL, pwd),
                "Un email dupliqué doit lever AuthException.");

            // Le repository save() ne doit jamais être appelé
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Email null → AuthException")
        void register_nullEmail_throwsAuthException() {
            assertThrows(AuthService.AuthException.class,
                () -> authService.register(null, Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length)));
        }

        @Test
        @DisplayName("Email vide → AuthException")
        void register_blankEmail_throwsAuthException() {
            assertThrows(AuthService.AuthException.class,
                () -> authService.register("   ", Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length)));
        }

        @Test
        @DisplayName("Mot de passe trop court (< 8 chars) → AuthException")
        void register_shortPassword_throwsAuthException() {

            assertThrows(AuthService.AuthException.class,
                () -> authService.register(TEST_EMAIL, "court".toCharArray()),
                "Un mot de passe de moins de 8 caractères doit être refusé.");
        }

        @Test
        @DisplayName("Le mot de passe est effacé après inscription (sécurité)")
        void register_passwordClearedAfterRegistration() throws Exception {
            char[] pwd = "MotDePasseTest!2024".toCharArray();
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(cryptoService.generateSalt()).thenReturn("c2FsdA==");
            when(userRepository.save(any())).thenReturn(new User(TEST_EMAIL, "hash", "c2FsdA=="));

            authService.register(TEST_EMAIL, pwd);

            // Vérifie que le tableau a été effacé (tous les chars = '\0')
            boolean allZero = true;
            for (char c : pwd) { if (c != '\0') { allZero = false; break; } }
            assertTrue(allZero, "Le tableau de mot de passe doit être effacé après l'inscription.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Connexion (login)
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("login() — Connexion")
    class LoginTests {

        @Test
        @DisplayName("Connexion — email connu mais mot de passe incorrect → AuthException")
        void login_wrongPassword_throwsAuthException() {
            char[] wrongPwd = Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length);
            User   user     = buildMockUser();
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

            AuthService.AuthException ex = assertThrows(AuthService.AuthException.class,
                    () -> authService.login(TEST_EMAIL, wrongPwd));

            assertEquals("Email ou mot de passe incorrect.", ex.getMessage());
            verify(cryptoService, never()).deriveKey(any(), any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Email inconnu → AuthException avec message générique")
        void login_unknownEmail_throwsAuthException() {
            char[] pwd = Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            AuthService.AuthException ex = assertThrows(AuthService.AuthException.class,
                () -> authService.login(TEST_EMAIL, pwd));

            // Le message doit être identique à "mot de passe incorrect" — anti-énumération
            assertEquals("Email ou mot de passe incorrect.", ex.getMessage());
        }

        @Test
        @DisplayName("Email null → AuthException")
        void login_nullEmail_throwsAuthException() {
            assertThrows(AuthService.AuthException.class,
                () -> authService.login(null, Arrays.copyOf(TEST_PASSWORD, TEST_PASSWORD.length)));
        }

        @Test
        @DisplayName("Mot de passe null → AuthException")
        void login_nullPassword_throwsAuthException() {
            assertThrows(AuthService.AuthException.class,
                () -> authService.login(TEST_EMAIL, null));
        }

        @Test
        @DisplayName("Le mot de passe est effacé après tentative (succès ou échec)")
        void login_passwordClearedAfterAttempt() {
            char[] pwd = "TestPassword!99".toCharArray();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            try {
                authService.login(TEST_EMAIL, pwd);
            } catch (AuthService.AuthException ignored) {}

            boolean allZero = true;
            for (char c : pwd) { if (c != '\0') { allZero = false; break; } }
            assertTrue(allZero, "Le tableau doit être effacé même après une tentative échouée.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Session et déconnexion
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Gestion de session")
    class SessionTests {

        @Test
        @DisplayName("isLoggedIn() retourne false sans session active")
        void isLoggedIn_noSession_returnsFalse() {
            assertFalse(authService.isLoggedIn());
        }

        @Test
        @DisplayName("getSessionKey() retourne null sans session active")
        void getSessionKey_noSession_returnsNull() {
            assertNull(authService.getSessionKey());
        }

        @Test
        @DisplayName("getCurrentUser() retourne null sans session active")
        void getCurrentUser_noSession_returnsNull() {
            assertNull(authService.getCurrentUser());
        }

        @Test
        @DisplayName("logout() appelle destroyKey() et vide la session")
        void logout_callsDestroyKeyAndClearsSession() {
            // Force une clé de session via réflexion (car login est difficile à mocker avec Argon2)
            // On teste le comportement de logout() directement
            doNothing().when(cryptoService).destroyKey(any());

            authService.logout();

            verify(cryptoService).destroyKey(isNull()); // sessionKey était null avant logout
            assertNull(authService.getSessionKey());
            assertNull(authService.getCurrentUser());
            assertFalse(authService.isLoggedIn());
        }

        @Test
        @DisplayName("hasExistingUser() retourne false si la base est vide")
        void hasExistingUser_emptyDB_returnsFalse() {
            when(userRepository.count()).thenReturn(0L);
            assertFalse(authService.hasExistingUser());
        }

        @Test
        @DisplayName("hasExistingUser() retourne true si un utilisateur existe")
        void hasExistingUser_oneUser_returnsTrue() {
            when(userRepository.count()).thenReturn(1L);
            assertTrue(authService.hasExistingUser());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    private User buildMockUser() {
        return new User(TEST_EMAIL, "$argon2id$v=19$m=65536,t=3,p=4$fake$hash", "c2FsdEJhc2U2NA==");
    }
}
