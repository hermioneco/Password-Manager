/**
 * Module principal de l'application LockBox.
 *
 * <p>Définit les dépendances du module Java (JPMS) pour
 * JavaFX, Spring, Hibernate, SQLite et les bibliothèques
 * de sécurité.</p>
 *
 * @author  Base de données / DevOps
 * @version 1.0-SNAPSHOT
 */
module com.lockbox{

    // ── JavaFX ──────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // ── UI extras ───────────────────────────────────────
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    // ── Spring Boot / Spring Data ────────────────────────
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.tx;

    // ── JPA / Hibernate ─────────────────────────────────
    requires jakarta.persistence;
    requires jakarta.annotation;
    requires org.hibernate.orm.core;

    // ── Sécurité ────────────────────────────────────────
    requires de.mkammerer.argon2;
    //requires com.sun.jna;
    requires com.nulabinc.zxcvbn;

    // ── Java standard ───────────────────────────────────
    requires java.sql;
    requires java.desktop;   // java.awt.Toolkit (presse-papier)
    requires java.logging;
    requires java.instrument; // requis par Hibernate (ClassFileTransformer)
    requires java.naming;     // requis par Hibernate (JNDI lookups internes)
    requires de.mkammerer.argon2.nolibs;

    // ── Ouvrir les packages pour JavaFX (injection FXML) ──
    opens com.lockbox to javafx.fxml, spring.core, spring.beans, spring.context;
    opens com.lockbox.controller to javafx.fxml, spring.beans, spring.context;
    opens com.lockbox.model to org.hibernate.orm.core, jakarta.persistence, spring.core;
    opens com.lockbox.services to spring.beans, spring.context, spring.core;
    opens com.lockbox.repository to spring.beans, spring.context, spring.data.jpa;
    opens com.lockbox.util to spring.beans, spring.context, spring.core;

    // ── Exporter les packages publics ───────────────────
    exports com.lockbox;
    exports com.lockbox.controller;
    exports com.lockbox.model;
    exports com.lockbox.services;
    exports com.lockbox.repository;
    exports com.lockbox.util;
}