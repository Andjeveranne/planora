@echo off
echo =====================================================
echo PLANNORA - FIX ENSEIGNANT ENTITIES
echo =====================================================
echo This will create Enseignant entities for existing teachers
echo to fix UE assignment functionality
echo =====================================================
echo.

echo Fixing Enseignant entities...

mysql -u root -p2003Rty. < fix-enseignant-entities.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ Enseignant entities fixed successfully!
    echo.
    echo 🎯 What was fixed:
    echo   • Created Enseignant entities for existing teachers
    echo   • UE assignment should now work
    echo   • Teacher-UE relationships enabled
    echo.
    echo 📋 Next steps:
    echo   1. Restart UserService (recommended)
    echo   2. Try UE assignment in admin dashboard
    echo   3. Test timetable generation
    echo.
) else (
    echo.
    echo ❌ Fix failed!
    echo Check MySQL connection and password.
)

echo.
pause