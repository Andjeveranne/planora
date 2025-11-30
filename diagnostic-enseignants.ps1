# Script de diagnostic pour la gestion des enseignants

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnostic - Gestion des Enseignants" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verifier les services
Write-Host "1. Verification des services..." -ForegroundColor Yellow
Write-Host ""

$services = @(
    @{Name="Gateway"; Port=8888; Url="http://localhost:8888/actuator/health"},
    @{Name="Eureka"; Port=8761; Url="http://localhost:8761"},
    @{Name="Auth Service"; Port=8081; Url="http://localhost:8081/actuator/health"},
    @{Name="User Service"; Port=8082; Url="http://localhost:8082/actuator/health"}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri $service.Url -TimeoutSec 2 -ErrorAction Stop
        Write-Host "[OK] $($service.Name) (Port $($service.Port))" -ForegroundColor Green
    } catch {
        Write-Host "[ERREUR] $($service.Name) (Port $($service.Port))" -ForegroundColor Red
    }
}

Write-Host ""

# 2. Test de connexion
Write-Host "2. Test de connexion..." -ForegroundColor Yellow
$authUrl = "http://localhost:8888/auth-service/api/auth/login"

try {
    $loginBody = @{
        email = "admin@plannora.com"
        mdp = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri $authUrl -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    $token = $loginResponse.token
    
    Write-Host "[OK] Connexion reussie" -ForegroundColor Green
    Write-Host "  Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "  Role: $($loginResponse.role)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "[ERREUR] Erreur de connexion: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# 3. Test de l'endpoint enseignants
Write-Host "3. Test de l'endpoint /enseignants..." -ForegroundColor Yellow

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$userUrl = "http://localhost:8888/user-service/api/utilisateurs/enseignants"

try {
    $enseignants = Invoke-RestMethod -Uri $userUrl -Method Get -Headers $headers -ErrorAction Stop
    Write-Host "[OK] Endpoint accessible" -ForegroundColor Green
    Write-Host "  Nombre d'enseignants: $($enseignants.Count)" -ForegroundColor Gray
    
    if ($enseignants.Count -gt 0) {
        Write-Host "  Premier enseignant:" -ForegroundColor Gray
        Write-Host "    - Nom: $($enseignants[0].nomUser) $($enseignants[0].prenomUser)" -ForegroundColor Gray
        Write-Host "    - Email: $($enseignants[0].email)" -ForegroundColor Gray
    } else {
        Write-Host "  [ATTENTION] Aucun enseignant dans la base de donnees" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "[ERREUR] Erreur lors de l'acces a l'endpoint" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# 4. Test de creation d'enseignant
Write-Host "4. Test de creation d'enseignant..." -ForegroundColor Yellow

$newEnseignant = @{
    email = "test.diagnostic.$(Get-Random)@plannora.com"
    mdp = "password123"
    nomUser = "Test"
    prenomUser = "Diagnostic"
    telephone = "+221 77 000 00 00"
    role = "ENSEIGNANT"
    specialite = "Test"
    departement = "Diagnostic"
} | ConvertTo-Json

try {
    $createUrl = "http://localhost:8888/user-service/api/utilisateurs/enseignant"
    $created = Invoke-RestMethod -Uri $createUrl -Method Post -Body $newEnseignant -Headers $headers -ErrorAction Stop
    Write-Host "[OK] Creation reussie" -ForegroundColor Green
    Write-Host "  ID: $($created.idUser)" -ForegroundColor Gray
    Write-Host ""
    
    # Nettoyage
    Write-Host "5. Nettoyage..." -ForegroundColor Yellow
    try {
        $deleteUrl = "http://localhost:8888/user-service/api/utilisateurs/$($created.idUser)"
        Invoke-RestMethod -Uri $deleteUrl -Method Delete -Headers $headers -ErrorAction Stop
        Write-Host "[OK] Enseignant de test supprime" -ForegroundColor Green
    } catch {
        Write-Host "[ATTENTION] Impossible de supprimer l'enseignant de test" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERREUR] Erreur lors de la creation" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
}

# Resume
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnostic termine!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si tous les tests sont OK, verifiez:" -ForegroundColor Yellow
Write-Host "1. La console du navigateur (F12)" -ForegroundColor White
Write-Host "2. L'onglet Network pour voir les requetes" -ForegroundColor White
Write-Host "3. Que le token est present dans localStorage" -ForegroundColor White
Write-Host ""
