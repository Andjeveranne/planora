@echo off
echo =====================================================
echo PLANNORA - FIX ALL CURRENT ISSUES
echo =====================================================
echo This will fix:
echo   • UE Statistics 500 error
echo   • UE Teacher Assignment 500 error  
echo   • Missing test data for Pascal Abega
echo =====================================================
echo.

set /p confirm="Continue with complete fix? Type YES to confirm: "
if not "%confirm%"=="YES" (
    echo Fix cancelled.
    pause
    exit /b
)

echo.
echo 🔧 Step 1: Setting up complete test data...
mysql -u root -p2003Rty. < setup-complete-test-data.sql

if %errorlevel% neq 0 (
    echo ❌ Step 1 failed! Check MySQL connection.
    pause
    exit /b 1
)

echo ✅ Step 1 complete: UEs, Classes, and Courses created

echo.
echo 🔧 Step 2: Fixing Enseignant entities...
mysql -u root -p2003Rty. < fix-enseignant-entities.sql

if %errorlevel% neq 0 (
    echo ❌ Step 2 failed! Check MySQL connection.
    pause
    exit /b 1
)

echo ✅ Step 2 complete: Enseignant entities created

echo.
echo 🎉 ALL FIXES APPLIED SUCCESSFULLY!
echo.
echo 📋 What was fixed:
echo   ✅ Created 5 UEs (fixes statistics 500 error)
echo   ✅ Created 5 Classes
echo   ✅ Created 5 Course assignments for Pascal Abega
echo   ✅ Created Enseignant entities (fixes UE assignment)
echo   ✅ Timetable generation should continue working
echo.
echo 🚀 Next steps:
echo   1. Restart UserService (recommended)
echo   2. Refresh admin dashboard
echo   3. Test UE assignment functionality
echo   4. Verify timetable generation still works
echo.
echo 🧪 Test checklist:
echo   □ UE Management loads without 500 error
echo   □ Can assign Pascal to a UE
echo   □ Timetable generation creates courses
echo   □ All time slots are filled properly
echo.

pause