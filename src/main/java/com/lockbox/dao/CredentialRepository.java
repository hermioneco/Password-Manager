package com.lockbox.dao;

import com.lockbox.model.Credential;
import com.lockbox.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Spring Data JPA pour l'entité {@link Credential}.
 *
 * <p>Fournit les requêtes nécessaires au CRUD des identifiants chiffrés.
 * Toutes les requêtes filtrent obligatoirement par {@code user} pour
 * garantir l'isolation des données entre utilisateurs.</p>
 *
 * @author Personne C — Base de données / DevOps
 * @version 1.0
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Retourne tous les credentials appartenant à un utilisateur,
     * triés par nom de service alphabétiquement.
     *
     * <p>C'est la requête principale utilisée par le Dashboard
     * pour charger la liste initiale dans l'ObservableList JavaFX.</p>
     *
     * @param user propriétaire des credentials
     * @return liste triée des credentials (données chiffrées)
     */
    List<Credential> findByUserOrderByServiceNameAsc(User user);

    /**
     * Retourne les credentials d'un utilisateur filtrés par catégorie.
     *
     * <p>Utilisé par le filtre catégorie du Dashboard.</p>
     *
     * @param user     propriétaire
     * @param category catégorie à filtrer (ex. : "Email", "Banque")
     * @return credentials de la catégorie, triés par service
     */
    List<Credential> findByUserAndCategoryOrderByServiceNameAsc(User user, String category);

    /**
     * Recherche en texte libre sur le nom du service (insensible à la casse).
     *
     * <p>Utilisé par la barre de recherche du Dashboard.
     * Note : seul {@code serviceName} est en clair — login et mot de passe
     * ne peuvent pas être recherchés sans déchiffrement préalable.</p>
     *
     * @param user        propriétaire
     * @param serviceName terme de recherche (partiel, insensible à la casse)
     * @return credentials dont le service contient le terme
     */
    @Query("SELECT c FROM Credential c WHERE c.user = :user " +
           "AND LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :serviceName, '%')) " +
           "ORDER BY c.serviceName ASC")
    List<Credential> searchByServiceName(@Param("user") User user,
                                         @Param("serviceName") String serviceName);

    /**
     * Retourne la liste distincte des catégories d'un utilisateur.
     *
     * <p>Utilisé pour peupler le ComboBox de filtre dans le Dashboard.</p>
     *
     * @param user propriétaire
     * @return liste des catégories utilisées (sans doublons)
     */
    @Query("SELECT DISTINCT c.category FROM Credential c WHERE c.user = :user " +
           "AND c.category IS NOT NULL ORDER BY c.category ASC")
    List<String> findDistinctCategoriesByUser(@Param("user") User user);

    /**
     * Compte le nombre de credentials appartenant à un utilisateur.
     *
     * <p>Affiché dans le header du Dashboard.</p>
     *
     * @param user propriétaire
     * @return nombre total de credentials
     */
    long countByUser(User user);

    /**
     * Supprime tous les credentials d'un utilisateur.
     *
     * <p>Utilisé lors de la suppression de compte (fonctionnalité v1.1).</p>
     *
     * @param user propriétaire
     */
    void deleteAllByUser(User user);
}
