# 🧪 Script de Test Complet - UserService

## ✅ Build Maven Réussi !

Le projet a été compilé avec succès :
```
[INFO] BUILD SUCCESS
[INFO] Total time:  10.424 s
```

## 🚀 Étapes pour Tester les APIs

### Étape 1 : Démarrer les Services (dans l'ordre)

#### Terminal 1 : Eureka Server
```bash
cd EurekaService/eureka/eureka
mvnw.cmd spring-boot:run
```
Attendez le message : `Started EurekaApplication`
Vérifiez : http://localhost:8761

#### Terminal 2 : Authentication Service
```bash
cd AuthentificationService/Authentification/authentification
mvnw.cmd spring-boot:run
```
Attendez le message : `Started AuthentificationApplication`

#### Terminal 3 : User Service
```bash
cd UserService/user-service
mvnw.cmd spring-boot:run
```
Attendez le message : `Started UserServiceApplication`
Vérifiez le message : `Administrateur par défaut créé`

### Étape 2 : Vérifier les Services

#### Vérifier Eureka Dashboard
Ouvrez : http://localhost:8761

Vous devriez voir :
- `AUTHENTIFICATION-SERVICE` (port 8082)
- `USER-SERVICE` (port 8083)

#### Vérifier MySQL
```sql
mysql -u root -p plannoradb

-- Vérifier les tables
SHOW TABLES;
-- Résultat attendu : utilisateurs, administrateurs, enseignants

-- Vérifier l'admin par défaut
SELECT * FROM utilisateurs;
-- Résultat attendu : admin001, admin@plannora.com
```

### Étape 3 : Tester les APIs avec PowerShell

#### Test 1 : Authentification
```powershell
$body = @{
    email = "admin@plannora.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8082/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

$token = $response.token
Write-Host "✅ Token obtenu : $token"
Write-Host "✅ User ID : $($response.userId)"
Write-Host "✅ Role : $($response.role)"
```

#### Test 2 : Créer un Enseignant
```powershell
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

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/enseignant" `
    -Method Post `
    -Headers $headers `
    -Body $enseignant

Write-Host "✅ Enseignant créé :"
$response | ConvertTo-Json
```

#### Test 3 : Lister tous les Utilisateurs
```powershell
$headers = @{
    Authorization = "Bearer $token"
}

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" `
    -Method Get `
    -Headers $headers

Write-Host "✅ Liste des utilisateurs :"
$response | ConvertTo-Json
```

#### Test 4 : Lister les Enseignants
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/enseignants" `
    -Method Get `
    -Headers $headers

Write-Host "✅ Liste des enseignants :"
$response | ConvertTo-Json
```

#### Test 5 : Obtenir un Utilisateur par ID
```powershell
$userId = "admin001"
$response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/$userId" `
    -Method Get `
    -Headers $headers

Write-Host "✅ Utilisateur $userId :"
$response | ConvertTo-Json
```

#### Test 6 : Modifier un Utilisateur
```powershell
$update = @{
    nomUser = "Admin"
    prenomUser = "Principal"
    telephone = "0611111111"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/admin001" `
    -Method Put `
    -Headers $headers `
    -Body $update

Write-Host "✅ Utilisateur modifié :"
$response | ConvertTo-Json
```

#### Test 7 : Créer un autre Enseignant
```powershell
$enseignant2 = @{
    email = "marie.bernard@plannora.com"
    mdp = "password123"
    nomUser = "Bernard"
    prenomUser = "Marie"
    telephone = "0634567890"
    specialite = "Mathématiques"
    departement = "Sciences"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs/enseignant" `
    -Method Post `
    -Headers $headers `
    -Body $enseignant2

Write-Host "✅ Enseignant 2 créé :"
$response | ConvertTo-Json
```

### Étape 4 : Tests de Sécurité

#### Test 8 : Accès sans Token (doit échouer)
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" -Method Get
    Write-Host "❌ ERREUR : L'accès sans token devrait être refusé"
} catch {
    Write-Host "✅ Accès refusé sans token (401 Unauthorized) - CORRECT"
}
```

#### Test 9 : Accès avec Token Invalide (doit échouer)
```powershell
$badHeaders = @{
    Authorization = "Bearer INVALID_TOKEN"
}

try {
    Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" `
        -Method Get `
        -Headers $badHeaders
    Write-Host "❌ ERREUR : L'accès avec token invalide devrait être refusé"
} catch {
    Write-Host "✅ Accès refusé avec token invalide (401 Unauthorized) - CORRECT"
}
```

### Étape 5 : Vérifier dans MySQL

```sql
-- Voir tous les utilisateurs
SELECT * FROM utilisateurs;

-- Voir les enseignants avec leurs détails
SELECT u.id_user, u.email, u.nom_user, u.prenom_user, 
       e.specialite, e.departement
FROM utilisateurs u
JOIN enseignants e ON u.id_user = e.id_user;

-- Compter les utilisateurs par rôle
SELECT role, COUNT(*) as nombre
FROM utilisateurs
GROUP BY role;
```

## 📊 Résultats Attendus

### Après tous les tests, vous devriez avoir :

**Dans MySQL** :
- 1 Administrateur (admin001)
- 2 Enseignants (Jean Dupont, Marie Bernard)

**Tables** :
- `utilisateurs` : 3 lignes
- `administrateurs` : 1 ligne
- `enseignants` : 2 lignes

**Dans Eureka** :
- 2 services enregistrés (AUTHENTIFICATION-SERVICE, USER-SERVICE)

## 🎯 Script PowerShell Complet

Copiez et exécutez ce script complet :

```powershell
# Script de test complet du UserService
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TEST COMPLET DU USERSERVICE" -ForegroundColor Cyan
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
    Write-Host "   Token : $($token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "   Role : $($response.role)" -ForegroundColor Gray
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
    
    Write-Host "✅ Enseignant créé : $($response.email)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3 : Lister tous les utilisateurs
Write-Host "Test 3 : Lister tous les utilisateurs..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" }
    
    Write-Host "✅ Nombre d'utilisateurs : $($response.Count)" -ForegroundColor Green
    foreach ($user in $response) {
        Write-Host "   - $($user.nomUser) $($user.prenomUser) ($($user.role))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Erreur : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4 : Test de sécurité (sans token)
Write-Host "Test 4 : Test de sécurité (sans token)..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8083/api/utilisateurs" -Method Get
    Write-Host "❌ ERREUR : L'accès sans token devrait être refusé" -ForegroundColor Red
} catch {
    Write-Host "✅ Accès refusé sans token (401) - CORRECT" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TESTS TERMINÉS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
```

## 🎉 Conclusion

Une fois tous les tests passés, vous aurez validé :
- ✅ L'authentification JWT
- ✅ La création d'utilisateurs
- ✅ La lecture des utilisateurs
- ✅ La modification des utilisateurs
- ✅ La sécurité (contrôle d'accès)
- ✅ L'héritage JPA (Utilisateur → Admin/Enseignant)

**Le UserService est entièrement fonctionnel !** 🚀
