# Gestion des Disponibilités des Enseignants

## 📋 Vue d'ensemble

Ce module implémente la gestion complète des disponibilités des enseignants dans le système Plannora. Il permet aux enseignants de définir leurs créneaux de disponibilité et aux administrateurs de consulter et gérer ces informations.

## 🏗️ Architecture

### Entités

#### Disponibilite
- **id** : Identifiant unique
- **jourSemaine** : Jour de la semaine (MONDAY, TUESDAY, etc.)
- **heureDebut** : Heure de début du créneau
- **heureFin** : Heure de fin du créneau
- **enseignant** : Référence vers l'enseignant

#### Relations
- **Enseignant ↔ Disponibilite** : One-to-Many
- Un enseignant peut avoir plusieurs créneaux de disponibilité
- Suppression en cascade

## 🔧 Fonctionnalités

### 1. Création de Disponibilité
- **POST /api/disponibilites**
- Autorisation : ADMIN ou ENSEIGNANT (ses propres disponibilités)
- Validations : heures cohérentes, pas de chevauchement

### 2. Modification de Disponibilité
- **PUT /api/disponibilites/{id}**
- Autorisation : ADMIN ou propriétaire

### 3. Suppression de Disponibilité
- **DELETE /api/disponibilites/{id}**
- Autorisation : ADMIN ou propriétaire

### 4. Consultation des Disponibilités
- **GET /api/disponibilites/enseignant/{id}** : Toutes les disponibilités
- **GET /api/disponibilites/enseignant/{id}/jour/{jour}** : Par jour

### 5. Recherche d'Enseignants Disponibles
- **GET /api/disponibilites/recherche**
- Paramètres : jourSemaine, heureDebut, heureFin
- Autorisation : ADMIN uniquement

## 🔒 Sécurité

- JWT obligatoire
- Contrôle d'accès par rôle
- Vérification de propriété pour les enseignants

## 📊 Exemples d'utilisation

### Créer une disponibilité
```json
POST /api/disponibilites
{
  "jourSemaine": "MONDAY",
  "heureDebut": "08:00:00",
  "heureFin": "10:00:00",
  "enseignantId": 1
}
```

### Rechercher des enseignants disponibles
```
GET /api/disponibilites/recherche?jourSemaine=MONDAY&heureDebut=08:00&heureFin=10:00
```

## 🧪 Tests

Utilisez le script `test-disponibilites.ps1` pour tester toutes les fonctionnalités.

## 📝 Notes

- Les heures sont au format HH:mm:ss
- Les jours utilisent les constantes Java DayOfWeek
- Validation automatique des chevauchements
- Logs détaillés pour le debugging