-- =====================================================
-- VALIDATE EXISTING EMAIL ADDRESSES
-- =====================================================
-- This script checks and reports on existing email addresses
-- to ensure they comply with the institutional domain policy
-- =====================================================

USE PlannoraDB;

-- =====================================================
-- CHECK CURRENT EMAIL ADDRESSES
-- =====================================================

SELECT 'CURRENT EMAIL ADDRESSES IN SYSTEM:' as Info;

-- Check all users in utilisateurs table
SELECT 
    'UTILISATEURS TABLE:' as Table_Name,
    id_user,
    nom_user,
    prenom_user,
    email,
    role,
    CASE 
        WHEN email LIKE '%@saintjeaningenieur.org' THEN '✅ VALID'
        ELSE '❌ INVALID DOMAIN'
    END as Domain_Status
FROM utilisateurs
ORDER BY email;

-- Check authentication service users if they exist
SELECT 'AUTHENTICATION SERVICE USERS:' as Info;

-- Note: This might not exist if using separate authentication database
-- SELECT * FROM auth_users WHERE email NOT LIKE '%@saintjeaningenieur.org';

-- =====================================================
-- SUMMARY STATISTICS
-- =====================================================

SELECT 'EMAIL DOMAIN STATISTICS:' as Info;

SELECT 
    'Valid Institutional Emails' as Category,
    COUNT(*) as Count
FROM utilisateurs 
WHERE email LIKE '%@saintjeaningenieur.org'

UNION ALL

SELECT 
    'Invalid Domain Emails' as Category,
    COUNT(*) as Count
FROM utilisateurs 
WHERE email NOT LIKE '%@saintjeaningenieur.org'

UNION ALL

SELECT 
    'Total Users' as Category,
    COUNT(*) as Count
FROM utilisateurs;

-- =====================================================
-- IDENTIFY PROBLEMATIC EMAILS
-- =====================================================

SELECT 'EMAILS THAT NEED ATTENTION:' as Info;

SELECT 
    id_user,
    nom_user,
    prenom_user,
    email as Current_Email,
    CONCAT(LOWER(prenom_user), '.', LOWER(nom_user), '@saintjeaningenieur.org') as Suggested_Email,
    role
FROM utilisateurs 
WHERE email NOT LIKE '%@saintjeaningenieur.org'
ORDER BY nom_user, prenom_user;

-- =====================================================
-- CHECK FOR DUPLICATE SUGGESTED EMAILS
-- =====================================================

SELECT 'CHECKING FOR POTENTIAL CONFLICTS:' as Info;

SELECT 
    CONCAT(LOWER(prenom_user), '.', LOWER(nom_user), '@saintjeaningenieur.org') as Suggested_Email,
    COUNT(*) as Occurrences,
    GROUP_CONCAT(CONCAT(prenom_user, ' ', nom_user) SEPARATOR ', ') as Users
FROM utilisateurs
GROUP BY CONCAT(LOWER(prenom_user), '.', LOWER(nom_user), '@saintjeaningenieur.org')
HAVING COUNT(*) > 1;

-- =====================================================
-- VALIDATION RULES CHECK
-- =====================================================

SELECT 'EMAIL FORMAT VALIDATION:' as Info;

SELECT 
    email,
    nom_user,
    prenom_user,
    CASE 
        WHEN email LIKE '%@saintjeaningenieur.org' THEN
            CASE 
                WHEN SUBSTRING_INDEX(email, '@', 1) REGEXP '^[a-z]+\\.[a-z]+$' THEN '✅ VALID FORMAT'
                WHEN SUBSTRING_INDEX(email, '@', 1) LIKE 'admin.%' THEN '✅ ADMIN FORMAT'
                WHEN SUBSTRING_INDEX(email, '@', 1) LIKE 'system.%' THEN '✅ SYSTEM FORMAT'
                ELSE '⚠️ INVALID FORMAT'
            END
        ELSE '❌ WRONG DOMAIN'
    END as Format_Status
FROM utilisateurs
ORDER BY 
    CASE 
        WHEN email LIKE '%@saintjeaningenieur.org' THEN 1 
        ELSE 2 
    END,
    email;

-- =====================================================
-- RECOMMENDATIONS
-- =====================================================

SELECT 'RECOMMENDATIONS:' as Info;

SELECT 
    '1. Update invalid domain emails to @saintjeaningenieur.org' as Recommendation
UNION ALL
SELECT 
    '2. Ensure email format follows: prenom.nom@saintjeaningenieur.org'
UNION ALL
SELECT 
    '3. Admin accounts can use: admin.name@saintjeaningenieur.org'
UNION ALL
SELECT 
    '4. System accounts can use: system.name@saintjeaningenieur.org'
UNION ALL
SELECT 
    '5. Test login after email updates to ensure authentication works';

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================
SELECT '📧 EMAIL VALIDATION COMPLETE!' as Status;
SELECT 'Review the results above and update emails as needed.' as NextStep;
SELECT 'After updates, restart authentication service to apply domain restrictions.' as Important;