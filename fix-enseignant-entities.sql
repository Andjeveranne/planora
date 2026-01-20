-- =====================================================
-- FIX ENSEIGNANT ENTITIES
-- =====================================================
-- This script creates Enseignant entities for existing
-- Utilisateur entries with role='ENSEIGNANT'
-- =====================================================

USE PlannoraDB;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- CREATE ENSEIGNANT ENTITIES FOR EXISTING TEACHERS
-- =====================================================

-- Create enseignants table if it doesn't exist (it should inherit from utilisateurs)
-- This is handled by JPA, but we need to ensure the data is there

-- Insert Enseignant entries for all existing teachers
INSERT IGNORE INTO enseignants (id_user, specialite, departement)
SELECT 
    id_user,
    'Non défini' as specialite,
    'Non défini' as departement
FROM utilisateurs 
WHERE role = 'ENSEIGNANT';

-- =====================================================
-- VERIFY ENSEIGNANT ENTITIES
-- =====================================================

SELECT 'ENSEIGNANT ENTITIES CREATED:' as Info;

SELECT 
    u.id_user,
    u.nom_user,
    u.prenom_user,
    u.email,
    e.specialite,
    e.departement
FROM utilisateurs u
JOIN enseignants e ON u.id_user = e.id_user
WHERE u.role = 'ENSEIGNANT';

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
SELECT '✅ ENSEIGNANT ENTITIES FIXED!' as Status;
SELECT 'UE assignment should now work properly.' as Message;