@echo off
echo ===============================================
echo === Diagnostic et réparation MySQL ===
echo ===============================================

echo.
echo 1. Vérification du service MySQL...
sc query MySQL80 >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Service MySQL80 non trouvé
    echo Vérifiez que MySQL est installé
    pause
    exit /b 1
) else (
    echo [OK] Service MySQL80 trouvé
)

echo.
echo 2. Démarrage du service MySQL...
net start MySQL80 >nul 2>&1
if errorlevel 1 (
    echo [INFO] MySQL était déjà démarré ou erreur de démarrage
) else (
    echo [OK] MySQL démarré
)

echo.
echo 3. Test de connexion sans mot de passe...
mysql -u root -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo [OK] Connexion root sans mot de passe réussie
    echo Configuration du mot de passe root...
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';"
    mysql -u root -e "FLUSH PRIVILEGES;"
    echo [OK] Mot de passe root configuré
    goto :test_final
)

echo.
echo 4. Test avec différents mots de passe courants...
set passwords=root password admin 123456 mysql

for %%p in (%passwords%) do (
    echo Test avec mot de passe: %%p
    mysql -u root -p%%p -e "SELECT 1;" >nul 2>&1
    if not errorlevel 1 (
        echo [OK] Connexion réussie avec mot de passe: %%p
        if not "%%p"=="root" (
            echo Changement du mot de passe vers 'root'...
            mysql -u root -p%%p -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';"
            mysql -u root -p%%p -e "FLUSH PRIVILEGES;"
            echo [OK] Mot de passe changé vers 'root'
        )
        goto :test_final
    )
)

echo.
echo [ERREUR] Aucun mot de passe courant ne fonctionne
echo.
echo === Solutions manuelles ===
echo 1. Réinitialiser le mot de passe MySQL:
echo    - Arrêtez MySQL: net stop MySQL80
echo    - Démarrez en mode sécurisé: mysqld --skip-grant-tables
echo    - Connectez-vous: mysql -u root
echo    - Changez le mot de passe: ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
echo    - Redémarrez MySQL normalement
echo.
echo 2. Ou utilisez MySQL Workbench pour changer le mot de passe
echo.
pause
exit /b 1

:test_final
echo.
echo 5. Test final avec root/root...
mysql -u root -proot -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] MySQL est maintenant configuré avec root/root
    
    echo.
    echo 6. Création de la base de données PlannoraDB...
    mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS PlannoraDB;"
    echo [OK] Base de données PlannoraDB créée/vérifiée
    
    echo.
    echo === Configuration terminée ===
    echo MySQL est prêt pour Plannora
    echo Vous pouvez maintenant lancer: start-plannora.bat
) else (
    echo [ERREUR] La configuration a échoué
    echo Contactez l'administrateur système
)

echo.
pause