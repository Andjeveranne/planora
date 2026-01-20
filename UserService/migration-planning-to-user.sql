-- =====================================================
-- Migration Script: PlannoraplanningDB to PlannoraDB
-- =====================================================
-- This script migrates data from plannoraplanningdb to plannoradb
-- and creates the necessary table structure for emplois_du_temps

USE plannoradb;

-- Create emplois_du_temps table in plannoradb if it doesn't exist
CREATE TABLE IF NOT EXISTS emplois_du_temps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_jour DATE NOT NULL,
    jour VARCHAR(255) NOT NULL,
    heure_debut INT NOT NULL,
    heure_fin INT NOT NULL,
    ue VARCHAR(255) NOT NULL,
    classe VARCHAR(255) NOT NULL,
    salle VARCHAR(255) NOT NULL,
    type_cours VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    enseignant_id VARCHAR(255) NOT NULL,
    
    -- Indexes for performance
    INDEX idx_enseignant_id (enseignant_id),
    INDEX idx_date_jour (date_jour),
    INDEX idx_salle (salle),
    INDEX idx_classe (classe),
    INDEX idx_ue (ue),
    
    -- Foreign key constraint (referencing enseignants.id_user)
    CONSTRAINT fk_emploi_enseignant 
        FOREIGN KEY (enseignant_id) 
        REFERENCES enseignants(id_user) 
        ON DELETE CASCADE
);

-- =====================================================
-- Step 1: Migrate emplois_du_temps data
-- =====================================================

-- Insert emplois_du_temps from plannoraplanningdb to plannoradb
INSERT INTO plannoradb.emplois_du_temps (
    date_jour, 
    jour, 
    heure_debut, 
    heure_fin, 
    ue, 
    classe, 
    salle, 
    type_cours, 
    created_at, 
    updated_at, 
    enseignant_id
)
SELECT 
    date_jour,
    jour,
    heure_debut,
    heure_fin,
    ue,
    classe,
    salle,
    type_cours,
    COALESCE(created_at, NOW()),
    COALESCE(updated_at, NOW()),
    enseignant_id
FROM plannoraplanningdb.emplois_du_temps
WHERE enseignant_id IS NOT NULL
ON DUPLICATE KEY UPDATE
    date_jour = VALUES(date_jour),
    jour = VALUES(jour),
    heure_debut = VALUES(heure_debut),
    heure_fin = VALUES(heure_fin),
    ue = VALUES(ue),
    classe = VALUES(classe),
    salle = VALUES(salle),
    type_cours = VALUES(type_cours),
    updated_at = NOW();

-- =====================================================
-- Step 2: Update disponibilites table structure if needed
-- =====================================================

-- Check if we need to update the disponibilites table structure
-- The plannoradb.disponibilites uses DayOfWeek enum and LocalTime
-- The plannoraplanningdb.disponibilites uses DateTime

-- First, let's create a temporary mapping table for disponibilites migration
CREATE TEMPORARY TABLE temp_disponibilites_mapping AS
SELECT 
    id,
    enseignant_id,
    DATE(date_heure_debut) as date_jour,
    DAYNAME(date_heure_debut) as jour_semaine_text,
    TIME(date_heure_debut) as heure_debut,
    TIME(date_heure_fin) as heure_fin,
    commentaire,
    CASE DAYOFWEEK(date_heure_debut)
        WHEN 1 THEN 'SUNDAY'
        WHEN 2 THEN 'MONDAY' 
        WHEN 3 THEN 'TUESDAY'
        WHEN 4 THEN 'WEDNESDAY'
        WHEN 5 THEN 'THURSDAY'
        WHEN 6 THEN 'FRIDAY'
        WHEN 7 THEN 'SATURDAY'
    END as jour_semaine_enum
FROM plannoraplanningdb.disponibilites
WHERE enseignant_id IS NOT NULL;

-- =====================================================
-- Step 3: Migrate disponibilites data (if any exists)
-- =====================================================

-- Note: This migration assumes that the plannoradb.disponibilites table
-- uses the UserService structure (DayOfWeek enum, LocalTime)
-- If there are disponibilites in plannoraplanningdb, we need to convert them

-- Insert disponibilites from plannoraplanningdb to plannoradb
-- Only if the enseignant exists in the plannoradb.enseignants table
INSERT IGNORE INTO plannoradb.disponibilites (
    jour_semaine,
    heure_debut,
    heure_fin,
    enseignant_id
)
SELECT DISTINCT
    tm.jour_semaine_enum,
    tm.heure_debut,
    tm.heure_fin,
    tm.enseignant_id
FROM temp_disponibilites_mapping tm
WHERE EXISTS (
    SELECT 1 FROM plannoradb.enseignants e 
    WHERE e.id_user = tm.enseignant_id
)
AND NOT EXISTS (
    SELECT 1 FROM plannoradb.disponibilites d
    WHERE d.enseignant_id = tm.enseignant_id
    AND d.jour_semaine = tm.jour_semaine_enum
    AND d.heure_debut = tm.heure_debut
    AND d.heure_fin = tm.heure_fin
);

-- =====================================================
-- Step 4: Verification queries
-- =====================================================

-- Count records migrated
SELECT 'emplois_du_temps' as table_name, COUNT(*) as records_count 
FROM plannoradb.emplois_du_temps
UNION ALL
SELECT 'disponibilites' as table_name, COUNT(*) as records_count 
FROM plannoradb.disponibilites;

-- Show sample of migrated data
SELECT 'Sample emplois_du_temps:' as info;
SELECT * FROM plannoradb.emplois_du_temps LIMIT 5;

SELECT 'Sample disponibilites:' as info;
SELECT * FROM plannoradb.disponibilites LIMIT 5;

-- =====================================================
-- Step 5: Clean up temporary table
-- =====================================================
DROP TEMPORARY TABLE IF EXISTS temp_disponibilites_mapping;

-- =====================================================
-- Migration Summary
-- =====================================================
SELECT 
    'Migration completed successfully!' as status,
    (SELECT COUNT(*) FROM plannoradb.emplois_du_temps) as emplois_migrated,
    (SELECT COUNT(*) FROM plannoradb.disponibilites) as disponibilites_total,
    NOW() as migration_timestamp;