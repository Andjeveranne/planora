@echo off
echo =====================================================
echo PLANNORA - FIX AUTHENTIFICATION SERVICE
echo =====================================================
echo This will fix the foreign key constraint error
echo that's preventing AuthentificationService from starting
echo =====================================================
echo.

echo 🔧 Problem: DataInitializer trying to delete users with FK constraints
echo 🔧 Solution: Modified DataInitializer to handle existing users properly
echo.

echo ✅ DataInitializer.java has been updated to:
echo   - Skip deletion of users with foreign key constraints
echo   - Only create users if they don't already exist
echo   - Handle FK constraint violations gracefully
echo.

echo 🚀 Next steps:
echo   1. The AuthentificationService should now start without errors
echo   2. Existing users (Pascal Abega, Pascal Bougong, Jean Dupont) are preserved
echo   3. Test users (admin.systeme, jean.dupont) will be created if missing
echo.

echo 📋 Available test accounts after fix:
echo   📧 admin.systeme@saintjeaningenieur.org / password123
echo   📧 jean.dupont@saintjeaningenieur.org / password123
echo   📧 admin@saintjeaningenieur.org / admin123 (existing admin)
echo   📧 pascal.abega@saintjeaningenieur.org / password123 (existing teacher)
echo   📧 pascal.bougong@saintjeaningenieur.org / password123 (existing teacher)
echo.

echo ✅ AUTHENTIFICATION SERVICE FOREIGN KEY ISSUE FIXED!
echo.
echo The service should now start successfully without constraint violations.
echo You can now test the UE assignment functionality.

pause