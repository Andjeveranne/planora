# Script de migration de GitHub vers GitLab
# Ce script retire les sous-modules et prépare le projet pour GitLab

Write-Host "=== Migration de Plannora vers GitLab ===" -ForegroundColor Cyan
Write-Host ""

# Vérifierer les sous-modules Git
Write-Host "Étape 1: Suppression des références Git dans les sous-modules..." -ForegroundColor Yellow

$submodules = @(
    "AuthentificationService",
    "NotificationService",
    "SalleService",
    "ReservationService",
    "IntegrationService",
    "UserService",
    "ReportingService",
    "PlanningService",
    "GatewayService",
    "EurekaService"
)

foreach ($submodule in $submodules) {
    if (Test-Path "$submodule/.git") {
        Write-Host "  - Suppression de $submodule/.git" -ForegroundColor Gray
        Remove-Item -Path "$submodule/.git" -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# 2. Nettoyer la configuration Git des sous-modules
Write-Host ""
Write-Host "Étape 2: Nettoyage de la configuration Git..." -ForegroundColor Yellow

if (Test-Path ".git/modules") {
    Write-Host "  - Suppression de .git/modules" -ForegroundColor Gray
    Remove-Item -Path ".git/modules" -Recurse -Force -ErrorAction SilentlyContinue
}

# Retirer les entrées de sous-modules du .git/config
git config --remove-section submodule.AuthentificationService 2>$null
git config --remove-section submodule.NotificationService 2>$null
git config --remove-section submodule.SalleService 2>$null
git config --remove-section submodule.ReservationService 2>$null
git config --remove-section submodule.IntegrationService 2>$null
git config --remove-section submodule.UserService 2>$null
git config --remove-section submodule.ReportingService 2>$null
git config --remove-section submodule.PlanningService 2>$null
git config --remove-section submodule.GatewayService 2>$null
git config --remove-section submodule.EurekaService 2>$null

# 3. Ajouter tous les fichiers au dépôt principal
Write-Host ""
Write-Host "Étape 3: Ajout de tous les fichiers au dépôt principal..." -ForegroundColor Yellow
git add -A

# 4. Créer un c