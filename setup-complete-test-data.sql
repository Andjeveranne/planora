-- =====================================================
-- PLANNORA SYSTEM - COMPLETE TEST DATA SETUP
-- =====================================================
-- This script creates all necessary data for testing
-- timetable generation functionality
-- =====================================================

USE PlannoraDB;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. CREATE UE (TEACHING UNITS)
-- =====================================================

INSERT INTO ues (code_ue, nom_ue, credits_ects, semestre, niveau, description, heures_cm, heures_td, heures_tp, active, created_at, updated_at) VALUES
('INF101', 'Programmation Java', 3, 'S1', 'L1', 'Introduction à la programmation Java orientée objet', 20, 15, 10, true, NOW(), NOW()),
('MAT201', 'Algèbre Linéaire', 4, 'S2', 'L2', 'Mathématiques appliquées - Algèbre et géométrie', 25, 20, 0, true, NOW(), NOW()),
('PHY301', 'Mécanique Quantique', 5, 'S3', 'L3', 'Physique avancée - Principes quantiques', 30, 15, 15, true, NOW(), NOW()),
('INF202', 'Base de Données', 3, 'S2', 'L2', 'Conception et gestion de bases de données', 18, 12, 15, true, NOW(), NOW()),
('MAT102', 'Analyse Mathématique', 4, 'S1', 'L1', 'Calcul différentiel et intégral', 22, 18, 0, true, NOW(), NOW());

-- =====================================================
-- 2. CREATE CLASSES
-- =====================================================

INSERT INTO classes (nom_classe, niveau, capacite, specialisation, description, active, created_at, updated_at) VALUES
('L1 Informatique A', 'L1', 30, 'Informatique', 'Classe de première année informatique - Groupe A', true, NOW(), NOW()),
('L2 Mathématiques B', 'L2', 25, 'Mathématiques', 'Classe de deuxième année mathématiques - Groupe B', true, NOW(), NOW()),
('L3 Physique C', 'L3', 20, 'Physique', 'Classe de troisième année physique - Groupe C', true, NOW(), NOW()),
('L1 Sciences Générales', 'L1', 35, 'Sciences', 'Classe de première année sciences générales', true, NOW(), NOW()),
('L2 Informatique Avancée', 'L2', 22, 'Informatique', 'Classe de deuxième année informatique avancée', true, NOW(), NOW());

-- =====================================================
-- 3. CREATE TEACHERS (ENSEIGNANT ENTITIES)
-- =====================================================

-- We'll use the teacher ID from the logs: 8924dc7b-8a43-402c-9756-066925fd6746
SET @teacher_id = '8924dc7b-8a43-402c-9756-066925fd6746';

-- First, ensure the teacher exists in utilisateurs table
INSERT IGNORE INTO utilisateurs (id_user, email, mdp, nom_user, prenom_user, telephone, role, active, created_at, updated_at) 
VALUES (@teacher_id, 'pascal.abega@plannora.com', '$2a$10$example_hash', 'Abega', 'Pascal', '+221 77 123 45 67', 'ENSEIGNANT', true, NOW(), NOW());

-- Create corresponding Enseignant entity (inherits from Utilisateur)
INSERT IGNORE INTO enseignants (id_user, specialite, departement) 
VALUES (@teacher_id, 'Informatique', 'Sciences');

-- Create additional teachers
INSERT IGNORE INTO utilisateurs (id_user, email, mdp, nom_user, prenom_user, telephone, role, active, created_at, updated_at) VALUES
('teacher-marie-001', 'marie.martin@plannora.com', '$2a$10$example_hash', 'Martin', 'Marie', '+221 77 234 56 78', 'ENSEIGNANT', true, NOW(), NOW()),
('teacher-pierre-002', 'pierre.durand@plannora.com', '$2a$10$example_hash', 'Durand', 'Pierre', '+221 77 345 67 89', 'ENSEIGNANT', true, NOW(), NOW());

-- Create corresponding Enseignant entities
INSERT IGNORE INTO enseignants (id_user, specialite, departement) VALUES
('teacher-marie-001', 'Mathématiques', 'Sciences'),
('teacher-pierre-002', 'Physique', 'Sciences');

-- =====================================================
-- 4. CREATE COURSE ASSIGNMENTS
-- =====================================================

-- Get IDs for UEs and Classes
SET @ue_inf101 = (SELECT id FROM ues WHERE code_ue = 'INF101');
SET @ue_inf202 = (SELECT id FROM ues WHERE code_ue = 'INF202');
SET @ue_mat201 = (SELECT id FROM ues WHERE code_ue = 'MAT201');
SET @ue_mat102 = (SELECT id FROM ues WHERE code_ue = 'MAT102');

SET @classe_l1_info = (SELECT id FROM classes WHERE nom_classe = 'L1 Informatique A');
SET @classe_l2_math = (SELECT id FROM classes WHERE nom_classe = 'L2 Mathématiques B');
SET @classe_l1_sciences = (SELECT id FROM classes WHERE nom_classe = 'L1 Sciences Générales');
SET @classe_l2_info_adv = (SELECT id FROM classes WHERE nom_classe = 'L2 Informatique Avancée');

-- Create course assignments for Pascal Bougong
INSERT INTO courses (enseignant_id, ue_id, classe_id, type_cours, semestre, annee_academique, active, created_at, updated_at) VALUES
(@teacher_id, @ue_inf101, @classe_l1_info, 'CM', 'S1', '2024-2025', true, NOW(), NOW()),
(@teacher_id, @ue_inf101, @classe_l1_info, 'TD', 'S1', '2024-2025', true, NOW(), NOW()),
(@teacher_id, @ue_inf202, @classe_l2_info_adv, 'CM', 'S2', '2024-2025', true, NOW(), NOW()),
(@teacher_id, @ue_inf202, @classe_l2_info_adv, 'TP', 'S2', '2024-2025', true, NOW(), NOW()),
(@teacher_id, @ue_mat102, @classe_l1_sciences, 'TD', 'S1', '2024-2025', true, NOW(), NOW());

-- =====================================================
-- 5. CREATE ADDITIONAL TEACHERS (Optional)
-- =====================================================

-- Additional teachers are already created above

-- Create courses for Marie (Mathematics)
INSERT INTO courses (enseignant_id, ue_id, classe_id, type_cours, semestre, annee_academique, active, created_at, updated_at) VALUES
('teacher-marie-001', @ue_mat201, @classe_l2_math, 'CM', 'S2', '2024-2025', true, NOW(), NOW()),
('teacher-marie-001', @ue_mat201, @classe_l2_math, 'TD', 'S2', '2024-2025', true, NOW(), NOW()),
('teacher-marie-001', @ue_mat102, @classe_l1_sciences, 'CM', 'S1', '2024-2025', true, NOW(), NOW());

-- Create courses for Pierre (Physics)
INSERT INTO courses (enseignant_id, ue_id, classe_id, type_cours, semestre, annee_academique, active, created_at, updated_at) VALUES
('teacher-pierre-002', (SELECT id FROM ues WHERE code_ue = 'PHY301'), (SELECT id FROM classes WHERE nom_classe = 'L3 Physique C'), 'CM', 'S3', '2024-2025', true, NOW(), NOW()),
('teacher-pierre-002', (SELECT id FROM ues WHERE code_ue = 'PHY301'), (SELECT id FROM classes WHERE nom_classe = 'L3 Physique C'), 'TP', 'S3', '2024-2025', true, NOW(), NOW());

-- =====================================================
-- 6. VERIFICATION QUERIES
-- =====================================================

-- Check created data
SELECT 'CREATED DATA SUMMARY:' as Info;

SELECT 'UEs Created:' as Category, COUNT(*) as Count FROM ues WHERE active = true
UNION ALL
SELECT 'Classes Created:', COUNT(*) FROM classes WHERE active = true
UNION ALL
SELECT 'Teachers Created:', COUNT(*) FROM utilisateurs WHERE role = 'ENSEIGNANT'
UNION ALL
SELECT 'Course Assignments:', COUNT(*) FROM courses WHERE active = true;

-- Show Pascal's course assignments
SELECT 
    'PASCAL ABEGA COURSE ASSIGNMENTS:' as Info,
    u.nom_user as Teacher,
    ue.code_ue as UE_Code,
    ue.nom_ue as UE_Name,
    c.nom_classe as Class,
    co.type_cours as Course_Type
FROM courses co
JOIN utilisateurs u ON co.enseignant_id = u.id_user
JOIN ues ue ON co.ue_id = ue.id
JOIN classes c ON co.classe_id = c.id
WHERE u.id_user = @teacher_id;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
SELECT '✅ COMPLETE TEST DATA SETUP SUCCESSFUL!' as Status;
SELECT 'Pascal Abega now has 5 course assignments for timetable generation.' as Message;
SELECT 'You can now test the timetable generation functionality.' as NextStep;