-- =====================================================
-- FIX UE ASSIGNMENT TABLES
-- =====================================================
-- This script creates the missing join table for
-- UE-Enseignant Many-to-Many relationship
-- =====================================================

USE PlannoraDB;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- CREATE MISSING JOIN TABLE
-- =====================================================

-- Create the enseignant_ue join table if it doesn't exist
CREATE TABLE IF NOT EXISTS enseignant_ue (
    enseignant_id VARCHAR(255) NOT NULL,
    ue_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (enseignant_id, ue_id),
    FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id_user) ON DELETE CASCADE,
    FOREIGN KEY (ue_id) REFERENCES ues(id) ON DELETE CASCADE
);

-- =====================================================
-- VERIFY TABLE STRUCTURE
-- =====================================================

-- Show the table structure
DESCRIBE enseignant_ue;

-- Show existing data (should be empty initially)
SELECT COUNT(*) as existing_assignments FROM enseignant_ue;

-- =====================================================
-- CREATE INDEXES FOR PERFORMANCE
-- =====================================================

-- Create indexes for better performance (MySQL doesn't support IF NOT EXISTS for indexes)
CREATE INDEX idx_enseignant_ue_enseignant ON enseignant_ue(enseignant_id);
CREATE INDEX idx_enseignant_ue_ue ON enseignant_ue(ue_id);

-- =====================================================
-- VERIFY FOREIGN KEY CONSTRAINTS
-- =====================================================

-- Check that referenced tables exist and have data
SELECT 'VERIFICATION:' as Info;

SELECT 'Utilisateurs (Teachers):' as Table_Name, COUNT(*) as Count 
FROM utilisateurs WHERE role = 'ENSEIGNANT'
UNION ALL
SELECT 'Enseignants:', COUNT(*) FROM enseignants
UNION ALL
SELECT 'UEs:', COUNT(*) FROM ues WHERE active = true
UNION ALL
SELECT 'Join Table:', COUNT(*) FROM enseignant_ue;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
SELECT '✅ UE ASSIGNMENT TABLES FIXED!' as Status;
SELECT 'enseignant_ue join table created successfully.' as Message;
SELECT 'UE-Teacher assignment should now work without transaction errors.' as NextStep;