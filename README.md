# LockBox — Coffre-fort de Mots de Passe Sécurisé

LockBox est une application desktop moderne développée en **Java (JavaFX)** avec **Spring Boot** et **SQLite**, permettant de stocker et de gérer vos identifiants en toute sécurité de manière locale.

---

## 📍 Où se trouve l'installeur (.exe) ?

L'installeur Windows généré est situé dans le dossier des livrables de votre projet :
👉 **[LockBox-1.0.exe (Installeur Windows)](file:///c:/Users/ashie/OneDrive/Documents/python%20files/password_manager/Password-Manager/target/dist/LockBox-1.0.exe)**

Vous pouvez également lancer la version portable sans installation via le dossier :
👉 **[Dossier de l'application portable](file:///c:/Users/ashie/OneDrive/Documents/python%20files/password_manager/Password-Manager/target/dist/LockBox/)** (lancez `LockBox.exe`).

---

## 🔒 Sécurité et Chiffrement

L'application respecte les meilleures pratiques de sécurité actuelles (recommandations OWASP et NIST) :
* **Dérivation de Clé (KDF)** : Votre mot de passe maître est dérivé en clé AES-256 à l'aide de **PBKDF2 avec HMAC-SHA256** (100 000 itérations) et d'un sel unique de 12 octets propre à chaque utilisateur.
* **Hachage d'Authentification** : Le mot de passe maître est haché à l'aide d'**Argon2id** (3 itérations, 64 Mo de mémoire, 4 threads) pour l'authentification lors de la connexion, évitant de stocker le mot de passe ou d'exposer la clé de déchiffrement.
* **Chiffrement des Données** : Les mots de passe stockés, les identifiants et les notes associées sont chiffrés individuellement en **AES-256-GCM (NoPadding)** avec un vecteur d'initialisation (IV) de 12 octets aléatoire et unique généré par `SecureRandom`.
* **Sécurité Mémoire** : Les mots de passe et clés secrètes saisis sous forme de tableaux de caractères (`char[]`) sont effacés de la mémoire à l'aide de `Arrays.fill(..., '\0')` dès que le traitement est terminé.
* **Presse-papier sécurisé** : Les mots de passe copiés dans le presse-papier sont automatiquement effacés après quelques secondes.

---

## 📖 Guide d'Utilisation

### 1. Inscription et Premier Démarrage
1. Lancez l'application en double-cliquant sur **LockBox-1.0.exe** (ou sur le raccourci créé).
2. Sur l'écran de connexion, cliquez sur **Créer un compte**.
3. Remplissez votre adresse e-mail et choisissez un mot de passe maître.
   * *Un indicateur de force visuel (basé sur l'algorithme `zxcvbn`) vous aidera à choisir un mot de passe robuste.*
4. **Attention :** En raison du chiffrement local de bout en bout, il n'y a aucun serveur externe. Si vous perdez votre mot de passe maître, il est impossible de restaurer votre coffre-fort.

### 2. Connexion
1. Saisissez votre adresse e-mail et votre mot de passe maître.
2. Si les informations d'identification sont correctes, votre clé de chiffrement est temporairement chargée en mémoire (de façon sécurisée) et vous êtes redirigé vers votre coffre-fort.

### 3. Gestion des Identifiants (CRUD)
* **Ajouter un identifiant** : Cliquez sur le bouton **Ajouter** (icône `+`). Saisissez le nom du service, l'URL, le login, et le mot de passe.
  * *Vous pouvez également utiliser le bouton 🔑 **Générer** intégré au formulaire pour générer un mot de passe aléatoire hautement sécurisé.*
* **Rechercher** : Utilisez la barre de recherche en haut pour filtrer instantanément par nom de service, identifiant ou URL.
* **Copier** : Cliquez sur l'icône de presse-papier (📋) sur la ligne correspondante. Une notification toast s'affiche et le mot de passe est copié. Il sera automatiquement nettoyé du presse-papier après 15 secondes pour éviter le vol accidentel.
* **Afficher** : Cliquez sur l'icône d'œil (👁) pour ouvrir une boîte de dialogue affichant les détails complets de l'entrée en clair (y compris le mot de passe).
* **Supprimer** : Cliquez sur la corbeille rouge (🗑) pour supprimer définitivement une entrée (après confirmation).

### 4. Déconnexion et Verrouillage
* Cliquez sur le bouton de déconnexion. La session est immédiatement fermée, et la clé secrète ainsi que le presse-papier sont effacés de la mémoire.

---

## 🛠 Guide de Développement

### Configuration requise
* **Java Development Kit (JDK)** version 21 ou supérieure.
* **Maven** (inclus via le wrapper `mvnw`).

### Lancer l'application en mode développement
```bash
./mvnw clean javafx:run
```

### Lancer les tests unitaires
```bash
./mvnw clean test
```

### Compiler et empaqueter sous forme de JAR
```bash
./mvnw clean package -DskipTests
```

### Recréer l'exécutable et l'installeur Windows (.exe)
Pour régénérer les livrables d'installation :
1. Copiez les dépendances :
   ```bash
   ./mvnw dependency:copy-dependencies -DoutputDirectory=target/libs
   ```
2. Copiez le JAR principal :
   ```bash
   copy target/lockbox-1.0-SNAPSHOT.jar target/libs/
   ```
3. Exécutez `jpackage` :
   ```bash
   jpackage --name LockBox --input target/libs --main-jar lockbox-1.0-SNAPSHOT.jar --main-class com.lockbox.Launcher --type exe --dest target/dist --icon src/main/resources/com/lockbox/treasure.ico
   ```
