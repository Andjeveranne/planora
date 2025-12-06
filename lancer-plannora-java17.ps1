# Script de demarrage Plannora avec Java 17
Write-Host "=== Demarrage de Plannora ===" -ForegroundColor Cyan

# Configurer Java 17
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "`n1. Configuration de Java 17..." -ForegroundColor Yellow
java -version
Write-Host "`n2. MySQL est en cours d'execution" -ForegroundColor Green

Write-Host "`n3. Demarrage des services..." -ForegroundColor Yellow
Write-Host "Cela peut prendre quelques minutes..." -ForegroundColor Gray

# Demarrer Eureka
Write-Host "`n   Demarrage d'Eureka (port 8761)..." -ForegroundColor Cyan
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME='$javaHome'; cd EurekaService/eureka/eureka; Write-Host '=== EUREKA SERVER ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer le Service d'Authentification
Write-Host "   Demarrage du Service d'Authentification (port 8085)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME='$javaHome'; cd AuthentificationService/Authentification/authentification; Write-Host '=== SERVICE AUTHENTIFICATION ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer le Service Utilisateur
Write-Host "   Demarrage du Service Utilisateur (port 8086)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME='$javaHome'; cd UserService/user-service; Write-Host '=== SERVICE UTILISATEUR ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
Start-Sleep -Seconds 3

# Demarrer la Gateway
Write-Host "   Demarrage de la Gateway (port 8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$env:JAVA_HOME='$javaHome'; cd GatewayService/gateway/gateway; Write-Host '=== GATEWAY ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
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
Write-Host "Admin:      admin.systeme@saintjeaningenieur.org / password123" -ForegroundColor Green
Write-Host "Enseignant: jean.dupont@saintjeaningenieur.org / password123" -ForegroundColor Green
