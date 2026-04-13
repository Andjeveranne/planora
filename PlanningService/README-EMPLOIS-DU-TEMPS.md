# Système de Génération d'Emplois du Temps

## Vue d'ensemble

Ce système utilise l'algorithme de Ford-Fulkerson pour générer automatiquement les emplois du temps des enseignants et des classes en tenant compte de toutes les contraintes et relations entre les entités.

## Architecture

### Entités principales

1. **EmploiDuTemps** : Représente un cours planifié
   - Classe, Enseignant, UE, Salle
   - Date/heure de début et fin
   - Type de cours (CM, TD, TP)
   - Semaine et jour de la semaine

2. **UEClasse** : Relation entre UE et Classes
   - Définit les heures de CM, TD, TP pour chaque UE par classe

3. **UEEnseignant** : Relation entre UE et Enseignants
   - Définit les types de cours qu'un enseignant peut dispenser pour une UE

4. **Disponibilite** : Créneaux de disponibilité des enseignants

### Algorithme Ford-Fulkerson

L'algorithme modélise le problème de planification comme un graphe de flot :

```
Source → Créneaux → Salles → Sink
```

#### Nœuds du graphe :
- **Source** : Point de départ
- **Créneaux** : Créneaux horaires disponibles (slot_semaine_jour_heure)
- **Salles** : Salles disponibles (salle_id)
- **Sink** : Point d'arrivée

#### Contraintes respectées :
1. **Disponibilité enseignant** : L'enseignant doit être disponible
2. **Capacité salle** : La salle doit avoir une capacité suffisante
3. **Type de salle** : Compatible avec le type de cours (CM/TD/TP)
4. **Pas de conflit** : Aucun chevauchement pour classe/enseignant/salle
5. **Statut salle** : Salle disponible (non en maintenance)

## API Endpoints

### Génération d'emplois du temps
```http
POST /api/planning/emplois-du-temps/generer?semaine=1&idEcole=ecole-1
```

### Consultation d'emplois du temps
```http
GET /api/planning/emplois-du-temps/classe/{classeId}?semaine=1
GET /api/planning/emplois-du-temps/enseignant/{enseignantId}?semaine=1
```

### Gestion des relations UE-Classe
```http
POST /api/planning/ue-classes
GET /api/planning/ue-classes
GET /api/planning/ue-classes/classe/{classeId}
GET /api/planning/ue-classes/ue/{ueId}
PUT /api/planning/ue-classes/{id}
DELETE /api/planning/ue-classes/{id}
```

### Gestion des relations UE-Enseignant
```http
POST /api/planning/ue-enseignants
GET /api/planning/ue-enseignants
GET /api/planning/ue-enseignants/enseignant/{enseignantId}
GET /api/planning/ue-enseignants/ue/{ueId}
PUT /api/planning/ue-enseignants/{id}
DELETE /api/planning/ue-enseignants/{id}
```

## Configuration requise

### 1. Relations UE-Classe
Avant de générer les emplois du temps, configurez les relations entre UE et Classes :

```json
{
  "ueId": 1,
  "classeId": "classe-1",
  "heuresCM": 20,
  "heuresTD": 10,
  "heuresTP": 10
}
```

### 2. Relations UE-Enseignant
Configurez quels enseignants peuvent dispenser quels types de cours pour chaque UE :

```json
{
  "ueId": 1,
  "enseignantId": "enseignant-1",
  "peutCM": true,
  "peutTD": true,
  "peutTP": false
}
```

### 3. Disponibilités des enseignants
Les enseignants doivent avoir saisi leurs disponibilités via l'interface dédiée.

### 4. Salles configurées
Les salles doivent être créées avec :
- Capacité appropriée
- Type correct (AMPHITHEATRE, SALLE_COURS, LABORATOIRE, etc.)
- Statut DISPONIBLE

## Interface utilisateur

### Dashboard Administrateur
L'interface de génération d'emplois du temps se trouve dans le dashboard administrateur :

1. **Configuration** : Définir la semaine et l'école
2. **Relations UE-Classe** : Gérer les heures par type de cours
3. **Relations UE-Enseignant** : Gérer les compétences des enseignants
4. **Génération** : Bouton unique pour générer tous les emplois du temps
5. **Visualisation** : Tableau des cours planifiés

### Fonctionnalités
- ✅ Génération automatique avec Ford-Fulkerson
- ✅ Gestion des relations UE-Classe et UE-Enseignant
- ✅ Visualisation des emplois du temps générés
- ✅ Suppression des emplois du temps existants
- ✅ Interface responsive et intuitive

## Algorithme détaillé

### 1. Préparation des données
```java
// Récupération des données depuis les autres services
Map<String, Object> classesData = getClassesData();
Map<String, Object> sallesData = getSallesData(idEcole);
Map<String, Object> uesData = getUEsData();

// Génération des créneaux (8h-18h, Lundi-Vendredi)
List<TimeSlot> timeSlots = generateTimeSlots(semaine);
```

### 2. Création des demandes de cours
```java
// Pour chaque relation UE-Classe
for (UEClasse ueClasse : ueClasses) {
    // Trouver l'enseignant compétent
    UEEnseignant enseignant = findEnseignantForUE(ueClasse.getUeId(), "CM");
    
    // Créer une demande de cours
    SchedulingRequest request = new SchedulingRequest();
    request.setClasseId(ueClasse.getClasseId());
    request.setUeId(ueClasse.getUeId());
    request.setEnseignantId(enseignant.getEnseignantId());
    request.setTypeCours("CM");
    request.setHeuresRequises(ueClasse.getHeuresCM());
}
```

### 3. Construction du graphe de flot
```java
FlowNetwork network = new FlowNetwork();

// Source → Créneaux (capacité 1)
for (TimeSlot slot : validSlots) {
    network.addEdge("source", "slot_" + slot.getId(), 1);
}

// Créneaux → Salles (capacité 1)
for (TimeSlot slot : validSlots) {
    for (Salle salle : compatibleSalles) {
        if (isSalleAvailable(salle, slot)) {
            network.addEdge("slot_" + slot.getId(), "salle_" + salle.getId(), 1);
        }
    }
}

// Salles → Sink (capacité 1)
for (Salle salle : salles) {
    network.addEdge("salle_" + salle.getId(), "sink", 1);
}
```

### 4. Application de Ford-Fulkerson
```java
int maxFlow = fordFulkerson(network, "source", "sink");

// Extraction de la solution
if (maxFlow > 0) {
    SchedulingResult result = extractSolutionFromFlow(network, request, timeSlots, salles);
    results.add(result);
}
```

## Avantages de l'approche

1. **Optimalité** : Ford-Fulkerson garantit une solution optimale
2. **Contraintes respectées** : Toutes les contraintes sont modélisées dans le graphe
3. **Flexibilité** : Facile d'ajouter de nouvelles contraintes
4. **Performance** : Algorithme efficace pour les tailles de problème typiques
5. **Robustesse** : Gestion des cas où aucune solution n'existe

## Limitations et améliorations futures

### Limitations actuelles
- Génération par semaine uniquement
- Pas de préférences d'horaires pour les enseignants
- Pas de gestion des salles spécialisées par UE

### Améliorations possibles
1. **Génération multi-semaines** : Planification sur plusieurs semaines
2. **Préférences enseignants** : Poids dans l'algorithme
3. **Optimisation multi-objectifs** : Minimiser les déplacements, équilibrer la charge
4. **Interface de visualisation** : Calendrier interactif
5. **Export** : PDF, iCal, Excel

## Dépendances

### Backend (Spring Boot)
- Spring Data JPA
- Spring Security
- Spring WebFlux (appels inter-services)
- MySQL Connector
- Lombok

### Frontend (Angular)
- Angular Material
- HttpClient
- Reactive Forms

## Installation et démarrage

1. **Base de données** : Créer la base `plannoradb`
2. **Services** : Démarrer tous les microservices
3. **Configuration** : Configurer les relations UE-Classe et UE-Enseignant
4. **Disponibilités** : Saisir les disponibilités des enseignants
5. **Génération** : Utiliser l'interface administrateur

## Tests

### Tests unitaires
- Algorithme Ford-Fulkerson
- Validation des contraintes
- Services de gestion des relations

### Tests d'intégration
- Génération complète d'emplois du temps
- Appels inter-services
- Persistance des données

### Tests de performance
- Génération avec 100+ enseignants
- 50+ classes et 200+ UE
- Mesure des temps de réponse