# Script de migration de GitHub vers GitLab
# Ce script retire les sous-modules et prépare le projet pour GitLab

Write-Host "=== Migration de GitHub vers GitLab ===" -ForegroundColor Cyan
Write-Host ""

# Étape 1: Retirer les sous-modules Git
Write-Host "Étape 1: 