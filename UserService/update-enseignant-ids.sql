-- Update script to map old enseignant IDs to new UUID format
-- This script updates the emplois_du_temps table to use the correct enseignant IDs

USE plannoradb;

-- Show current enseignants
SELECT 'Current enseignants in database:' as info;
SELECT e.id_user, u.nom_user, u.prenom_user 
FROM enseignants e 
JOIN utilisateurs u ON e.id_user = u.id_user;

-- Show current emplois_du_temps with old IDs
SELECT 'Current emplois_du_temps with old IDs:' as info;
SELECT DISTINCT enseignant_id FROM emplois_du_temps;

-- Create a mapping for the demo data
-- Assuming we want to assign all courses to the first enseignant for demo purposes
SET @first_enseignant_id = (SELECT id_user FROM enseignants LIMIT 1);

-- Update all emplois_du_temps to use the first enseignant's ID
UPDATE emplois_du_temps 
SET enseignant_id = @first_enseignant_id 
WHERE enseignant_id LIKE 'ens%';

-- Show results
SELECT 'Updated emplois_du_temps:' as info;
SELECT COUNT(*) as total_emplois, enseignant_id FROM emplois_du_temps GROUP BY enseignant_id;

-- Verify the relationship works
SELECT 'Verification - Emplois with enseignant details:' as info;
SELECT 
    e.id,
    e.ue,
    e.classe,
    e.salle,
    e.date_jour,
    CONCAT(u.prenom_user, ' ', u.nom_user) as enseignant_name
FROM emplois_du_temps e
JOIN enseignants ens ON e.enseignant_id = ens.id_user
JOIN utilisateurs u ON ens.id_user = u.id_user
LIMIT 5;