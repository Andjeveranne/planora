-- Script SQL pour créer la table des disponibilités des enseignants
-- À exécuter après le démarrage de l'application Spring Boot

USE PlannoraDB;

-- Création de la table disponibilites si elle n'existe pas déjà
-- (Normalement créée automatiquement par Hibernate avec ddl-auto=update)
CREATE TABLE IF NOT EXISTS disponibilites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jour_semaine VARCHAR(20) NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    enseignant_id BIGINT NOT NULL,
    FOREIGN KEY (enseignant_id) REFERENCES enseignants(id) ON DELETE CASCADE,
    INDEX idx_enseignant_jour (enseignant_id, jour_semaine),
    INDEX idx_jour_heure (jour_semaine, heure_debut, heure_fin)
);

-- Données de test pour les disponibilités
-- Enseignant ID 1 (si il existe)
INSERT IGNORE INTO disponibilites (jour_semaine, heure_debut, heure_fin, enseignant_id) VALUES
-- Lundi
('MONDAY', '08:00:00', '10:00:00', 1),
('MONDAY', '14:00:00', '16:00:00', 1),
-- Mardi
('TUESDAY', '09:00:00', '11:00:00', 1),
('TUESDAY', '15:00:00', '17:00:00', 1),
-- Mercredi
('WEDNESDAY', '08:00:00', '12:00:00', 1),
-- Jeudi
('THURSDAY', '10:00:00', '12:00:00', 1),
('THURSDAY', '14:00:00', '18:00:00', 1),
-- Vendredi
('FRIDAY', '08:00:00', '10:00:00', 1);

-- Enseignant ID 2 (si il existe)
INSERT IGNORE INTO disponibilites (jour_semaine, heure_debut, heure_fin, enseignant_id) VALUES
-- Lundi
('MONDAY', '10:00:00', '12:00:00', 2),
('MONDAY', '16:00:00', '18:00:00', 2),
-- Mardi
('TUESDAY', '08:00:00', '10:00:00', 2),
('TUESDAY', '13:00:00', '15:00:00', 2),
-- Mercredi
('WEDNESDAY', '14:00:00', '18:00:00', 2),
-- Jeudi
('THURSDAY', '08:00:00', '10:00:00', 2),
-- Vendredi
('FRIDAY', '10:00:00', '12:00:00', 2),
('FRIDAY', '14:00:00', '16:00:00', 2);

-- Vérification des données insérées
SELECT 
    d.id,
    e.nom,
    e.prenom,
    d.jour_semaine,
    d.heure_debut,
    d.heure_fin
FROM disponibilites d
JOIN enseignants e ON d.enseignant_id = e.id
ORDER BY e.nom, d.jour_semaine, d.heure_debut;