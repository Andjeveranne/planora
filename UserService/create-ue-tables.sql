-- =====================================================
-- UE Management Database Schema Creation
-- =====================================================
-- This script creates the necessary tables for UE management

USE plannoradb;

-- =====================================================
-- Create UEs table
-- =====================================================
CREATE TABLE IF NOT EXISTS ues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code_ue VARCHAR(20) NOT NULL UNIQUE,
    nom_ue VARCHAR(255) NOT NULL,
    description TEXT,
    heures_cm INT NOT NULL DEFAULT 0,
    heures_td INT NOT NULL DEFAULT 0,
    heures_tp INT NOT NULL DEFAULT 0,
    credits_ects INT NOT NULL,
    semestre VARCHAR(10) NOT NULL,
    niveau VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    
    -- Indexes for performance
    INDEX idx_code_ue (code_ue),
    INDEX idx_semestre (semestre),
    INDEX idx_niveau (niveau),
    INDEX idx_active (active),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- Create junction table for Enseignant-UE relationship
-- =====================================================
CREATE TABLE IF NOT EXISTS enseignant_ue (
    enseignant_id VARCHAR(255) NOT NULL,
    ue_id BIGINT NOT NULL,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    
    PRIMARY KEY (enseignant_id, ue_id),
    
    -- Foreign key constraints
    CONSTRAINT fk_enseignant_ue_enseignant 
        FOREIGN KEY (enseignant_id) 
        REFERENCES enseignants(id_user) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_enseignant_ue_ue 
        FOREIGN KEY (ue_id) 
        REFERENCES ues(id) 
        ON DELETE CASCADE,
        
    -- Indexes
    INDEX idx_enseignant_id (enseignant_id),
    INDEX idx_ue_id (ue_id)
);

-- =====================================================
-- Add UE relationship to emplois_du_temps table
-- =====================================================
-- Check if column exists before adding it
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'plannoradb' 
                   AND TABLE_NAME = 'emplois_du_temps' 
                   AND COLUMN_NAME = 'ue_id');

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE emplois_du_temps ADD COLUMN ue_id BIGINT', 
    'SELECT "Column ue_id already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraint if it doesn't exist
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
                  WHERE TABLE_SCHEMA = 'plannoradb' 
                  AND TABLE_NAME = 'emplois_du_temps' 
                  AND CONSTRAINT_NAME = 'fk_emploi_ue');

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE emplois_du_temps ADD CONSTRAINT fk_emploi_ue FOREIGN KEY (ue_id) REFERENCES ues(id) ON DELETE SET NULL', 
    'SELECT "Foreign key fk_emploi_ue already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index if it doesn't exist
SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                   WHERE TABLE_SCHEMA = 'plannoradb' 
                   AND TABLE_NAME = 'emplois_du_temps' 
                   AND INDEX_NAME = 'idx_ue_id');

SET @sql = IF(@idx_exists = 0, 
    'ALTER TABLE emplois_du_temps ADD INDEX idx_ue_id (ue_id)', 
    'SELECT "Index idx_ue_id already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- Insert sample UE data
-- =====================================================
INSERT IGNORE INTO ues (
    code_ue, nom_ue, description, heures_cm, heures_td, heures_tp, 
    credits_ects, semestre, niveau, created_at, updated_at
) VALUES 
-- L1 - S1
('MATH101', 'Mathématiques Fondamentales', 'Introduction aux mathématiques de base pour l\'informatique', 24, 24, 0, 6, 'S1', 'L1', NOW(), NOW()),
('INFO101', 'Introduction à l\'Informatique', 'Concepts de base de l\'informatique et de la programmation', 18, 12, 24, 6, 'S1', 'L1', NOW(), NOW()),
('ALGO101', 'Algorithmique', 'Bases de l\'algorithmique et structures de données', 20, 20, 16, 6, 'S1', 'L1', NOW(), NOW()),

-- L1 - S2
('PROG102', 'Programmation Orientée Objet', 'Concepts de la POO avec Java', 18, 18, 24, 6, 'S2', 'L1', NOW(), NOW()),
('BD102', 'Bases de Données', 'Introduction aux bases de données relationnelles', 20, 16, 20, 6, 'S2', 'L1', NOW(), NOW()),
('MATH102', 'Mathématiques Discrètes', 'Logique, ensembles, relations et fonctions', 24, 20, 0, 5, 'S2', 'L1', NOW(), NOW()),

-- L2 - S3
('STRUCT201', 'Structures de Données Avancées', 'Arbres, graphes, tables de hachage', 20, 16, 20, 6, 'S3', 'L2', NOW(), NOW()),
('RESEAU201', 'Réseaux Informatiques', 'Protocoles réseau et architecture TCP/IP', 18, 12, 24, 6, 'S3', 'L2', NOW(), NOW()),
('SYS201', 'Systèmes d\'Exploitation', 'Concepts des systèmes d\'exploitation Unix/Linux', 20, 16, 20, 6, 'S3', 'L2', NOW(), NOW()),

-- L2 - S4
('WEB201', 'Développement Web', 'HTML, CSS, JavaScript et frameworks modernes', 16, 16, 32, 6, 'S4', 'L2', NOW(), NOW()),
('MOBILE201', 'Développement Mobile', 'Applications mobiles Android et iOS', 12, 12, 36, 6, 'S4', 'L2', NOW(), NOW()),
('PROJET201', 'Projet de Développement', 'Projet en équipe de développement logiciel', 8, 8, 40, 8, 'S4', 'L2', NOW(), NOW()),

-- L3 - S5
('IA301', 'Intelligence Artificielle', 'Algorithmes d\'IA et apprentissage automatique', 24, 20, 16, 6, 'S5', 'L3', NOW(), NOW()),
('SECU301', 'Sécurité Informatique', 'Cryptographie et sécurité des systèmes', 20, 16, 20, 6, 'S5', 'L3', NOW(), NOW()),
('GENIE301', 'Génie Logiciel', 'Méthodes de développement et gestion de projets', 20, 20, 16, 6, 'S5', 'L3', NOW(), NOW()),

-- L3 - S6
('CLOUD301', 'Cloud Computing', 'Services cloud et architectures distribuées', 16, 16, 24, 6, 'S6', 'L3', NOW(), NOW()),
('STAGE301', 'Stage en Entreprise', 'Stage de fin d\'études en entreprise', 0, 0, 0, 12, 'S6', 'L3', NOW(), NOW()),

-- M1 - S7
('RECH401', 'Méthodes de Recherche', 'Méthodologie de la recherche en informatique', 24, 16, 8, 6, 'S7', 'M1', NOW(), NOW()),
('ALGO401', 'Algorithmique Avancée', 'Algorithmes complexes et optimisation', 20, 20, 16, 6, 'S7', 'M1', NOW(), NOW()),
('ARCH401', 'Architecture Logicielle', 'Patterns et architectures d\'entreprise', 18, 18, 20, 6, 'S7', 'M1', NOW(), NOW()),

-- M1 - S8
('BIG401', 'Big Data et Analytics', 'Traitement de données massives', 16, 16, 28, 6, 'S8', 'M1', NOW(), NOW()),
('DEVOPS401', 'DevOps et Intégration Continue', 'Pratiques DevOps et automatisation', 12, 12, 32, 6, 'S8', 'M1', NOW(), NOW()),

-- M2 - S9
('EXPERT501', 'Expertise Technique', 'Spécialisation dans un domaine technique', 20, 20, 20, 8, 'S9', 'M2', NOW(), NOW()),
('MANAGE501', 'Management de Projets IT', 'Gestion de projets informatiques complexes', 24, 16, 8, 6, 'S9', 'M2', NOW(), NOW()),

-- M2 - S10
('MEMOIRE501', 'Mémoire de Recherche', 'Travail de recherche et rédaction du mémoire', 0, 0, 0, 15, 'S10', 'M2', NOW(), NOW()),
('SOUTENANCE501', 'Soutenance et Projet Final', 'Présentation du projet de fin d\'études', 0, 0, 0, 15, 'S10', 'M2', NOW(), NOW());

-- =====================================================
-- Assign sample UEs to existing enseignants
-- =====================================================
-- Get the first enseignant ID for demo assignments
SET @enseignant1 = (SELECT id_user FROM enseignants LIMIT 1);
SET @enseignant2 = (SELECT id_user FROM enseignants LIMIT 1 OFFSET 1);
SET @enseignant3 = (SELECT id_user FROM enseignants LIMIT 1 OFFSET 2);

-- Assign UEs to enseignants (if enseignants exist)
INSERT IGNORE INTO enseignant_ue (enseignant_id, ue_id)
SELECT @enseignant1, id FROM ues WHERE code_ue IN ('MATH101', 'MATH102', 'ALGO101', 'STRUCT201', 'IA301')
AND @enseignant1 IS NOT NULL;

INSERT IGNORE INTO enseignant_ue (enseignant_id, ue_id)
SELECT @enseignant2, id FROM ues WHERE code_ue IN ('INFO101', 'PROG102', 'WEB201', 'MOBILE201', 'CLOUD301')
AND @enseignant2 IS NOT NULL;

INSERT IGNORE INTO enseignant_ue (enseignant_id, ue_id)
SELECT @enseignant3, id FROM ues WHERE code_ue IN ('BD102', 'RESEAU201', 'SYS201', 'SECU301', 'DEVOPS401')
AND @enseignant3 IS NOT NULL;

-- =====================================================
-- Update existing emplois_du_temps with UE references
-- =====================================================
-- Link existing emplois_du_temps to UEs based on the 'ue' field
UPDATE emplois_du_temps edt 
JOIN ues u ON u.nom_ue = edt.ue 
SET edt.ue_id = u.id 
WHERE edt.ue_id IS NULL;

-- If no exact match, try to match by similar names
UPDATE emplois_du_temps edt 
JOIN ues u ON LOWER(u.nom_ue) LIKE CONCAT('%', LOWER(SUBSTRING(edt.ue, 1, 10)), '%')
SET edt.ue_id = u.id 
WHERE edt.ue_id IS NULL;

-- =====================================================
-- Verification queries
-- =====================================================
SELECT 'UEs created:' as info, COUNT(*) as count FROM ues;
SELECT 'Enseignant-UE assignments:' as info, COUNT(*) as count FROM enseignant_ue;
SELECT 'Emplois du temps with UE links:' as info, COUNT(*) as count FROM emplois_du_temps WHERE ue_id IS NOT NULL;

-- Show sample data
SELECT 'Sample UEs by level:' as info;
SELECT niveau, COUNT(*) as count, SUM(credits_ects) as total_credits 
FROM ues 
WHERE active = TRUE 
GROUP BY niveau 
ORDER BY niveau;

SELECT 'Sample UE assignments:' as info;
SELECT u.code_ue, u.nom_ue, COUNT(eu.enseignant_id) as nb_enseignants
FROM ues u
LEFT JOIN enseignant_ue eu ON u.id = eu.ue_id
GROUP BY u.id, u.code_ue, u.nom_ue
LIMIT 10;

-- =====================================================
-- Schema creation completed successfully
-- =====================================================
SELECT 'UE Management Schema created successfully!' as status, NOW() as timestamp;