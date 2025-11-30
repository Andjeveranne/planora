# Script de test de connexion pour Plannora
Write-Host "=== Test de connexion Plannora ===" -ForegroundColor Cyan

# URL de l'API (Port mis à jour: 8085)
$apiUrl = "http://localhost:8085/api/auth"

# Test 1: Vérifier le nombre d'utilisateurs
Write-Host "`n1. Vérification du nombre d'utilisateurs..." -ForegroundColor Yellow
try {
    $debugResponse = Invoke-RestMethod -Uri "$apiUrl/debug/users" -Method Get
    Write-Host "✅ Nombre d'utilisateurs: $($debugResponse.totalUsers)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors de la vérification: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Connexion Admin
Write-Host "`n2. Test de connexion Admin..." -ForegroundColor Yellow
$adminCredentials = @{
    email = "admin@plannora.com"
    password = "password123"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $adminCredentials -ContentType "application/json"
    Write-Host "✅ Connexion Admin réussie!" -ForegroundColor Green
    Write-Host "   Token: $($adminResponse.token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "   Nom: $($adminResponse.prenom) $($adminResponse.nom)" -ForegroundColor Gray
    Write-Host "   Role: $($adminResponse.role)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur de connexion Admin: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Détails: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test 3: Connexion Enseignant
Write-Host "`n3. Test de connexion Enseignant..." -ForegroundColor Yellow
$enseignantCredentials = @{
    email = "enseignant@plannora.com"
    password = "password123"
} | ConvertTo-Json

try {
    $enseignantResponse = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $enseignantCredentials -ContentType "application/json"
    Write-Host "✅ Connexion Enseignant réussie!" -ForegroundColor Green
    Write-Host "   Token: $($enseignantResponse.token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "   Nom: $($enseignantResponse.prenom) $($enseignantResponse.nom)" -ForegroundColor Gray
    Write-Host "   Role: $($enseignantResponse.role)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur de connexion Enseignant: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Détails: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test 4: Connexion avec mauvais mot de passe
Write-Host "`n4. Test avec mauvais mot de passe..." -ForegroundColor Yellow
$badCredentials = @{
    email = "admin@plannora.com"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    $badResponse = Invoke-RestMethod -Uri "$apiUrl/login" -Method Post -Body $badCredentials -ContentType "application/json"
    Write-Host "❌ PROBLÈME: La connexion a réussi avec un mauvais mot de passe!" -ForegroundColor Red
} catch {
    Write-Host "✅ Rejet correct du mauvais mot de passe" -ForegroundColor Green
}

Write-Host "`n=== Tests terminés ===" -ForegroundColor Cyan
Write-Host "`nIdentifiants de test:" -ForegroundColor Yellow
Write-Host "  Admin: admin@plannora.com / password123" -ForegroundColor White
Write-Host "  Enseignant: enseignant@plannora.com / password123" -ForegroundColor White
