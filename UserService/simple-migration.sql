-- Simple Migration Script: PlannoraplanningDB to PlannoraDB
-- This script migrates data without foreign key constraints due to ID mismatch

USE plannoradb;

-- Create emplois_du_temps table without foreign key constraint
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
    
    INDEX idx_enseignant_id (enseignant_id),
    INDEX idx_date_jour (date_jour),
    INDEX idx_salle (salle),
    INDEX idx_classe (classe),
    INDEX idx_ue (ue)
);

-- Migrate emplois_du_temps data
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
FROM plannoraplanningdb.emplois_du_temps;

-- Show migration results
SELECT 'Migration completed!' as status;
SELECT COUNT(*) as emplois_migrated FROM plannoradb.emplois_du_temps;
SELECT * FROM plannoradb.emplois_du_temps LIMIT 3;