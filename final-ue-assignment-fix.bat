@echo off
echo =====================================================
echo PLANNORA - FINAL UE ASSIGNMENT FIX
echo =====================================================
echo This will completely fix the UE assignment 500 errors
echo by running all necessary database and code fixes
echo =====================================================
echo.

set /p confirm="Apply complete UE assignment fix? Type YES to confirm: "
if not "%confirm%"=="YES" (
    echo Fix cancelled.
    pause
    exit /b
)

echo.
echo 🔧 Step 1: Running diagnostic to check current state...
mysql -u root -p2003Rty. < diagnose-ue-assignment.sql > diagnosis-results.txt 2>&1

echo.
echo 🔧 Step 2: Creating missing database tables...
mysql -u root -p2003Rty. < fix-ue-assignment-tables.sql

if %errorlevel% neq 0 (
    echo ❌ Step 2 failed! Check MySQL connection.
    pause
    exit /b 1
)

echo ✅ Step 2 complete: Join tables created

echo.
echo 🔧 Step 3: Creating Enseignant entities...
mysql -u root -p2003Rty. < fix-enseignant-entities.sql

if %errorlevel% neq 0 (
    echo ❌ Step 3 failed! Check MySQL connection.
    pause
    exit /b 1
)

echo ✅ Step 3 complete: Enseignant entities created

echo.
echo 🔧 Step 4: Setting up complete test data...
mysql -u root -p2003Rty. < setup-complete-test-data.sql

if %errorlevel% neq 0 (
    echo ❌ Step 4 failed! Check MySQL connection.
    pause
    exit /b 1
)

echo ✅ Step 4 complete: Test data created

echo.
echo 🎉 ALL DATABASE FIXES APPLIED SUCCESSFULLY!
echo.
echo ⚠️  CRITICAL NEXT STEP:
echo    YOU MUST RESTART USERSERVICE NOW!
echo.
echo 📋 What was fixed:
echo   ✅ Created enseignant_ue join table
echo   ✅ Fixed foreign key constraints
echo   ✅ Created Enseignant entities for existing teachers
echo   ✅ Created UEs, Classes, and Course assignments
echo   ✅ Fixed UEService syntax errors (already done)
echo.
echo 🚀 After restarting UserService:
echo   1. Go to Admin Dashboard → UE Management
echo   2. Try assigning Pascal Abega to a UE
echo   3. Should work without 500 errors
echo.
echo 📊 Expected results:
echo   • UE assignment succeeds
echo   • No more "Could not commit JPA transaction"
echo   • Teacher appears in UE's assigned teachers list
echo.

pause