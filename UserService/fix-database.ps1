# Script PowerShell pour corriger la structure de la base de données
# Exécute le script SQL pour créer les tables manquantes

Write-Host "Correction de la structure de la base de données..." -ForegroundColor Yellow

try {
    # Exécuter le script SQL
    $sqlFile = "fix-database-structure.sql"
    
    if (Test-Path $sqlFile) {
        Write-Host "Exécution du script SQL: $sqlFile" -ForegroundColor Green
        
        # Utiliser mysql pour exécuter le script
        mysql -u root -p2003Rty. < $sqlFile
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Structure de la base de données corrigée avec succès!" -ForegroundColor Green
        } else {
            Write-Host "❌ Erreur lors de l'exécution du script SQL" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Fichier SQL non trouvé: $sqlFile" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Script terminé." -ForegroundColor Yellow