@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

rem ============================================
rem  PLANORA - Demarrage de tous les services
rem  Lancer depuis le dossier projet-tutore
rem ============================================

set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"

rem --- Runtime Java compatible (JDK 17/21) ---
set "JAVA_HOME="

rem Prefer the exact Java 17 JDK installed on this machine and do not allow Java 25 to be used.
if exist "C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
)
if "%JAVA_HOME%"=="" if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
)
if "%JAVA_HOME%"=="" for /d %%D in (
    "C:\Program Files\Microsoft\jdk-17*"
    "C:\Program Files\Eclipse Adoptium\jdk-17*"
    "C:\Program Files\Java\jdk-17*"
    "C:\Program Files\Amazon Corretto\jdk17*"
) do (
    if exist "%%~D\bin\java.exe" (
        set "JAVA_HOME=%%~fD"
        goto java_detected
    )
)

:java_detected
if "%JAVA_HOME%"=="" (
    echo [ERREUR] JDK compatible introuvable.
    echo Installez Java 17 ou 21 ou adaptez JAVA_HOME.
    exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERREUR] JDK compatible introuvable.
    echo Installez Java 17 ou 21 ou adaptez JAVA_HOME.
    exit /b 1
)

rem Force this JDK for the current shell and every child process started afterward.
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "JAVA_HOME=%JAVA_HOME%"

if not exist "%BASE%\.env" (
    echo [ERREUR] Fichier .env introuvable dans %BASE%
    echo Copiez .env.example vers .env et renseignez DB_PASSWORD, JWT_SECRET et les variables MAIL_*.
    exit /b 1
)

for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%BASE%\.env") do (
    if not "%%A"=="" set "%%A=%%B"
)

echo Java utilise :
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo ========================================
echo   PLANORA - Demarrage des services
echo ========================================
echo.
echo Dossier de base : %BASE%
echo.

if not exist "%BASE%\EurekaService\eureka\eureka\mvnw.cmd" (
    echo [ERREUR] Maven Wrapper introuvable pour Eureka. Verifiez le chemin.
    pause
    exit /b 1
)

rem --- Copie du Maven Wrapper pour NotificationService si absent ---
if not exist "%BASE%\NotificationService\notification-service\mvnw.cmd" (
    echo [SETUP] Copie du Maven Wrapper vers NotificationService...
    xcopy /e /i /q "%BASE%\ClassesService\classes-service\.mvn" "%BASE%\NotificationService\notification-service\.mvn" >nul
    copy /y "%BASE%\ClassesService\classes-service\mvnw.cmd" "%BASE%\NotificationService\notification-service\mvnw.cmd" >nul
    copy /y "%BASE%\ClassesService\classes-service\mvnw"     "%BASE%\NotificationService\notification-service\mvnw"     >nul 2>&1
    echo       Maven Wrapper copie.
)

echo IMPORTANT : Avant de relancer, executez stop-all-services.bat pour liberer les ports.
echo.

rem ----- 1. Eureka (8761) -----
echo [1/11] Demarrage Eureka (port 8761)...
if exist "%BASE%\EurekaService\eureka\eureka\target" rmdir /s /q "%BASE%\EurekaService\eureka\eureka\target"
start "PLANORA - Eureka 8761" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\EurekaService\eureka\eureka"" && mvnw.cmd clean spring-boot:run"
echo       Attente 50 s que Eureka soit pret...
timeout /t 50 /nobreak >nul

rem ----- 2. UE Service (8090) -----
echo [2/11] Demarrage UE Service (port 8090)...
if exist "%BASE%\UEService\ue-service\target" rmdir /s /q "%BASE%\UEService\ue-service\target"
start "PLANORA - UEService 8090" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\UEService\ue-service"" && mvnw.cmd clean spring-boot:run"
echo       Attente 20 s...
timeout /t 20 /nobreak >nul

rem ----- 3. Gateway (8081) -----
echo [3/11] Demarrage Gateway (port 8081)...
if exist "%BASE%\GatewayService\gateway\gateway\target" rmdir /s /q "%BASE%\GatewayService\gateway\gateway\target"
start "PLANORA - Gateway 8081" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\GatewayService\gateway\gateway"" && mvnw.cmd clean spring-boot:run"
echo       Attente 30 s...
timeout /t 30 /nobreak >nul

rem ----- 4. Authentification (8085) -----
echo [4/11] Demarrage Authentification (port 8085)...
if exist "%BASE%\AuthentificationService\Authentification\authentification\target" rmdir /s /q "%BASE%\AuthentificationService\Authentification\authentification\target"
start "PLANORA - Auth 8085" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\AuthentificationService\Authentification\authentification"" && mvnw.cmd clean spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 5. User Service (8086) -----
echo [5/11] Demarrage User Service (port 8086)...
if exist "%BASE%\UserService\user-service\target" rmdir /s /q "%BASE%\UserService\user-service\target"
start "PLANORA - UserService 8086" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\UserService\user-service"" && mvnw.cmd clean spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 6. Planning Service (8084) -----
echo [6/11] Demarrage Planning Service (port 8084)...
if exist "%BASE%\PlanningService\planning-service\target" rmdir /s /q "%BASE%\PlanningService\planning-service\target"
start "PLANORA - PlanningService 8084" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\PlanningService\planning-service"" && mvnw.cmd clean spring-boot:run"
timeout /t 25 /nobreak >nul

rem ----- 7. Classes Service (8092) -----
echo [7/11] Demarrage Classes Service (port 8092)...
if exist "%BASE%\ClassesService\classes-service\target" rmdir /s /q "%BASE%\ClassesService\classes-service\target"
start "PLANORA - ClassesService 8092" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\ClassesService\classes-service"" && mvnw.cmd clean spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 8. Salle Service (8087) -----
echo [8/11] Demarrage Salle Service (port 8087)...
if exist "%BASE%\SalleService\salle-service\target" rmdir /s /q "%BASE%\SalleService\salle-service\target"
start "PLANORA - SalleService 8087" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\SalleService\salle-service"" && mvnw.cmd clean spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 9. Notification Service (8088) -----
echo [9/11] Demarrage Notification Service (port 8088)...
if exist "%BASE%\NotificationService\notification-service\target" rmdir /s /q "%BASE%\NotificationService\notification-service\target"
start "PLANORA - NotificationService 8088" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\NotificationService\notification-service"" && mvnw.cmd clean spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 10. Ecole Service (8089) -----
echo [10/11] Demarrage Ecole Service (port 8089)...
if exist "%BASE%\EcoleService\ecole-service\target" rmdir /s /q "%BASE%\EcoleService\ecole-service\target"
start "PLANORA - EcoleService 8089" cmd /k "set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot" && set "PATH=%JAVA_HOME%\bin;%PATH%" && cd /d ""%BASE%\EcoleService\ecole-service"" && mvnw.cmd clean spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 11. Frontend Angular -----
echo [11/11] Nettoyage du cache Angular et demarrage Frontend (port 4200)...
if exist "%BASE%\Frontend\plannora-frontend\.angular\cache" (
    rmdir /s /q "%BASE%\Frontend\plannora-frontend\.angular\cache"
    echo       Cache Angular supprime.
)
start "PLANORA - Frontend 4200" /d "%BASE%\Frontend\plannora-frontend" cmd /k "npm start -- --open"

echo.
echo ========================================
echo   Tous les services ont ete lances
echo ========================================
echo.
echo  Point d'entree unique : Gateway http://localhost:8081
echo  Frontend              : http://localhost:4200
echo.
echo  Services backend (enregistres dans Eureka) :
echo   Eureka              : http://localhost:8761
echo   Gateway             : http://localhost:8081
echo   Auth                : http://localhost:8085
echo   UserService         : http://localhost:8086
echo   UEService           : http://localhost:8090
echo   PlanningService     : http://localhost:8084
echo   ClassesService      : http://localhost:8092
echo   SalleService        : http://localhost:8087
echo   NotificationService : http://localhost:8088
echo   EcoleService        : http://localhost:8089
echo.
echo  Temps d'attente : 4-5 minutes pour que tous les services soient operationnels
echo.
echo Chaque service tourne dans sa propre fenetre.
echo Pour arreter tous les services : executez stop-all-services.bat
echo.
