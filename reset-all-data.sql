-- =====================================================
-- PLANNORA SYSTEM - COMPLETE DATA RESET SCRIPT
-- =====================================================
-- This script will delete ALL data from all databases
-- Use this for fresh testing of all functionalities
-- =====================================================

-- Switch to PlannoraDB (UserService database)
USE PlannoraDB;

-- Disable foreign key checks to avoid constraint issues
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- USERSERVICE DATABASE CLEANUP
-- =====================================================

-- Delete all timetable data
DELETE FROM emplois_du_temps;
ALTER TABLE emplois_du_temps AUTO_INCREMENT = 1;

-- Delete all availability data
DELETE FROM disponibilites;
ALTER TABLE disponibilites AUTO_INCREMENT = 1;

-- Delete all course assignments
DELETE FROM courses;
ALTER TABLE courses AUTO_INCREMENT = 1;

-- Delete all classes
DELETE FROM classes;
ALTER TABLE classes AUTO_INCREMENT = 1;

-- Delete all UE (Teaching Units)
DELETE FROM ues;
ALTER TABLE ues AUTO_INCREMENT = 1;

-- Delete all teachers (keep admin users)
DELETE FROM utilisateurs WHERE role = 'ENSEIGNANT';

-- Reset admin password (optional - uncomment if needed)
-- UPDATE utilisateurs SET mdp = '$2a$10$example_hash' WHERE role = 'ADMIN';

-- =====================================================
-- PLANNING SERVICE DATABASE CLEANUP
-- =====================================================

-- Switch to PlannoraPlanningDB (PlanningService database)
USE PlannoraPlanningDB;

-- Delete all planning data
DELETE FROM disponibilites;
ALTER TABLE disponibilites AUTO_INCREMENT = 1;

DELETE FROM emplois_du_temps;
ALTER TABLE emplois_du_temps AUTO_INCREMENT = 1;

-- =====================================================
-- AUTHENTICATION DATABASE CLEANUP (if separate)
-- =====================================================

-- Switch back to main database
USE PlannoraDB;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify cleanup
SELECT 'UserService Data Counts:' as Info;
SELECT 'emplois_du_temps' as table_name, COUNT(*) as count FROM emplois_du_temps
UNION ALL
SELECT 'disponibilites', COUNT(*) FROM disponibilites
UNION ALL
SELECT 'courses', COUNT(*) FROM courses
UNION ALL
SELECT 'classes', COUNT(*) FROM classes
UNION ALL
SELECT 'ues', COUNT(*) FROM ues
UNION ALL
SELECT 'enseignants', COUNT(*) FROM utilisateurs WHERE role = 'ENSEIGNANT'
UNION ALL
SELECT 'admins', COUNT(*) FROM utilisateurs WHERE role = 'ADMIN';

-- Switch to planning database for verification
USE PlannoraPlanningDB;
SELECT 'PlanningService Data Counts:' as Info;
SELECT 'planning_disponibilites' as table_name, COUNT(*) as count FROM disponibilites
UNION ALL
SELECT 'planning_emplois_du_temps', COUNT(*) FROM emplois_du_temps;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
SELECT '✅ ALL SYSTEM DATA HAS BEEN RESET SUCCESSFULLY!' as Status;
SELECT 'You can now test all functionalities from scratch.' as Message;