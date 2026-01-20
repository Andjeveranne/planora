@echo off
echo =====================================================
echo PLANNORA - FIX UE ASSIGNMENT TRANSACTION ERRORS
echo =====================================================
echo This will fix "Could not commit JPA transaction" errors
echo by creating missing database tables and relationships
echo =====================================================
echo.

echo 🔧 Creating missing join tables and fixing constraints...

mysql -u root -p2003Rty. < fix-ue-assignment-tables.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ Database tables fixed successfully!
    echo.
    echo 🎯 What was fixed:
    echo   • Created enseignant_ue join table
    echo   • Added proper foreign key constraints
    echo   • Created performance indexes
    echo   • Fixed Many-to-Many relationship structure
    echo.
    echo 📋 Next steps:
    echo   1. Restart UserService (REQUIRED)
    echo   2. Try UE assignment again
    echo   3. Should work without transaction errors
    echo.
    echo ⚠️  IMPORTANT: You MUST restart UserService for changes to take effect!
    echo.
) else (
    echo.
    echo ❌ Database fix failed!
    echo Check MySQL connection and password.
)

echo.
pause