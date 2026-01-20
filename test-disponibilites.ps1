# Script PowerShell pour tester les fonctionnalités de disponibilité des enseignants
# Assurez-vous que les services sont démarrés avant d'exécuter ce script

$baseUrl = "http://localhost:8086/api"
$authUrl = "http://localhost:8085/api/auth"

Write-Host "=== Test des Fonctionnalités de Disponibilité ===" -ForegroundColor Cyan

# 1. Authentification en tant qu'admin
Write-Host "`n1. Authentification en tant qu'admin..." -ForegroundColor Yellow
$loginData = @{
    email = "admin@plannora.com"
    motDePasse = "password123"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$authUrl/login" -Method POST -Body $loginData -ContentType "application/json"
    $token = $authResponse.token
    $headers = @{ "Authorization" = "Bearer $token" }
    Write-Host "✅ Authentification réussie" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur d'authentification: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 2. Lister les enseignants
Write-Host "`n2. Récupération de la liste des enseignants..." -ForegroundColor Yellow
try {
    $enseignants = Invoke-RestMethod -Uri "$baseUrl/utilisateurs/enseignants" -Method GET -Headers $headers
    if ($enseignants.Count -gt 0) {
        $enseignantId = $enseignants[0].id
        Write-Host "✅ Enseignants trouvés. Premier enseignant ID: $enseignantId" -ForegroundColor Green
        Write-Host "   Nom: $($enseignants[0].nom) $($enseignants[0].prenom)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Aucun enseignant trouvé" -ForegroundColor Red
        exit
    }
} catch {
    Write-Host "❌ Erreur lors de la récupération des enseignants: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 3. Créer une disponibilité
Write-Host "`n3. Création d'une disponibilité..." -ForegroundColor Yellow
$disponibiliteData = @{
    jourSemaine = "MONDAY"
    heureDebut = "08:00:00"
    heureFin = "10:00:00"
    enseignantId = $enseignantId
} | ConvertTo-Json

try {
    $nouvelleDisponibilite = Invoke-RestMethod -Uri "$baseUrl/disponibilites" -Method POST -Body $disponibiliteData -ContentType "application/json" -Headers $headers
    $disponibiliteId = $nouvelleDisponibilite.id
    Write-Host "✅ Disponibilité créée avec l'ID: $disponibiliteId" -ForegroundColor Green
    Write-Host "   Créneau: $($nouvelleDisponibilite.jourSemaine) de $($nouvelleDisponibilite.heureDebut) à $($nouvelleDisponibilite.heureFin)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Erreur lors de la création de la disponibilité: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Lister les disponibilités de l'enseignant
Write-Host "`n4. Récupération des disponibilités de l'enseignant..." -ForegroundColor Yellow
try {
    $disponibilites = Invoke-RestMethod -Uri "$baseUrl/disponibilites/enseignant/$enseignantId" -Method GET -Headers $headers
    Write-Host "✅ Disponibilités récupérées: $($disponibilites.Count) créneau(x)" -ForegroundColor Green
    foreach ($dispo in $disponibilites) {
        Write-Host "   - $($dispo.jourSemaine): $($dispo.heureDebut) - $($dispo.heureFin)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Erreur lors de la récupération des disponibilités: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Créer une autre disponibilité
Write-Host "`n5. Création d'une seconde disponibilité..." -ForegroundColor Yellow
$disponibiliteData2 = @{
    jourSemaine = "TUESDAY"
    heureDebut = "14:00:00"
    heureFin = "16:00:00"
    enseignantId = $enseignantId
} | ConvertTo-Json

try {
    $nouvelleDisponibilite2 = Invoke-RestMethod -Uri "$baseUrl/disponibilites" -Method POST -Body $disponibiliteData2 -ContentType "application/json" -Headers $headers
    Write-Host "✅ Seconde disponibilité créée avec l'ID: $($nouvelleDisponibilite2.id)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors de la création de la seconde disponibilité: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Test de chevauchement (doit échouer)
Write-Host "`n6. Test de chevauchement (doit échouer)..." -ForegroundColor Yellow
$disponibiliteChevauche = @{
    jourSemaine = "MONDAY"
    heureDebut = "09:00:00"
    heureFin = "11:00:00"
    enseignantId = $enseignantId
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "$baseUrl/disponibilites" -Method POST -Body $disponibiliteChevauche -ContentType "application/json" -Headers $headers
    Write-Host "❌ Le chevauchement n'a pas été détecté (problème)" -ForegroundColor Red
} catch {
    Write-Host "✅ Chevauchement correctement détecté et rejeté" -ForegroundColor Green
    Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Gray
}

# 7. Recherche d'enseignants disponibles
Write-Host "`n7. Recherche d'enseignants disponibles le lundi de 08:00 à 10:00..." -ForegroundColor Yellow
try {
    $enseignantsDisponibles = Invoke-RestMethod -Uri "$baseUrl/disponibilites/recherche?jourSemaine=MONDAY&heureDebut=08:00&heureFin=10:00" -Method GET -Headers $headers
    Write-Host "✅ Enseignants disponibles trouvés: $($enseignantsDisponibles.Count)" -ForegroundColor Green
    foreach ($id in $enseignantsDisponibles) {
        Write-Host "   - Enseignant ID: $id" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Erreur lors de la recherche d'enseignants disponibles: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Modification d'une disponibilité
if ($disponibiliteId) {
    Write-Host "`n8. Modification de la première disponibilité..." -ForegroundColor Yellow
    $disponibiliteModifiee = @{
        jourSemaine = "MONDAY"
        heureDebut = "08:30:00"
        heureFin = "10:30:00"
        enseignantId = $enseignantId
    } | ConvertTo-Json

    try {
        $result = Invoke-RestMethod -Uri "$baseUrl/disponibilites/$disponibiliteId" -Method PUT -Body $disponibiliteModifiee -ContentType "application/json" -Headers $headers
        Write-Host "✅ Disponibilité modifiée avec succès" -ForegroundColor Green
        Write-Host "   Nouveau créneau: $($result.jourSemaine) de $($result.heureDebut) à $($result.heureFin)" -ForegroundColor Gray
    } catch {
        Write-Host "❌ Erreur lors de la modification: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 9. Récupération des disponibilités par jour
Write-Host "`n9. Récupération des disponibilités du lundi..." -ForegroundColor Yellow
try {
    $disponibilitesLundi = Invoke-RestMethod -Uri "$baseUrl/disponibilites/enseignant/$enseignantId/jour/MONDAY" -Method GET -Headers $headers
    Write-Host "✅ Disponibilités du lundi: $($disponibilitesLundi.Count) créneau(x)" -ForegroundColor Green
    foreach ($dispo in $disponibilitesLundi) {
        Write-Host "   - $($dispo.heureDebut) - $($dispo.heureFin)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Erreur lors de la récupération des disponibilités du lundi: $($_.Exception.Message)" -ForegroundColor Red
}

# 10. Suppression d'une disponibilité
if ($disponibiliteId) {
    Write-Host "`n10. Suppression de la première disponibilité..." -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/disponibilites/$disponibiliteId" -Method DELETE -Headers $headers
        Write-Host "✅ Disponibilité supprimée avec succès" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors de la suppression: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Tests terminés ===" -ForegroundColor Cyan
Write-Host "`n💡 Conseils:" -ForegroundColor Yellow
Write-Host "- Vérifiez les logs du UserService pour plus de détails" -ForegroundColor Gray
Write-Host "- Utilisez Postman ou un autre client REST pour des tests plus avancés" -ForegroundColor Gray
Write-Host "- Consultez la documentation Swagger sur http://localhost:8086/swagger-ui.html" -ForegroundColor Gray