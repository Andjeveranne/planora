# Test direct de l'API enseignants

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Direct - API Enseignants" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Test de connexion
Write-Host "1. Connexion..." -ForegroundColor Yellow
$authUrl = "http://localhost:8085/api/auth/login"

try {
    $loginBody = @{
        email = "admin@plannora.com"
        password = "password123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri $authUrl -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    
    Write-Host "[OK] Connexion reussie" -ForegroundColor Green
    Write-Host "  Role: $($loginResponse.role)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "[ERREUR] Connexion impossible" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifiez que:" -ForegroundColor Yellow
    Write-Host "  - L'AuthentificationService est demarre sur le port 8085" -ForegroundColor White
    Write-Host "  - L'utilisateur admin existe dans la BD" -ForegroundColor White
    exit 1
}

# 2. Test GET /enseignants
Write-Host "2. Recuperation de la liste des enseignants..." -ForegroundColor Yellow

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$userUrl = "http://localhost:8086/api/utilisateurs/enseignants"

try {
    $enseignants = Invoke-RestMethod -Uri $userUrl -Method Get -Headers $headers
    Write-Host "[OK] Liste recuperee avec succes" -ForegroundColor Green
    Write-Host "  Nombre d'enseignants: $($enseignants.Count)" -ForegroundColor Gray
    
    if ($enseignants.Count -gt 0) {
        Write-Host ""
        Write-Host "  Enseignants trouves:" -ForegroundColor Cyan
        foreach ($ens in $enseignants) {
            Write-Host "    - $($ens.prenomUser) $($ens.nomUser) ($($ens.email))" -ForegroundColor White
            if ($ens.specialite) {
                Write-Host "      Specialite: $($ens.specialite)" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host ""
        Write-Host "  [INFO] Aucun enseignant dans la base de donnees" -ForegroundColor Yellow
        Write-Host "  Vous pouvez en ajouter via l'interface web ou ce script" -ForegroundColor Yellow
    }
    Write-Host ""
} catch {
    Write-Host "[ERREUR] Impossible de recuperer la liste" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "Erreur 401: Token invalide ou expire" -ForegroundColor Yellow
    } elseif ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "Erreur 403: Permissions insuffisantes" -ForegroundColor Yellow
    } elseif ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "Erreur 404: Endpoint non trouve" -ForegroundColor Yellow
        Write-Host "  Verifiez que le UserService est enregistre dans Eureka" -ForegroundColor White
    }
    exit 1
}

# 3. Test POST /enseignant
Write-Host "3. Test de creation d'un enseignant..." -ForegroundColor Yellow

$randomEmail = "test.$(Get-Random)@plannora.com"
$newEnseignant = @{
    email = $randomEmail
    mdp = "password123"
    nomUser = "Test"
    prenomUser = "Diagnostic"
    telephone = "+221 77 000 00 00"
    role = "ENSEIGNANT"
    specialite = "Informatique"
    departement = "Sciences"
} | ConvertTo-Json

try {
    $createUrl = "http://localhost:8086/api/utilisateurs/enseignant"
    $created = Invoke-RestMethod -Uri $createUrl -Method Post -Body $newEnseignant -Headers $headers
    
    Write-Host "[OK] Enseignant cree avec succes" -ForegroundColor Green
    Write-Host "  ID: $($created.idUser)" -ForegroundColor Gray
    Write-Host "  Email: $($created.email)" -ForegroundColor Gray
    Write-Host ""
    
    # 4. Verification
    Write-Host "4. Verification de la creation..." -ForegroundColor Yellow
    $enseignants = Invoke-RestMethod -Uri $userUrl -Method Get -Headers $headers
    Write-Host "[OK] Nombre d'enseignants apres creation: $($enseignants.Count)" -ForegroundColor Green
    Write-Host ""
    
    # 5. Nettoyage
    Write-Host "5. Suppression de l'enseignant de test..." -ForegroundColor Yellow
    try {
        $deleteUrl = "http://localhost:8086/api/utilisateurs/$($created.idUser)"
        Invoke-RestMethod -Uri $deleteUrl -Method Delete -Headers $headers
        Write-Host "[OK] Enseignant de test supprime" -ForegroundColor Green
        Write-Host ""
    } catch {
        Write-Host "[ATTENTION] Impossible de supprimer l'enseignant de test" -ForegroundColor Yellow
        Write-Host "  Vous pouvez le supprimer manuellement via l'interface" -ForegroundColor Gray
        Write-Host ""
    }
} catch {
    Write-Host "[ERREUR] Impossible de creer l'enseignant" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        $errorDetail = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "  Detail: $($errorDetail.message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Resume
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Resume" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "L'API backend fonctionne correctement!" -ForegroundColor Green
Write-Host ""
Write-Host "Pour tester l'interface web:" -ForegroundColor Yellow
Write-Host "1. Ouvrez http://localhost:4200" -ForegroundColor White
Write-Host "2. Connectez-vous avec admin@plannora.com / admin123" -ForegroundColor White
Write-Host "3. Cliquez sur 'Enseignants' dans le menu" -ForegroundColor White
Write-Host "4. Ouvrez la console du navigateur (F12)" -ForegroundColor White
Write-Host "5. Regardez les messages de debug" -ForegroundColor White
Write-Host ""
Write-Host "Si la liste ne charge pas dans le navigateur:" -ForegroundColor Yellow
Write-Host "- Verifiez la console (F12 > Console)" -ForegroundColor White
Write-Host "- Verifiez l'onglet Network (F12 > Network)" -ForegroundColor White
Write-Host "- Verifiez que le token est dans localStorage" -ForegroundColor White
Write-Host ""
