@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

rem ============================================
rem  PLANORA - Demarrage de tous les services
rem  Lancer depuis le dossier projet-tutore
rem ============================================

set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"

rem --- Runtime Java 25 et configuration locale ---
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERREUR] JDK 25 introuvable : %JAVA_HOME%
    echo Installez Java 25 ou adaptez JAVA_HOME dans ce script.
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

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
start "PLANORA - Eureka 8761" /d "%BASE%\EurekaService\eureka\eureka" cmd /k "mvnw.cmd spring-boot:run"
echo       Attente 50 s que Eureka soit pret...
timeout /t 50 /nobreak >nul

rem ----- 2. UE Service (8090) -----
echo [2/11] Demarrage UE Service (port 8090)...
start "PLANORA - UEService 8090" /d "%BASE%\UEService\ue-service" cmd /k "mvnw.cmd spring-boot:run"
echo       Attente 20 s...
timeout /t 20 /nobreak >nul

rem ----- 3. Gateway (8081) -----
echo [3/11] Demarrage Gateway (port 8081)...
start "PLANORA - Gateway 8081" /d "%BASE%\GatewayService\gateway\gateway" cmd /k "mvnw.cmd spring-boot:run"
echo       Attente 30 s...
timeout /t 30 /nobreak >nul

rem ----- 4. Authentification (8085) -----
echo [4/11] Demarrage Authentification (port 8085)...
start "PLANORA - Auth 8085" /d "%BASE%\AuthentificationService\Authentification\authentification" cmd /k "mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 5. User Service (8086) -----
echo [5/11] Demarrage User Service (port 8086)...
start "PLANORA - UserService 8086" /d "%BASE%\UserService\user-service" cmd /k "mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 6. Planning Service (8084) -----
echo [6/11] Demarrage Planning Service (port 8084)...
start "PLANORA - PlanningService 8084" /d "%BASE%\PlanningService\planning-service" cmd /k "mvnw.cmd spring-boot:run"
timeout /t 25 /nobreak >nul

rem ----- 7. Classes Service (8092) -----
echo [7/11] Demarrage Classes Service (port 8092)...
start "PLANORA - ClassesService 8092" /d "%BASE%\ClassesService\classes-service" cmd /k "mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 8. Salle Service (8087) -----
echo [8/11] Demarrage Salle Service (port 8087)...
start "PLANORA - SalleService 8087" /d "%BASE%\SalleService\salle-service" cmd /k "mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 9. Notification Service (8088) -----
echo [9/11] Demarrage Notification Service (port 8088)...
start "PLANORA - NotificationService 8088" /d "%BASE%\NotificationService\notification-service" cmd /k "mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak >nul

rem ----- 10. Ecole Service (8089) -----
echo [10/11] Demarrage Ecole Service (port 8089)...
start "PLANORA - EcoleService 8089" /d "%BASE%\EcoleService\ecole-service" cmd /k "mvnw.cmd spring-boot:run"
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
