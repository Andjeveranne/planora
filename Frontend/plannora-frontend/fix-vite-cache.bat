@echo off
chcp 65001 >nul

echo ========================================
echo   Correction du cache Vite Angular
echo ========================================
echo.

echo [1/4] Nettoyage du cache Angular...
if exist ".angular" (
    rmdir /s /q ".angular"
    echo ✅ Cache Angular supprimé
) else (
    echo ℹ️ Pas de cache Angular trouvé
)

echo.
echo [2/4] Nettoyage du cache Vite...
if exist "node_modules\.vite" (
    rmdir /s /q "node_modules\.vite"
    echo ✅ Cache Vite supprimé
) else (
    echo ℹ️ Pas de cache Vite trouvé
)

echo.
echo [3/4] Nettoyage du cache npm...
npm cache clean --force
echo ✅ Cache npm nettoyé

echo.
echo [4/4] Redémarrage du serveur de développement...
echo ⚠️ Fermez le serveur Angular actuel (Ctrl+C) avant de continuer
pause

echo Démarrage du serveur Angular avec cache propre...
ng serve --port 4200 --host 0.0.0.0

pause