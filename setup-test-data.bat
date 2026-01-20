@echo off
echo =====================================================
echo PLANNORA - SETUP COMPLETE TEST DATA
echo =====================================================
echo This will create UEs, Classes, and Course Assignments
echo for testing timetable generation functionality
echo =====================================================
echo.

echo Setting up complete test data...

mysql -u root -p2003Rty. < setup-complete-test-data.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ Test data setup successful!
    echo.
    echo 📋 What was created:
    echo   • 5 UEs (Teaching Units)
    echo   • 5 Classes
    echo   • 3 Teachers (including Pascal Bougong)
    echo   • 10 Course Assignments
    echo.
    echo 🎯 Next steps:
    echo   1. Pascal Bougong can now generate timetables
    echo   2. His availabilities will be used to create courses
    echo   3. Test the "Générer EDT" functionality
    echo.
) else (
    echo.
    echo ❌ Test data setup failed!
    echo Check MySQL connection and password.
)

echo.
pause