@echo off
echo =====================================================
echo PLANNORA - DATABASE RESET ONLY
echo =====================================================
echo This will delete ALL data but keep services running
echo =====================================================
echo.

set /p confirm="Are you sure? Type YES to confirm: "
if not "%confirm%"=="YES" (
    echo Reset cancelled.
    pause
    exit /b
)

echo.
echo Resetting databases...

mysql -u root -p2003Rty. < reset-all-data.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ Database reset successful!
    echo.
    echo You can now test with clean data.
    echo Services are still running.
) else (
    echo.
    echo ❌ Database reset failed!
    echo Check MySQL connection and password.
)

echo.
pause