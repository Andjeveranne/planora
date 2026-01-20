-- =====================================================
-- DIAGNOSE UE ASSIGNMENT ISSUES
-- =====================================================
-- This script checks the current state of UE assignment
-- tables and relationships
-- =====================================================

USE PlannoraDB;

-- =====================================================
-- CHECK TABLE EXISTENCE
-- =====================================================

SELECT 'TABLE EXISTENCE CHECK:' as Info;

SELECT 
    TABLE_NAME,
    TABLE_TYPE,
    ENGINE
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PlannoraDB' 
AND TABLE_NAME IN ('utilisateurs', 'enseignants', 'ues', 'enseignant_ue')
ORDER BY TABLE_NAME;

-- =====================================================
-- CHECK DATA COUNTS
-- =====================================================

SELECT 'DATA COUNTS:' as Info;

SELECT 'utilisateurs (ENSEIGNANT)' as Table_Name, COUNT(*) as Count 
FROM utilisateurs WHERE role = 'ENSEIGNANT'
UNION ALL
SELECT 'enseignants', COUNT(*) FROM enseignants
UNION ALL
SELECT 'ues (active)', COUNT(*) FROM ues WHERE active = true
UNION ALL
SELECT 'enseignant_ue (assignments)', COALESCE((SELECT COUNT(*) FROM enseignant_ue), 0);

-- =====================================================
-- CHECK FOREIGN KEY CONSTRAINTS
-- =====================================================

SELECT 'FOREIGN KEY CONSTRAINTS:' as Info;

SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'PlannoraDB'
AND TABLE_NAME = 'enseignant_ue'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- =====================================================
-- CHECK SPECIFIC TEACHER DATA
-- =====================================================

SELECT 'PASCAL ABEGA DATA CHECK:' as Info;

-- Check if Pascal exists in utilisateurs
SELECT 'Pascal in utilisateurs:' as Check_Type, 
       CASE WHEN COUNT(*) > 0 THEN 'EXISTS' ELSE 'MISSING' END as Status
FROM utilisateurs 
WHERE id_user = '8924dc7b-8a43-402c-9756-066925fd6746';

-- Check if Pascal exists in enseignants
SELECT 'Pascal in enseignants:' as Check_Type,
       CASE WHEN COUNT(*) > 0 THEN 'EXISTS' ELSE 'MISSING' END as Status
FROM enseignants 
WHERE id_user = '8924dc7b-8a43-402c-9756-066925fd6746';

-- =====================================================
-- CHECK UE ASSIGNMENT READINESS
-- =====================================================

SELECT 'UE ASSIGNMENT READINESS:' as Info;

-- Check if join table exists
SELECT 'enseignant_ue table:' as Component,
       CASE WHEN COUNT(*) > 0 THEN 'EXISTS' ELSE 'MISSING' END as Status
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PlannoraDB' AND TABLE_NAME = 'enseignant_ue';

-- Check if UEs exist
SELECT 'Available UEs:' as Component,
       CONCAT(COUNT(*), ' UEs available') as Status
FROM ues WHERE active = true;

-- Check if teachers exist
SELECT 'Available Teachers:' as Component,
       CONCAT(COUNT(*), ' teachers available') as Status
FROM enseignants;

-- =====================================================
-- RECOMMENDATIONS
-- =====================================================

SELECT 'RECOMMENDATIONS:' as Info;

SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PlannoraDB' AND TABLE_NAME = 'enseignant_ue') = 0 
        THEN 'RUN: fix-ue-assignment-tables.sql to create join table'
        WHEN (SELECT COUNT(*) FROM enseignants) = 0 
        THEN 'RUN: fix-enseignant-entities.sql to create teacher entities'
        WHEN (SELECT COUNT(*) FROM ues WHERE active = true) = 0 
        THEN 'RUN: setup-complete-test-data.sql to create UEs'
        ELSE 'All tables exist - restart UserService and try assignment'
    END as Next_Action;