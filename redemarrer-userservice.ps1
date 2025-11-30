# Script pour redemarrer le UserService

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Redemarrage du UserService" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "IMPORTANT: Arretez d'abord le UserService en cours" -ForegroundColor Yellow
Write-Host "  - Allez dans le terminal du UserService" -ForegroundColor White
Write-Host "  - Appuyez sur Ctrl+C" -ForegroundColor White
Write-Host ""

$response = Read-Host "Le UserService est-il arrete? (o/n)"

if ($response -ne "o" -and $response -ne "O") {
    Write-Host "Operation annulee" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "1. Compilation du UserService..." -ForegroundColor Yellow

try {
    Set-Location "UserService/user-service"
    
    Write-Host "   Nettoyage et compilation..." -ForegroundColor Gray
    $output = mvn clean install -DskipTests 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   [OK] Compilation reussie" -ForegroundColor Green
    } else {
        Write-Host "   [ERREUR] Echec de la compilation" -ForegroundColor Red
        Write-Host $output
        Set-Location "../.."
        exit 1
    }
} catch {
    Write-Host "   [ERREUR] $($_.Exception.Message)" -ForegroundColor Red
    Set-Location "../.."
    exit 1
}

Write-Host ""
Write-Host "2. Demarrage du UserService..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Le UserService va demarrer dans ce terminal." -ForegroundColor Cyan
Write-Host "Attendez de voir: 'Started UserServiceApplication'" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour tester ensuite:" -ForegroundColor Yellow
Write-Host "  1. Ouvrez un NOUVEAU terminal" -ForegroundColor White
Write-Host "  2. Executez: .\test-direct-enseignants.ps1" -ForegroundColor White
Write-Host "  3. Testez le frontend: http://localhost:4200" -ForegroundColor White
Write-Host ""
Write-Host "Demarrage en cours..." -ForegroundColor Green
Write-Host ""

# Demarrer le service
mvn spring-boot:run

# Retour au repertoire racine si le service s'arrete
Set-Location "../.."
