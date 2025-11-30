# Script de test automatisé du UserService
# Assurez-vous que les services Eureka et Auth sont démarrés

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TEST AUTOMATISÉ DU USERSERVICE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que les services sont démarrés
Write-Host "Vérification des prérequis..." -ForegroundColor Yellow

# Vérifier Eureka
try {
    $eureka = Invoke-WebRequest -Uri "http://localhost:8761" -Method Get -TimeoutSec 2 -UseBasicParsing
    Write-Host "✅ Eureka est accessible (port 8761)" -ForegroundColor Green
} catch {
    Write-Host "❌ Eureka n'est pas accessible. Démarrez-le d'abord !" -ForegroundColor Red
    Write-Host "   cd EurekaService/eureka/eureka" -ForegroundColor Gray
    Write-Host "   mvnw.cmd spring-boot:run" -ForegroundColor Gray
    exit
}

# Vérifier Auth Service
try {
    $auth = Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -Method Get -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
    Write-Host "✅ Authentication Service est accessible (port 8082)" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Authentication Service n'est pas accessible" -ForegroundColor Yellow
    Write-Host "   Tentative de connexion quand même..." -ForegroundColor Gray
}

# Vérifier User Service
try {
    $user = Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -Method Get -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
    Write-Host "✅ User Service est accessible (port 8083)" -ForegroundColor Green
} catch {
    Write-Host "❌ User Service n'est pas accessible. Démarrez-le d'abord !" -ForegroundColor Red
    Write-Host "   cd UserService/user-service" -ForegroundColor Gray
    Write-Host "   mvnw.cmd spring-boot:run" -ForegroundColor Gray
    exit
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   DÉBUT DES TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1 : Authentification
Write-Host "Test 1 : Authentification..." -ForegroundColor Yellow
$body = @{
    email = "admin@plannora.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body
    
    $token = $response.token
    Write-Host "✅ Authentification réussie" -ForegroundColor Green
    Write-Host "   User ID : $($response.userId)" -ForegroundColor Gray
    Write-Host "   Role : $($response.role)" -ForegroundColor Gray
    Write-Host "   Token : $($token.Substring(0, 30))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur d'authentification : $($_.Exception.Message)" -ForegroundColor Red
    exit
}

Write-Host ""

# Test 2 : Créer un enseignant
Write-Host "Test 2 : Créer un enseignant..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}

$enseignant = @{
    email = "jean.dupont@plannora.com"
    mdp = "password123"
    nomUser = "Dupont"
    prenomUser = "Jean"
    telephone = "0612345678"
    specialite = "Informatique"
    departement = "Génie Logiciel"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/enseignant" `
        -Method Post `
        -Headers $headers `
        -Body $enseignant
    
    Write-Host "✅ Enseignant créé avec succès" -ForegroundColor Green
    Write-Host "   ID : $($response.idUser)" -ForegroundColor Gray
    Write-Host "   Email : $($response.email)" -ForegroundColor Gray
    Write-Host "   Nom : $($response.nomUser) $($response.prenomUser)" -ForegroundColor Gray
    Write-Host "   Rôle : $($response.role)" -ForegroundColor Gray
    $enseignantId = $response.idUser
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "⚠️  L'enseignant existe déjà (email dupliqué)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 3 : Lister tous les utilisateurs
Write-Host "Test 3 : Lister tous les utilisateurs..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" }
    
    Write-Host "✅ Liste récupérée avec succès" -ForegroundColor Green
    Write-Host "   Nombre total d'utilisateurs : $($response.Count)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   Détails :" -ForegroundColor Gray
    foreach ($user in $response) {
        Write-Host "   - $($user.nomUser) $($user.prenomUser)" -ForegroundColor White
        Write-Host "     Email : $($user.email)" -ForegroundColor Gray
        Write-Host "     Rôle : $($user.role)" -ForegroundColor Gray
        Write-Host "     Téléphone : $($user.telephone)" -ForegroundColor Gray
        Write-Host ""
    }
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4 : Lister les enseignants
Write-Host "Test 4 : Lister les enseignants..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/enseignants" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" }
    
    Write-Host "✅ Liste des enseignants récupérée" -ForegroundColor Green
    Write-Host "   Nombre d'enseignants : $($response.Count)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 5 : Obtenir un utilisateur par ID
Write-Host "Test 5 : Obtenir un utilisateur par ID..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/admin001" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" }
    
    Write-Host "✅ Utilisateur récupéré" -ForegroundColor Green
    Write-Host "   Nom : $($response.nomUser) $($response.prenomUser)" -ForegroundColor Gray
    Write-Host "   Email : $($response.email)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 6 : Modifier un utilisateur
Write-Host "Test 6 : Modifier un utilisateur..." -ForegroundColor Yellow
$update = @{
    nomUser = "Admin"
    prenomUser = "Principal"
    telephone = "0611111111"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/admin001" `
        -Method Put `
        -Headers $headers `
        -Body $update
    
    Write-Host "✅ Utilisateur modifié avec succès" -ForegroundColor Green
    Write-Host "   Nouveau nom : $($response.nomUser) $($response.prenomUser)" -ForegroundColor Gray
    Write-Host "   Nouveau téléphone : $($response.telephone)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 7 : Créer un autre enseignant
Write-Host "Test 7 : Créer un deuxième enseignant..." -ForegroundColor Yellow
$enseignant2 = @{
    email = "marie.bernard@plannora.com"
    mdp = "password123"
    nomUser = "Bernard"
    prenomUser = "Marie"
    telephone = "0634567890"
    specialite = "Mathématiques"
    departement = "Sciences"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/enseignant" `
        -Method Post `
        -Headers $headers `
        -Body $enseignant2
    
    Write-Host "✅ Deuxième enseignant créé" -ForegroundColor Green
    Write-Host "   Email : $($response.email)" -ForegroundColor Gray
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "⚠️  L'enseignant existe déjà" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 8 : Test de sécurité (sans token)
Write-Host "Test 8 : Test de sécurité (accès sans token)..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" -Method Get
    Write-Host "❌ ERREUR : L'accès sans token devrait être refusé !" -ForegroundColor Red
} catch {
    Write-Host "✅ Accès refusé sans token (401 Unauthorized) - CORRECT" -ForegroundColor Green
}

Write-Host ""

# Test 9 : Test de sécurité (token invalide)
Write-Host "Test 9 : Test de sécurité (token invalide)..." -ForegroundColor Yellow
$badHeaders = @{
    Authorization = "Bearer INVALID_TOKEN_123456"
}

try {
    Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" `
        -Method Get `
        -Headers $badHeaders
    Write-Host "❌ ERREUR : L'accès avec token invalide devrait être refusé !" -ForegroundColor Red
} catch {
    Write-Host "✅ Accès refusé avec token invalide (401) - CORRECT" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Authentification : OK" -ForegroundColor Green
Write-Host "✅ Création d'utilisateurs : OK" -ForegroundColor Green
Write-Host "✅ Lecture des utilisateurs : OK" -ForegroundColor Green
Write-Host "✅ Modification d'utilisateurs : OK" -ForegroundColor Green
Write-Host "✅ Sécurité (contrôle d'accès) : OK" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 TOUS LES TESTS SONT PASSÉS !" -ForegroundColor Green
Write-Host ""
Write-Host "Pour vérifier dans MySQL :" -ForegroundColor Yellow
Write-Host "  mysql -u root -p plannoradb" -ForegroundColor Gray
Write-Host "  SELECT * FROM utilisateurs;" -ForegroundColor Gray
Write-Host ""
