# Script pour tester le pipeline localement

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Pipeline CI/CD - Plannora" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"

# Fonction pour afficher le résultat
function Show-Result {
    param($step, $success)
    if ($success) {
        Write-Host "[OK] $step" -ForegroundColor Green
    } else {
        Write-Host "[ERREUR] $step" -ForegroundColor Red
    }
}

# 1. Build Backend Services
Write-Host "1. Build des services backend..." -ForegroundColor Yellow
Write-Host ""

$services = @(
    "EurekaService/eureka/eureka",
    "GatewayService/gateway/gateway",
    "AuthentificationService/Authentification/authentification",
    "UserService/user-service"
)

foreach ($service in $services) {
    Write-Host "  Building $service..." -ForegroundColor Gray
    Push-Location $service
    $result = mvn clean install -DskipTests 2>&1
    $success = $LASTEXITCODE -eq 0
    Pop-Location
    Show-Result "Build $service" $success
}

Write-Host ""

# 2. Build Frontend
Write-Host "2. Build du frontend..." -ForegroundColor Yellow
Write-Host ""

Push-Location "Frontend/plannora-frontend"
Write-Host "  Installing dependencies..." -ForegroundColor Gray
npm ci 2>&1 | Out-Null
$npmInstall = $LASTEXITCODE -eq 0

Write-Host "  Building..." -ForegroundColor Gray
npm run build 2>&1 | Out-Null
$npmBuild = $LASTEXITCODE -eq 0
Pop-Location

Show-Result "npm install" $npmInstall
Show-Result "npm build" $npmBuild

Write-Host ""

# 3. Tests Backend
Write-Host "3. Tests backend..." -ForegroundColor Yellow
Write-Host ""

Push-Location "UserService/user-service"
Write-Host "  Running tests..." -ForegroundColor Gray
mvn test 2>&1 | Out-Null
$testSuccess = $LASTEXITCODE -eq 0
Pop-Location

Show-Result "Backend tests" $testSuccess

Write-Host ""

# 4. Lint Frontend
Write-Host "4. Lint frontend..." -ForegroundColor Yellow
Write-Host ""

Push-Location "Frontend/plannora-frontend"
Write-Host "  Running lint..." -ForegroundColor Gray
npm run lint 2>&1 | Out-Null
$lintSuccess = $LASTEXITCODE -eq 0
Pop-Location

Show-Result "Frontend lint" $lintSuccess

Write-Host ""

# 5. Docker Build (optionnel)
Write-Host "5. Test Docker build..." -ForegroundColor Yellow
Write-Host ""

$dockerAvailable = Get-Command docker -ErrorAction SilentlyContinue
if ($dockerAvailable) {
    Write-Host "  Building UserService Docker image..." -ForegroundColor Gray
    docker build -t plannora/user-service:test ./UserService/user-service 2>&1 | Out-Null
    $dockerSuccess = $LASTEXITCODE -eq 0
    Show-Result "Docker build" $dockerSuccess
} else {
    Write-Host "  [SKIP] Docker non disponible" -ForegroundColor Yellow
}

Write-Host ""

# Résumé
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Résumé du pipeline" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Services backend:" -ForegroundColor White
foreach ($service in $services) {
    Write-Host "  - $service" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Frontend:" -ForegroundColor White
Write-Host "  - Build: $(if($npmBuild){'OK'}else{'ERREUR'})" -ForegroundColor $(if($npmBuild){'Green'}else{'Red'})
Write-Host "  - Lint: $(if($lintSuccess){'OK'}else{'ERREUR'})" -ForegroundColor $(if($lintSuccess){'Green'}else{'Red'})

Write-Host ""
Write-Host "Tests:" -ForegroundColor White
Write-Host "  - Backend: $(if($testSuccess){'OK'}else{'ERREUR'})" -ForegroundColor $(if($testSuccess){'Green'}else{'Red'})

Write-Host ""
Write-Host "Pour déployer avec Docker Compose:" -ForegroundColor Yellow
Write-Host "  docker-compose up --build" -ForegroundColor White
Write-Host ""
