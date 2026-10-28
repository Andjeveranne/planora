@echo off
setlocal EnableDelayedExpansion

set "URL=https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.msi"
set "DST=C:\Tools\jdk17.msi"
set "JAVA_HOME_TARGET=C:\Program Files\Microsoft\jdk-17"

mkdir "C:\Tools" 2>nul

where bitsadmin >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    bitsadmin /transfer Java17MSI /download /priority normal "%URL%" "%DST%" >nul 2>&1
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%URL%' -OutFile '%DST%'"
)

if exist "%DST%" (
    echo Installing Java 17...
    msiexec.exe /i "%DST%" /qn /norestart
) else (
    echo Java 17 download failed.
    exit /b 1
)

if exist "%JAVA_HOME_TARGET%\bin\java.exe" (
    echo Java installed at %JAVA_HOME_TARGET%
    setx JAVA_HOME "%JAVA_HOME_TARGET%" >nul
    set "PATH=%JAVA_HOME_TARGET%\bin;%PATH%"
    "%JAVA_HOME_TARGET%\bin\java.exe" -version
    exit /b 0
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe" (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
        if exist "%%~D\bin\java.exe" (
            set "JAVA_HOME=%%~D"
            setx JAVA_HOME "%%~D" >nul
            set "PATH=%%~D\bin;%PATH%"
            "%%~D\bin\java.exe" -version
            exit /b 0
        )
    )
)

echo Java 17 was not found after installation.
exit /b 1
