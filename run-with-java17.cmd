@echo off
setlocal EnableDelayedExpansion

set "JAVA_HOME="

rem Prefer the exact Java 17 installation present on this machine.
if exist "C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
)

if "%JAVA_HOME%"=="" for /d %%D in (
    "C:\Program Files\Microsoft\jdk-17*"
    "C:\Program Files\Eclipse Adoptium\jdk-17*"
    "C:\Program Files\Java\jdk-17*"
    "C:\Program Files\Amazon Corretto\jdk17*"
    "C:\Program Files\Microsoft\jdk-21*"
    "C:\Program Files\Eclipse Adoptium\jdk-21*"
    "C:\Program Files\Java\jdk-21*"
    "C:\Program Files\Amazon Corretto\jdk21*"
) do (
    if exist "%%~D\bin\java.exe" (
        set "JAVA_HOME=%%~fD"
        goto java_found
    )
)

if "%JAVA_HOME%"=="" (
    for /f "delims=" %%I in ('where java 2^>nul') do (
        set "JAVA_PATH=%%~fI"
        if exist "!JAVA_PATH!" (
            for %%J in ("!JAVA_PATH!") do set "JAVA_HOME=%%~dpJ"
            set "JAVA_HOME=!JAVA_HOME:bin\=!"
            if exist "!JAVA_HOME!\bin\java.exe" goto java_found
        )
    )
)

:java_found
if "%JAVA_HOME%"=="" (
    echo [ERREUR] Aucune installation Java 17/21 trouvee.
    exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERREUR] Java introuvable dans %JAVA_HOME%
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
set "JAVA_HOME=%JAVA_HOME%"

echo [JAVA] Utilise : %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version

if "%~1"=="" (
    exit /b 0
)

cd /d "%~1"
shift

if exist "target" (
    echo [CLEAN] Suppression des classes precedentes dans %CD%\target
    rmdir /s /q "target"
)

call %*
exit /b %ERRORLEVEL%
