# Rapport des fonctionnalités

## Objectif

SmartLibrary Pro est une application JavaFX de gestion de bibliothèque moderne, pensée comme un logiciel professionnel quotidien plutôt qu'un mini-projet.

## Architecture

- Java 21 avec Maven.
- JavaFX Controls et FXML.
- CSS JavaFX séparé.
- Architecture MVC.
- Pattern DAO avec interfaces et implémentations JDBC.
- Service métier central pour garantir la cohérence du stock.
- MySQL comme base relationnelle.

## Entités

### Livre

Champs: titre, auteur, ISBN, catégorie, éditeur, année, description, quantité, disponibilité, nombre d'emprunts.

Fonctionnalités: ajout, modification, suppression, consultation, recherche instantanée, filtrage par catégorie, disponibilité et statut.

### Emprunt

Champs: livre, emprunteur, email, téléphone, date d'emprunt, date de retour prévue, date de retour effective, statut, notes.

Fonctionnalités: création, modification, suppression, retour, recherche, filtre par statut et export CSV.

## Gestion intelligente du stock

- À la création d'un emprunt, la quantité du livre diminue automatiquement dans une transaction SQL.
- Quand la quantité atteint zéro, le livre devient indisponible.
- Lors d'un retour, la quantité augmente automatiquement.
- Lors de la suppression d'un emprunt actif, le stock est restauré.
- Les retards sont recalculés avant l'affichage des emprunts et statistiques.

## Interface et expérience utilisateur

- Splash screen premium avec ProgressIndicator et ProgressBar.
- Sidebar avec icônes, MenuBar et navigation fluide.
- Dashboard avec cartes KPI animées.
- Graphiques JavaFX: PieChart, BarChart, LineChart.
- Notifications toast avec animations.
- Alert et Dialog JavaFX.
- Préférences persistantes: thème, couleur d'accent, taille de police et animations.
- Centre d'aide avec Accordion, TitledPane, FAQ et raccourcis.

## Contrôles JavaFX intégrés

TextField, TextArea, Button, Label, RadioButton, ToggleGroup, CheckBox, ComboBox, ListView, TableView, DatePicker, Slider, Spinner, ProgressBar, ProgressIndicator, Tooltip, MenuBar, Alert, Dialog, Accordion, TitledPane, ColorPicker et FileChooser.

## Base de données

Le fichier `database/init_db.sql` crée:

- La base `smart_library`.
- La table `livres`.
- La table `emprunts`.
- Les clés étrangères.
- Les contraintes CHECK.
- Les index de recherche.
- Des données de démonstration cohérentes.

## Exports

Les pages Livres et Emprunts exportent des fichiers CSV exploitables avec séparateur `;` et encodage UTF-8.

## Limites volontaires

L'application n'intègre pas de système d'authentification pour rester centrée sur les entités demandées: Livre et Emprunt. La structure permet toutefois d'ajouter un module Utilisateur sans modifier les DAO existants.
