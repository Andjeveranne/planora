# =====================================================
# PLANNORA SYSTEM - COMPLETE RESET SCRIPT
# =====================================================
# This PowerShell script will:
# 1. Stop all running services
# 2. Reset all databases
# 3. Clear application logs
# 4. Restart services in correct order
# =====================================================

Write-Host "🔄 PLANNORA SYSTEM RESET STARTING..." -ForegroundColor Yellow
Write-Host "This will delete ALL data and restart all services." -ForegroundColor Red
Write-Host ""

# Confirm with user
$confirmation = Read-Host "Are you sure you want to reset ALL system data? (type 'YES' to confirm)"
if ($confirmation -ne "YES") {
    Write-Host "❌ Reset cancelled." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "🛑 Step 1: Stopping all services..." -ForegroundColor Cyan

# Stop all Java processes (services)
try {
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host "✅ All Java services stopped" -ForegroundColor Green
} catch {
    Write-Host "⚠️  No Java services were running" -ForegroundColor Yellow
}

# Stop Angular dev server if running
try {
    Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {$_.ProcessName -eq "node"} | Stop-Process -Force
    Write-Host "✅ Angular dev server stopped" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Angular dev server was not running" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🗄️  Step 2: Resetting databases..." -ForegroundColor Cyan

# Execute SQL reset script
try {
    mysql -u root -p2003Rty. < reset-all-data.sql
    Write-Host "✅ All databases reset successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Database reset failed. Please check MySQL connection." -ForegroundColor Red
    Write-Host "Make sure MySQL is running and password is correct." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "🧹 Step 3: Clearing application logs..." -ForegroundColor Cyan

# Clear logs directory
if (Test-Path "logs") {
    Remove-Item "logs\*" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "✅ Application logs cleared" -ForegroundColor Green
} else {
    Write-Host "⚠️  No logs directory found" -ForegroundColor Yellow
}

# Clear target directories (compiled classes)
$targetDirs = @(
    "UserService\user-service\target",
    "PlanningService\planning-service\target", 
    "GatewayService\gateway\gateway\target",
    "EurekaService\eureka\eureka\target",
    "AuthentificationService\Authentification\authentification\target"
)

foreach ($dir in $targetDirs) {
    if (Test-Path $dir) {
        Remove-Item $dir -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "✅ Cleared $dir" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "🚀 Step 4: Starting services in correct order..." -ForegroundColor Cyan

Write-Host "Starting Eureka Service..." -ForegroundColor White
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'EurekaService\eureka\eureka'; mvn spring-boot:run"
Write-Host "⏳ Waiting 30 seconds for Eureka to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "Starting Authentication Service..." -ForegroundColor White
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'AuthentificationService\Authentification\authentification'; mvn spring-boot:run"
Start-Sleep -Seconds 15

Write-Host "Starting User Service..." -ForegroundColor White
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'UserService\user-service'; mvn spring-boot:run"
Start-Sleep -Seconds 15

Write-Host "Starting Planning Service..." -ForegroundColor White
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'PlanningService\planning-service'; mvn spring-boot:run"
Start-Sleep -Seconds 15

Write-Host "Starting Gateway Service..." -ForegroundColor White
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'GatewayService\gateway\gateway'; mvn spring-boot:run"
Start-Sleep -Seconds 15

Write-Host "Starting Frontend..." -ForegroundColor White
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'Frontend\plannora-frontend'; npm start"

Write-Host ""
Write-Host "✅ SYSTEM RESET COMPLETE!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Services will be available at:" -ForegroundColor Cyan
Write-Host "   • Frontend: http://localhost:4200" -ForegroundColor White
Write-Host "   • Eureka: http://localhost:8761" -ForegroundColor White
Write-Host "   • Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "   • Auth Service: http://localhost:8085" -ForegroundColor White
Write-Host "   • User Service: http://localhost:8086" -ForegroundColor White
Write-Host "   • Planning Service: http://localhost:8088" -ForegroundColor White
Write-Host ""
Write-Host "📋 Next Steps for Testing:" -ForegroundColor Yellow
Write-Host "1. Wait 2-3 minutes for all services to fully start" -ForegroundColor White
Write-Host "2. Go to http://localhost:4200" -ForegroundColor White
Write-Host "3. Create admin account and teachers" -ForegroundColor White
Write-Host "4. Test all functionalities from scratch" -ForegroundColor White
Write-Host ""
Write-Host "📖 Use the testing guide: COMPLETE-TESTING-GUIDE.md" -ForegroundColor Cyan