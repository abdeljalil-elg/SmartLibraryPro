# SmartLibrary Pro

Application JavaFX premium de gestion de bibliothèque intelligente, construite avec Java 21+, FXML, CSS JavaFX, JDBC, MySQL, MVC et DAO.

## Fonctionnalités principales

- Splash screen animé avec progression.
- Sidebar moderne, MenuBar, navigation fluide et raccourcis clavier.
- Dashboard avec KPI animés, PieChart, BarChart, LineChart et livres populaires.
- CRUD complet des livres avec recherche instantanée, filtres, RadioButton, CheckBox, Spinner, TextArea et export CSV.
- CRUD complet des emprunts avec DatePicker, retour intelligent et synchronisation automatique du stock.
- Notifications toast modernes, Alert JavaFX et Dialog personnalisé.
- Paramètres persistants: thème clair/sombre, ColorPicker, Slider de taille de police, activation des animations.
- Centre d'aide avec Accordion, TitledPane, FAQ et raccourcis.
- Script MySQL complet avec contraintes, clés étrangères, index et données de démonstration.

## Prérequis

- Java 21 ou plus récent.
- MySQL 8 ou plus récent.
- IntelliJ IDEA.
- Maven. IntelliJ peut utiliser son Maven intégré si `mvn` n'est pas installé globalement.

## Installation de la base de données

1. Ouvrir MySQL Workbench, phpMyAdmin ou un terminal MySQL.
2. Exécuter le script:

```sql
source database/init_db.sql;
```

3. Vérifier la configuration JDBC dans:

```text
src/main/resources/database.properties
```

Valeurs par défaut:

```properties
db.url=jdbc:mysql://localhost:3306/smart_library?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
db.user=root
db.password=
```

Vous pouvez aussi surcharger ces valeurs avec les variables d'environnement:

- `SMARTLIB_DB_URL`
- `SMARTLIB_DB_USER`
- `SMARTLIB_DB_PASSWORD`

## Lancement dans IntelliJ IDEA

1. Ouvrir le dossier `SmartLibraryPro` dans IntelliJ IDEA.
2. Attendre l'import Maven.
3. Vérifier que le SDK du projet est Java 21 ou plus récent.
4. Lancer la configuration Maven:

```bash
mvn javafx:run
```

Alternative: ouvrir `MainApp.java` et exécuter la classe `com.smartlibrary.app.MainApp`.

## Structure

```text
src/main/java/com/smartlibrary
├── app          # MainApp et contexte applicatif
├── config       # Configuration MySQL et préférences utilisateur
├── controller   # Contrôleurs FXML
├── dao          # Interfaces DAO
├── dao/jdbc     # Implémentations JDBC MySQL
├── model        # Entités Livre, Emprunt, métriques, notifications
├── service      # Services métier, CSV, notifications
└── util         # Animations, dialogues, validation

src/main/resources/com/smartlibrary
├── view         # Fichiers FXML
└── style        # CSS JavaFX premium

database/init_db.sql
```
## Diagrammes UML

Le dossier `diagrams/` contient les diagrammes UML réalisés durant la phase de conception de l'application SmartLibrary Pro.

### Contenu du diagramms

- `class_diagram.puml` : diagramme de classes représentant les principales entités du système, leurs attributs et les relations entre elles.
- `use_case.puml` : diagramme de cas d'utilisation illustrant les interactions entre l'utilisateur et l'application.
- `sequence_diagram.puml` : diagramme de séquence décrivant le déroulement des principales opérations, notamment les fonctionnalités CRUD et les interactions avec la base de données.

Ces diagrammes ont été réalisés avec **PlantUML** et servent à documenter l'architecture et le fonctionnement de l'application avant son implémentation.

#### Méthode de visualisation des diagrammes : Extension VS Code (Recommandée)

1. Installer l'extension **PlantUML**.
2. Ouvrir un fichier `.puml`.
3. Appuyer sur :

```text
Alt + D
```

pour prévisualiser le diagramme.


## Notes 

L'application reste ouvrable même si MySQL n'est pas encore lancé: elle affiche une notification d'erreur au lieu de crasher. Dès que `init_db.sql` est importé et que `database.properties` est correct, toutes les fonctionnalités DAO/JDBC deviennent actives.
## Vidéo de démonstration

Lien Google Drive :

https://drive.google.com/drive/folders/1vHHf30suFkk9h5lh4H50Wq4gJ5Gt3ggP

La vidéo présente :

L’interface de l’application
La navigation
Le CRUD des livres
Le CRUD des emprunts
Les statistiques
L’export CSV
Les principaux contrôles JavaFX utilisés