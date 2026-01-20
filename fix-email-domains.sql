-- =====================================================
-- FIX EMAIL DOMAINS TO INSTITUTIONAL FORMAT
-- =====================================================
-- This script updates existing email addresses to comply
-- with the institutional domain policy
-- =====================================================

USE PlannoraDB;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- BACKUP CURRENT EMAILS (for safety)
-- =====================================================

-- Create backup table if it doesn't exist
CREATE TABLE IF NOT EXISTS email_backup_20260120 AS
SELECT id_user, email, nom_user, prenom_user, role, NOW() as backup_date
FROM utilisateurs;

SELECT 'Email backup created in email_backup_20260120 table' as Info;

-- =====================================================
-- UPDATE INVALID EMAIL DOMAINS
-- =====================================================

SELECT 'UPDATING EMAIL DOMAINS TO INSTITUTIONAL FORMAT:' as Info;

-- Update emails that don't have the correct domain
UPDATE utilisateurs 
SET email = CONCAT(LOWER(prenom_user), '.', LOWER(nom_user), '@saintjeaningenieur.org')
WHERE email NOT LIKE '%@saintjeaningenieur.org'
AND role IN ('ENSEIGNANT', 'ETUDIANT');

-- Update admin emails with proper format
UPDATE utilisateurs 
SET email = CONCAT('admin.', LOWER(nom_user), '@saintjeaningenieur.org')
WHERE email NOT LIKE '%@saintjeaningenieur.org'
AND role IN ('ADMIN', 'ADMINISTRATEUR');

-- =====================================================
-- HANDLE SPECIAL CASES
-- =====================================================

-- Fix any emails that might have special characters or spaces
UPDATE utilisateurs 
SET email = REPLACE(REPLACE(REPLACE(email, ' ', ''), '-', ''), '_', '.')
WHERE email LIKE '%@saintjeaningenieur.org'
AND (email LIKE '% %' OR email LIKE '%-%' OR email LIKE '%_%');

-- Ensure all emails are lowercase
UPDATE utilisateurs 
SET email = LOWER(email)
WHERE email LIKE '%@saintjeaningenieur.org';

-- =====================================================
-- HANDLE DUPLICATE EMAILS
-- =====================================================

-- Find and fix potential duplicates by adding numbers
SET @counter = 0;

UPDATE utilisateurs u1
JOIN (
    SELECT email, COUNT(*) as cnt
    FROM utilisateurs 
    WHERE email LIKE '%@saintjeaningenieur.org'
    GROUP BY email 
    HAVING cnt > 1
) u2 ON u1.email = u2.email
SET u1.email = CONCAT(
    SUBSTRING_INDEX(u1.email, '@', 1), 
    '.', 
    (@counter := @counter + 1),
    '@saintjeaningenieur.org'
)
WHERE u1.id_user NOT IN (
    SELECT MIN(id_user) 
    FROM (SELECT * FROM utilisateurs) temp 
    WHERE temp.email = u1.email
);

-- =====================================================
-- VERIFY UPDATES
-- =====================================================

SELECT 'EMAIL UPDATE RESULTS:' as Info;

SELECT 
    'Updated Emails' as Category,
    COUNT(*) as Count
FROM utilisateurs 
WHERE email LIKE '%@saintjeaningenieur.org';

SELECT 
    'Remaining Invalid Emails' as Category,
    COUNT(*) as Count
FROM utilisateurs 
WHERE email NOT LIKE '%@saintjeaningenieur.org';

-- Show all current emails
SELECT 
    'CURRENT EMAIL ADDRESSES:' as Info;

SELECT 
    id_user,
    nom_user,
    prenom_user,
    email,
    role,
    '✅ VALID' as Status
FROM utilisateurs
WHERE email LIKE '%@saintjeaningenieur.org'
ORDER BY email;

-- Show any remaining invalid emails
SELECT 
    'REMAINING INVALID EMAILS:' as Info;

SELECT 
    id_user,
    nom_user,
    prenom_user,
    email,
    role,
    '❌ NEEDS MANUAL FIX' as Status
FROM utilisateurs
WHERE email NOT LIKE '%@saintjeaningenieur.org';

-- =====================================================
-- UPDATE AUTHENTICATION SERVICE USERS (if applicable)
-- =====================================================

-- Note: If using separate authentication database, you might need to run similar updates there
-- This depends on your authentication service setup

-- =====================================================
-- CLEAN UP ENSEIGNANT TABLE REFERENCES
-- =====================================================

-- Update any references in enseignants table if needed
-- (This assumes enseignants table might have email references)

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
SELECT '✅ EMAIL DOMAIN FIX COMPLETE!' as Status;
SELECT 'All emails should now use @saintjeaningenieur.org domain' as Message;
SELECT 'Backup created in email_backup_20260120 table' as Backup_Info;
SELECT 'Restart authentication service to apply domain restrictions' as NextStep;