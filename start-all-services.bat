@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

rem ============================================
rem  PLANORA - Demarrage de tous les services
rem  Lancer ce script depuis le dossier projet-tutore
rem  (Double-clic ou: cd chemin\vers\projet-tutore puis start-all-services.bat)
rem ============================================

set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"

echo.
echo ========================================
echo   PLANORA - Demarrage des services
echo ========================================
echo.
echo Dossier de base: %BASE%
echo.
echo Chaque service utilise mvnw.cmd (Maven Wrapper).
echo.

rem ----- 1. Eureka (8761) - DOIT demarrer en premier -----
echo [1/8] Eureka (port 8761)...
start "Eureka - 8761" cmd /k "cd /d ""%BASE%\EurekaService\eureka\eureka"" && mvnw.cmd spring-boot:run"
echo       Attente 45 s que Eureka soit pret...
timeout /t 45 /nobreak >nul

rem ----- 2. Gateway (8081) -----
echo [2/8] Gateway (port 8081)...
start "Gateway - 8081" cmd /k "cd /d ""%BASE%\GatewayService\gateway\gateway"" && mvnw.cmd spring-boot:run"
echo       Attente 25 s...
timeout /t 25 /nobreak >nul

rem ----- 3. Authentification (8085) -----
echo [3/8] Authentification (port 8085)...
start "Authentification - 8085" cmd /k "cd /d ""%BASE%\AuthentificationService\Authentification\authentification"" && mvnw.cmd spring-boot:run"
timeout /t 15 /nobreak >nul

rem ----- 4. User Service (8086) -----
echo [4/8] User Service (port 8086)...
start "UserService - 8086" cmd /k "cd /d ""%BASE%\UserService\user-service"" && mvnw.cmd spring-boot:run"
timeout /t 15 /nobreak >nul

rem ----- 5. Salle Service (8087) -----
echo [5/8] Salle Service (port 8087)...
start "SalleService - 8087" cmd /k "cd /d ""%BASE%\SalleService\salle-service"" && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

rem ----- 6. Planning Service (8088) -----
echo [6/8] Planning Service (port 8088)...
start "PlanningService - 8088" cmd /k "cd /d ""%BASE%\PlanningService\planning-service"" && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

rem ----- 7. Ecole Service (8089) -----
echo [7/8] Ecole Service (port 8089)...
start "EcoleService - 8089" cmd /k "cd /d ""%BASE%\EcoleService\ecole-service"" && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak >nul

rem ----- 8. UE Service (8090) -----
echo [8/8] UE Service (port 8090)...
start "UEService - 8090" cmd /k "cd /d ""%BASE%\UEService\ue-service"" && mvnw.cmd spring-boot:run"

echo.
echo ========================================
echo   Tous les services ont ete lances.
echo ========================================
echo.
echo Eureka       : http://localhost:8761
echo Gateway      : http://localhost:8081
echo Auth         : http://localhost:8085
echo UserService  : http://localhost:8086
echo SalleService : http://localhost:8087
echo Planning     : http://localhost:8088
echo EcoleService : http://localhost:8089
echo UEService    : http://localhost:8090
echo.
echo Chaque service tourne dans sa propre fenetre.
echo Fermez les fenetres pour arreter les services.
echo.
pause
