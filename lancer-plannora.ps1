# Script de demarrage Plannora
Write-Host "=== Demarrage de Plannora ===" -ForegroundColor Cyan

Write-Host "`n1. MySQL est en cours d'execution" -ForegroundColor Green

Write-Host "`n2. Demarrage des services..." -ForegroundColor Yellow
Write-Host "Cela peut prendre quelques minutes..." -ForegroundColor Gray

# Demarrer Eureka
Write-Host "`n   Demarrage d'Eureka (port 8761)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd EurekaService/eureka/eureka; Write-Host '=== EUREKA SERVER ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer le Service d'Authentification
Write-Host "   Demarrage du Service d'Authentification (port 8085)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd AuthentificationService/Authentification/authentification; Write-Host '=== SERVICE AUTHENTIFICATION ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer le Service Utilisateur
Write-Host "   Demarrage du Service Utilisateur (port 8086)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd UserService/user-service; Write-Host '=== SERVICE UTILISATEUR ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer la Gateway
Write-Host "   Demarrage de la Gateway (port 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd GatewayService/gateway/gateway; Write-Host '=== GATEWAY ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer le Frontend
Write-Host "   Demarrage du Frontend Angular (port 4200)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd Frontend/plannora-frontend; Write-Host '=== FRONTEND ANGULAR ===' -ForegroundColor Cyan; npm start"

Write-Host "`nTous les services sont en cours de demarrage!" -ForegroundColor Green
Write-Host "Attendez environ 2-3 minutes que tous les services soient prets..." -ForegroundColor Yellow

Write-Host "`n=== URLs des Services ===" -ForegroundColor Cyan
Write-Host "Eureka:           http://localhost:8761" -ForegroundColor White
Write-Host "Authentification: http://localhost:8085/api/auth" -ForegroundColor White
Write-Host "UserService:      http://localhost:8086" -ForegroundColor White
Write-Host "Gateway:          http://localhost:8081" -ForegroundColor White
Write-Host "Frontend:         http://localhost:4200" -ForegroundColor Green

Write-Host "`n=== Identifiants de Test ===" -ForegroundColor Cyan
Write-Host "Admin:      admin@plannora.com / password123" -ForegroundColor Green
Write-Host "Enseignant: enseignant@plannora.com / password123" -ForegroundColor Green
