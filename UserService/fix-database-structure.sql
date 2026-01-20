-- Script pour corriger la structure de la base de données
-- À exécuter sur la base de données plannoradb

USE plannoradb;

-- Vérifier et créer la table classes si elle n'existe pas
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

-- Vérifier et créer la table courses si elle n'existe pas
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
    ue_id BIGINT,
    enseignant_id VARCHAR(255),
    classe_id BIGINT,
    
    -- Index pour les performances
    INDEX idx_courses_ue (ue_id),
    INDEX idx_courses_enseignant (enseignant_id),
    INDEX idx_courses_classe (classe_id),
    INDEX idx_courses_type (type_cours),
    INDEX idx_courses_semestre (semestre),
    INDEX idx_courses_active (active)
);

-- Ajouter les contraintes de clés étrangères si elles n'existent pas
-- Vérifier d'abord si les contraintes existent
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'plannoradb'
    AND TABLE_NAME = 'courses'
    AND CONSTRAINT_NAME = 'fk_courses_ue'
);

-- Ajouter la contrainte UE si elle n'existe pas
SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE courses ADD CONSTRAINT fk_courses_ue FOREIGN KEY (ue_id) REFERENCES ues(id) ON DELETE CASCADE',
    'SELECT "Contrainte fk_courses_ue existe déjà" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Vérifier et ajouter la contrainte enseignant
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'plannoradb'
    AND TABLE_NAME = 'courses'
    AND CONSTRAINT_NAME = 'fk_courses_enseignant'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE courses ADD CONSTRAINT fk_courses_enseignant FOREIGN KEY (enseignant_id) REFERENCES enseignants(id_user) ON DELETE CASCADE',
    'SELECT "Contrainte fk_courses_enseignant existe déjà" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Vérifier et ajouter la contrainte classe
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'plannoradb'
    AND TABLE_NAME = 'courses'
    AND CONSTRAINT_NAME = 'fk_courses_classe'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE courses ADD CONSTRAINT fk_courses_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE',
    'SELECT "Contrainte fk_courses_classe existe déjà" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ajouter la colonne course_id à emplois_du_temps si elle n'existe pas
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'plannoradb'
    AND TABLE_NAME = 'emplois_du_temps'
    AND COLUMN_NAME = 'course_id'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE emplois_du_temps ADD COLUMN course_id BIGINT',
    'SELECT "Colonne course_id existe déjà" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ajouter la contrainte de clé étrangère pour course_id si elle n'existe pas
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'plannoradb'
    AND TABLE_NAME = 'emplois_du_temps'
    AND CONSTRAINT_NAME = 'fk_emplois_du_temps_course'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE emplois_du_temps ADD CONSTRAINT fk_emplois_du_temps_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL',
    'SELECT "Contrainte fk_emplois_du_temps_course existe déjà" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Insérer des données de test pour les classes si elles n'existent pas
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

-- Afficher le résultat
SELECT 'Tables créées et données insérées avec succès' as status;

-- Vérifier les tables créées
SELECT 
    'classes' as table_name, 
    COUNT(*) as record_count 
FROM classes 
WHERE active = TRUE
UNION ALL
SELECT 
    'courses' as table_name, 
    COUNT(*) as record_count 
FROM courses 
WHERE active = TRUE;

COMMIT;