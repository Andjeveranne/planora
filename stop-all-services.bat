@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

rem ============================================
rem  PLANORA - Arret des services (liberation des ports)
rem ============================================

echo.
echo Arret des processus Java sur les ports PLANORA...
echo.

set PORTS=8761 8081 8084 8085 8086 8087 8088 8089 8090 8092
set N=0

for %%P in (%PORTS%) do (
    for /f "tokens=5" %%A in ('netstat -ano 2^>nul ^| findstr "%%P" ^| findstr "LISTENING"') do (
        set PID=%%A
        if not "!PID!"=="" (
            taskkill /PID !PID! /F >nul 2>&1
            if !errorlevel! equ 0 (
                echo Port %%P : processus !PID! arrete.
                set /a N+=1
            )
        )
    )
)

if %N% equ 0 (
    echo Aucun processus trouve sur les ports PLANORA.
) else (
    echo.
    echo %N% processus arrete(s). Les ports sont liberes.
)

echo.
pause
