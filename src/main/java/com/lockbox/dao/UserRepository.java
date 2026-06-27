package com.lockbox.dao;

import com.lockbox.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité {@link User}.
 *
 * <p>Spring génère automatiquement l'implémentation SQL via Hibernate/SQLite.
 * Aucun SQL brut nécessaire pour les opérations CRUD standard.</p>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * <p>Utilisé par {@code AuthService} lors de la connexion pour
     * récupérer le hash Argon2id et le sel PBKDF2.</p>
     *
     * @param email adresse email de l'utilisateur
     * @return {@link Optional} contenant l'utilisateur si trouvé
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email est déjà enregistré.
     *
     * <p>Utilisé lors de l'inscription pour empêcher les doublons.</p>
     *
     * @param email adresse email à vérifier
     * @return {@code true} si l'email existe déjà
     */
    boolean existsByEmail(String email);
}
