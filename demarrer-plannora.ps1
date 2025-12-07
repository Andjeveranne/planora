# Script de demarrage simplifie pour Plannora
Write-Host "=== Demarrage de Plannora ===" -ForegroundColor Cyan

# Verifier MySQL
Write-Host "`n1. Verification de MySQL..." -ForegroundColor Yellow
try {
    $mysqlTest = mysql -u root -p"2003Rty." -e "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] MySQL est accessible" -ForegroundColor Green
    } else {
        Write-Host "[ERREUR] MySQL n'est pas accessible" -ForegroundColor Red
        Write-Host "[ATTENTION] Demarrez MySQL avant de continuer" -ForegroundColor Yellow
        exit
    }
} catch {
    Write-Host "[ERREUR] MySQL n'est pas installe ou n'est pas demarre" -ForegroundColor Red
    Write-Host "[ATTENTION] Installez et demarrez MySQL avant de continuer" -ForegroundColor Yellow
    exit
}

# Creer la base de donnees si elle n'existe pas
Write-Host "`n2. Verification de la base de donnees..." -ForegroundColor Yellow
$dbExists = mysql -u root -p"2003Rty." -e "SHOW DATABASES LIKE 'PlannoraDB';" 2>&1
if ($dbExists -match "PlannoraDB") {
    Write-Host "[OK] Base de donnees PlannoraDB existe" -ForegroundColor Green
} else {
    Write-Host "[INFO] Creation de la base de donnees PlannoraDB..." -ForegroundColor Yellow
    mysql -u root -p"2003Rty." -e "CREATE DATABASE PlannoraDB;"
    Write-Host "[OK] Base de donnees creee" -ForegroundColor Green
}

Write-Host "`n3. Demarrage des services..." -ForegroundColor Yellow
Write-Host "[INFO] Cela peut prendre quelques minutes..." -ForegroundColor Gray

# Demarrer Eureka
Write-Host "`n   [1/5] Demarrage d'Eureka (port 8761)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd EurekaService/eureka/eureka; Write-Host '=== EUREKA SERVER ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 5

# Demarrer le Service d'Authentification
Write-Host "   [2/5] Demarrage du Service d'Authentification (port 8085)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd AuthentificationService/Authentification/authentification; Write-Host '=== SERVICE AUTHENTIFICATION ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 5

# Demarrer le Service Utilisateur
Write-Host "   [3/5] Demarrage du Service Utilisateur (port 8086)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd UserService/user-service; Write-Host '=== SERVICE UTILISATEUR ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 5

# Demarrer la Gateway
Write-Host "   [4/5] Demarrage de la Gateway (port 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd GatewayService/gateway/gateway; Write-Host '=== GATEWAY ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 5

# Demarrer le Frontend
Write-Host "   [5/5] Demarrage du Frontend Angular (port 4200)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd Frontend/plannora-frontend; Write-Host '=== FRONTEND ANGULAR ===' -ForegroundColor Cyan; npm start"

Write-Host "`n[OK] Tous les services sont en cours de demarrage!" -ForegroundColor Green
Write-Host "`n[INFO] Attendez environ 2-3 minutes que tous les services soient prets..." -ForegroundColor Yellow

Write-Host "`n=== URLs des Services ===" -ForegroundColor Cyan
Write-Host "Eureka:           http://localhost:8761" -ForegroundColor White
Write-Host "Authentification: http://localhost:8085/api/auth" -ForegroundColor White
Write-Host "UserService:      http://localhost:8086" -ForegroundColor White
Write-Host "Gateway:          http://localhost:8081" -ForegroundColor White
Write-Host "Frontend:         http://localhost:4200" -ForegroundColor Green

Write-Host "`n=== Identifiants de Test ===" -ForegroundColor Cyan
Write-Host "Admin:      admin@plannora.com / password123" -ForegroundColor Green
Write-Host "Enseignant: enseignant@plannora.com / password123" -ForegroundColor Green

Write-Host "`n[CONSEIL] Attendez de voir dans les fenetres de services:" -ForegroundColor Yellow
Write-Host "   - 'Started AuthentificationApplication' pour l'authentification" -ForegroundColor Gray
Write-Host "   - 'Utilisateurs de test crees avec succes!' dans les logs" -ForegroundColor Gray
Write-Host "   - 'Compiled successfully' pour le frontend" -ForegroundColor Gray

Write-Host "`n[TEST] Pour tester l'authentification apres le demarrage :" -ForegroundColor Yellow
Write-Host "   ./diagnostic-auth.ps1" -ForegroundColor Cyan

Write-Host "`n=== Demarrage en cours... ===" -ForegroundColor Cyan
