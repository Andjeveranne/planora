-- Script de création des tables pour Course et Classe
-- À exécuter sur la base de données plannoradb

USE plannoradb;

-- Table des Classes
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_classe VARCHAR(100) NOT NULL UNIQUE,
    niveau VARCHAR(50) NOT NULL,
    filiere VARCHAR(100) NOT NULL,
    annee_academique VARCHAR(20) NOT NULL,
    nombre_etudiants INT NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_classes_niveau (niveau),
    INDEX idx_classes_filiere (filiere),
    INDEX idx_classes_annee (annee_academique),
    INDEX idx_classes_active (active)
);

-- Table des Courses
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code_course VARCHAR(50) NOT NULL UNIQUE,
    type_cours VARCHAR(20) NOT NULL,
    nombre_heures INT NOT NULL,
    semestre VARCHAR(10) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Clés étrangères
    ue_id BIGINT NOT NULL,
    enseignant_id VARCHAR(255) NOT NULL,
    classe_id BIGINT NOT NULL,
    
    -- Contraintes de clés étrangères
    FOREIGN KEY (ue_id) REFERENCES ues(id) ON DELETE CASCADE,
    FOREIGN KEY (enseignant_id) REFERENCES enseignants(id_user) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE,
    
    -- Index pour les performances
    INDEX idx_courses_ue (ue_id),
    INDEX idx_courses_enseignant (enseignant_id),
    INDEX idx_courses_classe (classe_id),
    INDEX idx_courses_type (type_cours),
    INDEX idx_courses_semestre (semestre),
    INDEX idx_courses_active (active)
);

-- Ajouter la colonne course_id à la table emplois_du_temps si elle n'existe pas
ALTER TABLE emplois_du_temps 
ADD COLUMN course_id BIGINT;

-- Ajouter la contrainte de clé étrangère
ALTER TABLE emplois_du_temps 
ADD CONSTRAINT fk_emplois_du_temps_course 
FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL;

-- Insertion de données de test pour les classes
INSERT IGNORE INTO classes (nom_classe, niveau, filiere, annee_academique, nombre_etudiants, description) VALUES
-- Licence 1
('L1-INFO-A', 'L1', 'Informatique', '2023-2024', 35, 'Classe de première année informatique - Groupe A'),
('L1-INFO-B', 'L1', 'Informatique', '2023-2024', 32, 'Classe de première année informatique - Groupe B'),
('L1-MATH-A', 'L1', 'Mathématiques', '2023-2024', 28, 'Classe de première année mathématiques - Groupe A'),

-- Licence 2
('L2-INFO-A', 'L2', 'Informatique', '2023-2024', 30, 'Classe de deuxième année informatique - Groupe A'),
('L2-INFO-B', 'L2', 'Informatique', '2023-2024', 29, 'Classe de deuxième année informatique - Groupe B'),
('L2-MATH-A', 'L2', 'Mathématiques', '2023-2024', 25, 'Classe de deuxième année mathématiques - Groupe A'),

-- Licence 3
('L3-INFO-A', 'L3', 'Informatique', '2023-2024', 27, 'Classe de troisième année informatique - Groupe A'),
('L3-INFO-B', 'L3', 'Informatique', '2023-2024', 26, 'Classe de troisième année informatique - Groupe B'),
('L3-MATH-A', 'L3', 'Mathématiques', '2023-2024', 22, 'Classe de troisième année mathématiques - Groupe A'),

-- Master 1
('M1-INFO-A', 'M1', 'Informatique', '2023-2024', 24, 'Classe de première année master informatique - Groupe A'),
('M1-INFO-B', 'M1', 'Informatique', '2023-2024', 23, 'Classe de première année master informatique - Groupe B'),
('M1-MATH-A', 'M1', 'Mathématiques', '2023-2024', 20, 'Classe de première année master mathématiques - Groupe A'),

-- Master 2
('M2-INFO-A', 'M2', 'Informatique', '2023-2024', 22, 'Classe de deuxième année master informatique - Groupe A'),
('M2-INFO-B', 'M2', 'Informatique', '2023-2024', 21, 'Classe de deuxième année master informatique - Groupe B'),
('M2-MATH-A', 'M2', 'Mathématiques', '2023-2024', 18, 'Classe de deuxième année master mathématiques - Groupe A');

-- Insertion de données de test pour les courses
-- Note: Ces insertions nécessitent que les UEs et enseignants existent déjà
-- Vous devrez adapter les IDs selon votre base de données

-- Exemples de courses (à adapter selon vos données existantes)
INSERT IGNORE INTO courses (code_course, type_cours, nombre_heures, semestre, description, ue_id, enseignant_id, classe_id) 
SELECT 
    CONCAT(u.code_ue, '-CM-', c.nom_classe) as code_course,
    'CM' as type_cours,
    u.heures_cm as nombre_heures,
    u.semestre,
    CONCAT('Cours magistral de ', u.nom_ue, ' pour la classe ', c.nom_classe) as description,
    u.id as ue_id,
    e.id_user as enseignant_id,
    c.id as classe_id
FROM ues u
CROSS JOIN classes c
CROSS JOIN enseignants e
WHERE u.active = TRUE 
  AND c.active = TRUE
  AND u.heures_cm > 0
  AND (
    (u.niveau = c.niveau) OR 
    (u.niveau = 'L1' AND c.niveau IN ('L1', 'L2')) OR
    (u.niveau = 'L2' AND c.niveau IN ('L2', 'L3')) OR
    (u.niveau = 'M1' AND c.niveau IN ('M1', 'M2'))
  )
LIMIT 20; -- Limiter pour éviter trop d'insertions

-- Vérification des données insérées
SELECT 'Classes créées:' as info, COUNT(*) as count FROM classes WHERE active = TRUE
UNION ALL
SELECT 'Courses créées:' as info, COUNT(*) as count FROM courses WHERE active = TRUE;

-- Affichage des relations créées
SELECT 
    c.code_course,
    c.type_cours,
    u.code_ue,
    u.nom_ue,
    CONCAT(ut.prenom_user, ' ', ut.nom_user) as enseignant,
    cl.nom_classe,
    cl.niveau
FROM courses c
JOIN ues u ON c.ue_id = u.id
JOIN enseignants e ON c.enseignant_id = e.id_user
JOIN utilisateurs ut ON e.id_user = ut.id_user
JOIN classes cl ON c.classe_id = cl.id
WHERE c.active = TRUE
ORDER BY cl.niveau, cl.nom_classe, u.code_ue
LIMIT 10;

COMMIT;