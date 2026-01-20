@echo off
echo ===============================================
echo === Configuration MySQL avec mot de passe 2003Rty. ===
echo ===============================================

echo.
echo Test de connexion avec différents mots de passe...

REM Test sans mot de passe
mysql -u root -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo [OK] Connexion sans mot de passe réussie
    echo Configuration du mot de passe vers 2003Rty....
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '2003Rty.';"
    mysql -u root -e "FLUSH PRIVILEGES;"
    echo [OK] Mot de passe configuré
    goto :test_final
)

REM Test avec root
mysql -u root -proot -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo [OK] Connexion avec 'root' réussie
    echo Changement vers 2003Rty....
    mysql -u root -proot -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '2003Rty.';"
    mysql -u root -proot -e "FLUSH PRIVILEGES;"
    echo [OK] Mot de passe changé
    goto :test_final
)

REM Test avec le mot de passe actuel
mysql -u root -p"2003Rty." -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo [OK] Le mot de passe 2003Rty. est déjà configuré
    goto :test_final
)

echo [ERREUR] Impossible de se connecter à MySQL
echo Utilisez MySQL Workbench ou la console MySQL pour configurer le mot de passe
pause
exit /b 1

:test_final
echo.
echo Test final avec 2003Rty....
mysql -u root -p"2003Rty." -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] MySQL configuré avec le mot de passe 2003Rty.
    
    echo.
    echo Création de la base de données...
    mysql -u root -p"2003Rty." -e "CREATE DATABASE IF NOT EXISTS PlannoraDB;"
    echo [OK] Base de données PlannoraDB créée
    
    echo.
    echo === Configuration terminée ===
    echo MySQL est prêt avec le mot de passe 2003Rty.
) else (
    echo [ERREUR] La configuration a échoué
)

echo.
pause