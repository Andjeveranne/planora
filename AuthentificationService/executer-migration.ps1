# Script PowerShell pour exécuter la migration SQL
# Supprime la table 'users' et utilise 'utilisateurs'

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   MIGRATION : users → utilisateurs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration MySQL
$mysqlUser = "root"
$mysqlPassword = "root"
$database = "PlannoraDB"

Write-Host "Configuration :" -ForegroundColor Yellow
Write-Host "  Base de données : $database" -ForegroundColor Gray
Write-Host "  Utilisateur : $mysqlUser" -ForegroundColor Gray
Write-Host ""

# Vérifier si MySQL est accessible
Write-Host "Vérification de MySQL..." -ForegroundColor Yellow
try {
    $testConnection = Test-NetConnection -ComputerName localhost -Port 3306 -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($testConnection) {
        Write-Host "✅ MySQL est accessible (port 3306)" -ForegroundColor Green
    } else {
        Write-Host "❌ MySQL n'est pas accessible" -ForegroundColor Red
        Write-Host "   Démarrez MySQL et réessayez" -ForegroundColor Gray
        exit
    }
} catch {
    Write-Host "❌ Erreur lors de la vérification de MySQL" -ForegroundColor Red
    exit
}

Write-Host ""

# Exécuter la migration
Write-Host "Exécution de la migration..." -ForegroundColor Yellow
Write-Host ""

# Commandes SQL à exécuter
$sqlCommands = @"
USE $database;

-- Sauvegarder les données de users (si elle existe)
DROP TABLE IF EXISTS users_backup;
CREATE TABLE IF NOT EXISTS users_backup LIKE users;
INSERT INTO users_backup SELECT * FROM users WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = '$database' AND table_name = 'users');

-- Supprimer la table users
DROP TABLE IF EXISTS users;

-- Afficher les tables
SHOW TABLES;
"@

# Écrire les commandes dans un fichier temporaire
$tempFile = [System.IO.Path]::GetTempFileName() + ".sql"
$sqlCommands | Out-File -FilePath $tempFile -Encoding UTF8

Write-Host "Commandes SQL à exécuter :" -ForegroundColor Gray
Write-Host $sqlCommands -ForegroundColor DarkGray
Write-Host ""

# Exécuter avec mysql.exe
try {
    Write-Host "Exécution de la migration..." -ForegroundColor Yellow
    
    # Chercher mysql.exe
    $mysqlPath = "mysql"
    
    # Exécuter la migration
    $result = & $mysqlPath -u $mysqlUser -p$mysqlPassword -e $sqlCommands 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Migration réussie !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Résultat :" -ForegroundColor Yellow
        Write-Host $result -ForegroundColor Gray
    } else {
        Write-Host "❌ Erreur lors de la migration" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Vous pouvez exécuter manuellement le fichier SQL :" -ForegroundColor Yellow
    Write-Host "   mysql -u $mysqlUser -p$mysqlPassword < migration-to-utilisateurs.sql" -ForegroundColor Gray
} finally {
    # Nettoyer le fichier temporaire
    if (Test-Path $tempFile) {
        Remove-Item $tempFile -Force
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   PROCHAINES ÉTAPES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Arrêter les services en cours" -ForegroundColor Yellow
Write-Host "2. Redémarrer le service d'authentification" -ForegroundColor Yellow
Write-Host "3. Redémarrer le UserService" -ForegroundColor Yellow
Write-Host "4. Tester la connexion avec admin@plannora.com / admin123" -ForegroundColor Yellow
Write-Host ""
