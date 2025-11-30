# Script de test pour la gestion des enseignants
# Ce script teste les endpoints de l'API UserService

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test de la gestion des enseignants" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8888"
$authUrl = "$baseUrl/auth-service/api/auth/login"
$userUrl = "$baseUrl/user-service/api/utilisateurs"

# Fonction pour afficher les résultats
function Show-Result {
    param($title, $response, $statusCode)
    Write-Host ">>> $title" -ForegroundColor Yellow
    Write-Host "Status: $statusCode" -ForegroundColor $(if ($statusCode -ge 200 -and $statusCode -lt 300) { "Green" } else { "Red" })
    if ($response) {
        Write-Host ($response | ConvertTo-Json -Depth 5)
    }
    Write-Host ""
}

# 1. Connexion en tant qu'administrateur
Write-Host "1. Connexion en tant qu'administrateur..." -ForegroundColor Cyan
try {
    $loginBody = @{
        email = "admin@plannora.com"
        mdp = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri $authUrl -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    
    Show-Result "Connexion réussie" $loginResponse 200
    
    if (-not $token) {
        Write-Host "ERREUR: Token non reçu" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERREUR lors de la connexion: $_" -ForegroundColor Red
    exit 1
}

# Headers avec le token
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Récupérer la liste des enseignants
Write-Host "2. Récupération de la liste des enseignants..." -ForegroundColor Cyan
try {
    $enseignants = Invoke-RestMethod -Uri "$userUrl/enseignants" -Method Get -Headers $headers
    Show-Result "Liste des enseignants" $enseignants 200
    Write-Host "Nombre d'enseignants: $($enseignants.Count)" -ForegroundColor Green
} catch {
    Write-Host "ERREUR lors de la récupération: $_" -ForegroundColor Red
}

# 3. Créer un nouvel enseignant
Write-Host "3. Création d'un nouvel enseignant..." -ForegroundColor Cyan
try {
    $newEnseignant = @{
        email = "test.enseignant.$(Get-Random)@plannora.com"
        mdp = "password123"
        nomUser = "Diop"
        prenomUser = "Amadou"
        telephone = "+221 77 123 45 67"
        role = "ENSEIGNANT"
        specialite = "Informatique"
        departement = "Sciences et Technologies"
    } | ConvertTo-Json

    $createdEnseignant = Invoke-RestMethod -Uri "$userUrl/enseignant" -Method Post -Body $newEnseignant -Headers $headers
    Show-Result "Enseignant créé" $createdEnseignant 201
    
    $enseignantId = $createdEnseignant.idUser
    Write-Host "ID de l'enseignant créé: $enseignantId" -ForegroundColor Green
} catch {
    Write-Host "ERREUR lors de la création: $_" -ForegroundColor Red
    $enseignantId = $null
}

# 4. Récupérer l'enseignant créé
if ($enseignantId) {
    Write-Host "4. Récupération de l'enseignant créé..." -ForegroundColor Cyan
    try {
        $enseignant = Invoke-RestMethod -Uri "$userUrl/$enseignantId" -Method Get -Headers $headers
        Show-Result "Enseignant récupéré" $enseignant 200
    } catch {
        Write-Host "ERREUR lors de la récupération: $_" -ForegroundColor Red
    }
}

# 5. Mettre à jour l'enseignant
if ($enseignantId) {
    Write-Host "5. Mise à jour de l'enseignant..." -ForegroundColor Cyan
    try {
        $updateData = @{
            nomUser = "Diop"
            prenomUser = "Amadou"
            email = $createdEnseignant.email
            telephone = "+221 77 999 88 77"
            specialite = "Intelligence Artificielle"
            departement = "Sciences et Technologies"
        } | ConvertTo-Json

        $updatedEnseignant = Invoke-RestMethod -Uri "$userUrl/$enseignantId" -Method Put -Body $updateData -Headers $headers
        Show-Result "Enseignant mis à jour" $updatedEnseignant 200
    } catch {
        Write-Host "ERREUR lors de la mise à jour: $_" -ForegroundColor Red
    }
}

# 6. Récupérer à nouveau la liste
Write-Host "6. Récupération de la liste mise à jour..." -ForegroundColor Cyan
try {
    $enseignantsUpdated = Invoke-RestMethod -Uri "$userUrl/enseignants" -Method Get -Headers $headers
    Show-Result "Liste mise à jour" $enseignantsUpdated 200
    Write-Host "Nombre d'enseignants: $($enseignantsUpdated.Count)" -ForegroundColor Green
} catch {
    Write-Host "ERREUR lors de la récupération: $_" -ForegroundColor Red
}

# 7. Supprimer l'enseignant de test
if ($enseignantId) {
    Write-Host "7. Suppression de l'enseignant de test..." -ForegroundColor Cyan
    try {
        Invoke-RestMethod -Uri "$userUrl/$enseignantId" -Method Delete -Headers $headers
        Write-Host ">>> Enseignant supprimé avec succès" -ForegroundColor Yellow
        Write-Host "Status: 204" -ForegroundColor Green
        Write-Host ""
    } catch {
        Write-Host "ERREUR lors de la suppression: $_" -ForegroundColor Red
    }
}

# 8. Vérifier la suppression
Write-Host "8. Vérification de la suppression..." -ForegroundColor Cyan
try {
    $enseignantsFinal = Invoke-RestMethod -Uri "$userUrl/enseignants" -Method Get -Headers $headers
    Show-Result "Liste finale" $enseignantsFinal 200
    Write-Host "Nombre d'enseignants: $($enseignantsFinal.Count)" -ForegroundColor Green
} catch {
    Write-Host "ERREUR lors de la vérification: $_" -ForegroundColor Red
}

# Résumé
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tests terminés!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour tester l'interface web:" -ForegroundColor Yellow
Write-Host "1. Ouvrez http://localhost:4200" -ForegroundColor White
Write-Host "2. Connectez-vous avec admin@plannora.com / admin123" -ForegroundColor White
Write-Host "3. Cliquez sur 'Enseignants' dans le menu" -ForegroundColor White
Write-Host "4. Testez l'ajout et la suppression d'enseignants" -ForegroundColor White
Write-Host ""
