DROP DATABASE IF EXISTS smart_library;
CREATE DATABASE smart_library
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_library;

CREATE TABLE livres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(180) NOT NULL,
    auteur VARCHAR(140) NOT NULL,
    isbn VARCHAR(32) NOT NULL,
    categorie VARCHAR(90) NOT NULL,
    editeur VARCHAR(120),
    annee_publication INT,
    description TEXT,
    quantite INT NOT NULL DEFAULT 0,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    nombre_emprunts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_livres_isbn UNIQUE (isbn),
    CONSTRAINT chk_livres_quantite CHECK (quantite >= 0),
    CONSTRAINT chk_livres_annee CHECK (annee_publication IS NULL OR annee_publication BETWEEN 1000 AND 2100)
);

CREATE TABLE emprunts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    livre_id INT NOT NULL,
    emprunteur_nom VARCHAR(140) NOT NULL,
    emprunteur_email VARCHAR(160),
    emprunteur_telephone VARCHAR(40),
    date_emprunt DATE NOT NULL,
    date_retour_prevue DATE NOT NULL,
    date_retour_effective DATE,
    statut ENUM('ACTIF', 'RETOURNE', 'RETARD') NOT NULL DEFAULT 'ACTIF',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emprunts_livre
        FOREIGN KEY (livre_id)
        REFERENCES livres(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_emprunts_dates CHECK (date_retour_prevue >= date_emprunt),
    CONSTRAINT chk_emprunts_retour CHECK (date_retour_effective IS NULL OR date_retour_effective >= date_emprunt)
);

CREATE INDEX idx_livres_titre ON livres(titre);
CREATE INDEX idx_livres_auteur ON livres(auteur);
CREATE INDEX idx_livres_categorie ON livres(categorie);
CREATE INDEX idx_livres_disponible ON livres(disponible);
CREATE INDEX idx_emprunts_statut ON emprunts(statut);
CREATE INDEX idx_emprunts_dates ON emprunts(date_emprunt, date_retour_prevue);
CREATE INDEX idx_emprunts_emprunteur ON emprunts(emprunteur_nom);

INSERT INTO livres
(titre, auteur, isbn, categorie, editeur, annee_publication, description, quantite, disponible, nombre_emprunts)
VALUES
('Clean Code', 'Robert C. Martin', '9780132350884', 'Informatique', 'Prentice Hall', 2008,
 'Pratiques professionnelles pour écrire du code maintenable.', 0, FALSE, 18),
('Designing Data-Intensive Applications', 'Martin Kleppmann', '9781449373320', 'Informatique', 'O''Reilly', 2017,
 'Architecture des systèmes de données modernes.', 3, TRUE, 14),
('Le Petit Prince', 'Antoine de Saint-Exupéry', '9782070612758', 'Littérature', 'Gallimard', 1943,
 'Conte poétique et philosophique.', 6, TRUE, 21),
('Sapiens', 'Yuval Noah Harari', '9780062316097', 'Histoire', 'Harper', 2014,
 'Une brève histoire de l''humanité.', 2, TRUE, 9),
('Atomic Habits', 'James Clear', '9780735211292', 'Développement personnel', 'Avery', 2018,
 'Méthode pratique pour construire de bonnes habitudes.', 4, TRUE, 11),
('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', '9780201616224', 'Informatique', 'Addison-Wesley', 1999,
 'Guide de carrière et de qualité pour développeurs.', 1, TRUE, 16),
('Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 'Psychologie', 'Farrar, Straus and Giroux', 2011,
 'Exploration des deux systèmes de pensée.', 5, TRUE, 7),
('Le Comte de Monte-Cristo', 'Alexandre Dumas', '9782253004226', 'Littérature', 'Le Livre de Poche', 1844,
 'Roman d''aventure, de justice et de vengeance.', 2, TRUE, 12),
('Introduction to Algorithms', 'Thomas H. Cormen', '9780262046305', 'Informatique', 'MIT Press', 2022,
 'Référence complète sur les algorithmes.', 0, FALSE, 8),
('Les Misérables', 'Victor Hugo', '9782253096337', 'Littérature', 'Le Livre de Poche', 1862,
 'Fresque sociale et romanesque majeure.', 3, TRUE, 10);

INSERT INTO emprunts
(livre_id, emprunteur_nom, emprunteur_email, emprunteur_telephone, date_emprunt, date_retour_prevue, date_retour_effective, statut, notes)
VALUES
(1, 'Nadia El Amrani', 'nadia.elamrani@example.com', '+212 600 100 101', CURRENT_DATE - INTERVAL 18 DAY, CURRENT_DATE - INTERVAL 4 DAY, NULL, 'RETARD', 'Relance prioritaire.'),
(9, 'Youssef Benali', 'youssef.benali@example.com', '+212 600 100 102', CURRENT_DATE - INTERVAL 7 DAY, CURRENT_DATE + INTERVAL 7 DAY, NULL, 'ACTIF', 'Consultation pour projet algorithmique.'),
(6, 'Meryem Alaoui', 'meryem.alaoui@example.com', '+212 600 100 103', CURRENT_DATE - INTERVAL 2 DAY, CURRENT_DATE + INTERVAL 12 DAY, NULL, 'ACTIF', 'Exemplaire en très bon état.'),
(3, 'Hicham Idrissi', 'hicham.idrissi@example.com', '+212 600 100 104', CURRENT_DATE - INTERVAL 25 DAY, CURRENT_DATE - INTERVAL 11 DAY, CURRENT_DATE - INTERVAL 8 DAY, 'RETOURNE', 'Retour sans incident.'),
(5, 'Sara Mansouri', 'sara.mansouri@example.com', '+212 600 100 105', CURRENT_DATE - INTERVAL 36 DAY, CURRENT_DATE - INTERVAL 20 DAY, CURRENT_DATE - INTERVAL 19 DAY, 'RETOURNE', 'Historique de démonstration.'),
(8, 'Omar Bekkali', 'omar.bekkali@example.com', '+212 600 100 106', CURRENT_DATE - INTERVAL 10 DAY, CURRENT_DATE + INTERVAL 4 DAY, NULL, 'ACTIF', 'Retour attendu cette semaine.');
